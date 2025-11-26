package ru.stroy1click.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.stroy1click.notification.dto.OrderDto;
import ru.stroy1click.notification.service.NotificationService;
import ru.stroy1click.notification.validator.OrderValidator;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notification Controller", description = "Взаимодействие с уведомлениями")
public class NotificationController {

    private final NotificationService notificationService;

    private final OrderValidator orderValidator;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Получить новые заказы в формате SSE")
    public Flux<OrderDto> getNotification() {
        return this.notificationService.getOrders();
    }

    @PostMapping
    @Operation(summary = "Отправить новый заказ")
    public Mono<Void> sendNotification(@RequestBody Mono<OrderDto> orderDto) {
        return orderDto.transform(this.orderValidator.validate())
                .as(this.notificationService::send);
    }
}
