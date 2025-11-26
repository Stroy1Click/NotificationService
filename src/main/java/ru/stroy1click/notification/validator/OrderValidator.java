package ru.stroy1click.notification.validator;

import reactor.core.publisher.Mono;
import ru.stroy1click.notification.dto.OrderDto;

import java.util.function.UnaryOperator;

public interface OrderValidator {

    UnaryOperator<Mono<OrderDto>> validate();
}
