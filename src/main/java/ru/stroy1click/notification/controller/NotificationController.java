package ru.stroy1click.notification.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.stroy1click.notification.dto.OrderDto;
import ru.stroy1click.notification.service.NotificationService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notification Controller", description = "Взаимодействие с уведомлениями")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<OrderDto> stream() {
        return this.notificationService.getOrders();
    }

    @PostMapping
    public Mono<Void> send(@RequestBody OrderDto orderDto) {
        return notificationService.send(orderDto);
    }
}
