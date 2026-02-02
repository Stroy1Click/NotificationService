package ru.stroy1click.notification.validator.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.stroy1click.notification.dto.OrderDto;
import ru.stroy1click.notification.exception.ValidationException;
import ru.stroy1click.notification.dto.OrderStatus;
import ru.stroy1click.notification.validator.OrderValidator;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;


@Component
@RequiredArgsConstructor
public class OrderValidatorImpl implements OrderValidator {

    private final MessageSource messageSource;

    @Override
    public UnaryOperator<Mono<OrderDto>> validate() {
        Map<Predicate<OrderDto>, String> validators = Map.of(
                hasValidId(), "validation.orderdto.id",
                hasValidUserId(), "validation.orderdto.user_id",
                hasValidOrderItems(), "validation.orderdto.order_items",
                hasValidNotes(), "validation.orderdto.notes",
                hasValidOrderStatus(), "validation.orderdto.order_status",
                hasValidCreateAtDate(), "validation.orderdto.created_at",
                hasValidUpdatedAtDate(), "validation.orderdto.updated_at"
        );

        return mono -> mono.flatMap(dto -> {
            Optional<String> errorKey = validators.entrySet().stream()
                    .filter(entry -> !entry.getKey().test(dto))
                    .map(Map.Entry::getValue)
                    .findFirst();

            if (errorKey.isPresent()) {
                String message = this.messageSource.getMessage(errorKey.get(), null, Locale.getDefault());
                return Mono.error(new ValidationException(message));
            } else {
                return Mono.just(dto);
            }
        });
    }

    private Predicate<OrderDto> hasValidId(){
        return dto -> Objects.nonNull(dto.getId()) && dto.getId() > 0;
    }

    private Predicate<OrderDto> hasValidUserId(){
        return dto -> Objects.nonNull(dto.getUserId()) && dto.getUserId() > 0;
    }
    private Predicate<OrderDto> hasValidOrderItems(){
        return dto -> !dto.getOrderItems().stream()
                .filter(Objects::nonNull)
                .filter(orderItemDto -> orderItemDto.getProductId() > 0
                && orderItemDto.getQuantity() > 0)
                .toList().isEmpty() ;
    }
    private Predicate<OrderDto> hasValidNotes(){
        return dto -> Objects.nonNull(dto.getNotes());
    }
    private Predicate<OrderDto> hasValidOrderStatus() {
        return dto -> {
            if (dto.getOrderStatus() == null) return false;
            return Arrays.stream(OrderStatus.values())
                    .anyMatch(status -> status.name().equals(dto.getOrderStatus().toString()));
        };
    }
    private Predicate<OrderDto> hasValidCreateAtDate(){
        return dto -> Objects.nonNull(dto.getCreatedAt())
                && dto.getCreatedAt().isBefore(LocalDateTime.now());
    }

    private Predicate<OrderDto> hasValidUpdatedAtDate(){
        return dto -> Objects.nonNull(dto.getUpdatedAt())
                && dto.getUpdatedAt().isBefore(LocalDateTime.now());
    }
}
