package ru.stroy1click.notification.service.impl;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.*;
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

    private final RListReactive<OrderDto> redisList;

    private final RTopicReactive redisTopic;

    private static final String LIST_KEY = "notifications:list";

    private static final String TOPIC_KEY = "notifications:topic";

    public NotificationServiceImpl(RedissonReactiveClient client) {
        this.redisList = client.getList(LIST_KEY);
        this.redisList.expire(Duration.ofDays(1)).subscribe();

        RTopicReactive topic = client.getTopic(TOPIC_KEY);

        topic.getMessages(OrderDto.class)
                .doOnNext(sink::tryEmitNext)
                .subscribe();

        this.redisTopic = topic;
    }

    @Override
    public Mono<Void> send(Mono<OrderDto> orderDto) {
        return orderDto.flatMap(order ->
                redisList.add(order)
                        .then(redisTopic.publish(order))
                        .then(Mono.fromRunnable(() -> sink.tryEmitNext(order)))
        ).then();
    }

    @Override
    public Flux<OrderDto> getOrders() {
        return this.sink.asFlux();
    }

    private Mono<Void> loadHistory() {
        return redisList.readAll() // Mono<List<OrderDto>>
                .flatMapMany(Flux::fromIterable)
                .doOnNext(sink::tryEmitNext)
                .then();
    }

    @PostConstruct
    public void init() { //с sinks надо работать только тогда, когда бин полностью сформировался
        loadHistory().subscribe();
    }
}
