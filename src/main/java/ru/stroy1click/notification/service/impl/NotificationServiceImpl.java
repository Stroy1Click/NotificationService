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
        this.redisList.expire(Duration.ofDays(2)).subscribe();

        RTopicReactive topic = client.getTopic(TOPIC_KEY);

        topic.getMessages(OrderDto.class)
                .doOnNext(order -> {
                    Sinks.EmitResult result = sink.tryEmitNext(order);
                    if (!result.isSuccess()) log.warn("Не удалось добавить заказ в sink: {}", result);
                })
                .doOnError(e -> log.error("Ошибка получения сообщений из топика", e))
                .retry()
                .subscribe();

        this.redisTopic = topic;
    }

    @Override
    public Mono<Void> send(Mono<OrderDto> orderDto) {
        return orderDto
                .doOnNext(dto -> log.info("send {}", dto))
                .flatMap(order -> this.redisList.add(order)
                        .then(this.redisTopic.publish(order))
                        .then(Mono.fromRunnable(() -> {
                            Sinks.EmitResult result = this.sink.tryEmitNext(order);
                            if (!result.isSuccess()) {
                                log.warn("Не удалось отправить заказ в sink: {}", result);
                            }
                        }))
                )
                .doOnError(e -> log.error("Ошибка отправки заказа ", e))
                .then();
    }

    @Override
    public Flux<OrderDto> getNewOrders() {
        log.info("getNewOrders");
        return this.sink.asFlux();
    }

    public Mono<Void> loadHistory() {
        return this.redisList.readAll() // Mono<List<OrderDto>>
                .flatMapMany(Flux::fromIterable)
                .doOnNext(order -> {
                    Sinks.EmitResult result = sink.tryEmitNext(order);
                    if (!result.isSuccess()) log.warn("Не удалось добавить заказ в sink: {}", result);
                })
                .then();
    }

    @PostConstruct
    public void init() { //с sinks надо работать только тогда, когда бин полностью сформировался
        loadHistory()
                .doOnError(e -> log.error("Ошибка загрузки истории ", e))
                .retry()
                .subscribe();
    }
}
