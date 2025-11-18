package ru.stroy1click.notification.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.stroy1click.notification.dto.OrderDto;

public interface NotificationService {

    Mono<Void> send(OrderDto orderDto);

    Flux<OrderDto> getOrders();
}
