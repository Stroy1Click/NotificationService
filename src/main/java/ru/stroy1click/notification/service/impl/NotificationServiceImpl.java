package ru.stroy1click.notification.service.impl;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RList;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import ru.stroy1click.notification.dto.OrderDto;
import ru.stroy1click.notification.service.NotificationService;
import java.time.Duration;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    private final Sinks.Many<OrderDto> sink = Sinks.many().replay().limit(100);

    private final RList<OrderDto> redisList;

    private final RTopic redisTopic;

    private static final String LIST_KEY = "notifications:list";

    private static final String TOPIC_KEY = "notifications:topic";

    public NotificationServiceImpl(RedissonClient redissonClient) {
        this.redisList = redissonClient.getList(LIST_KEY);
        this.redisList.expire(Duration.ofDays(1));

        this.redisTopic = redissonClient.getTopic(TOPIC_KEY);

        this.redisTopic.addListener(OrderDto.class, (channel, order) -> {
            this.sink.tryEmitNext(order);
        });
    }

    @Override
    public Mono<Void> send(OrderDto orderDto) {
        return Mono.just(orderDto)
                .doOnNext(this.redisList::addFirst)
                .doOnNext(this.redisTopic::publish)
                .then();
    }

    @Override
    public Flux<OrderDto> getOrders() {
        return this.sink.asFlux();
    }

    private Mono<Void> loadHistory(){
        return Flux.fromIterable(this.redisList.get())
                .doOnNext(this.sink::tryEmitNext)
                .then();
    }

    @PostConstruct
    public void init() { //с sinks надо работать только тогда, когда бин полностью сформировался
        loadHistory().subscribe();
    }
}
