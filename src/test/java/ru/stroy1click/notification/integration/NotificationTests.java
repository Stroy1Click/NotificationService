package ru.stroy1click.notification.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.stroy1click.notification.dto.OrderDto;
import ru.stroy1click.notification.dto.OrderItemDto;
import ru.stroy1click.notification.dto.OrderStatus;
import ru.stroy1click.notification.service.NotificationService;

import java.time.LocalDateTime;
import java.util.List;

@Import({TestcontainersConfiguration.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class NotificationTests {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private NotificationService notificationService;

    private OrderDto orderDto;

    @BeforeEach
    void setUp() {
        this.orderDto = OrderDto.builder()
                .id(1L)
                .notes("Test notes")
                .orderItems(List.of(new OrderItemDto(1L, 5, 100)))
                .orderStatus(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .userId(777L)
                .build();
    }

    @Test
    public void sendNotification_ShouldReturnVoid_WhenValidData(){
        this.webTestClient.post()
                .uri("/api/v1/notifications")
                .bodyValue(this.orderDto)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(Void.class);
    }

    @Test
    public void sendNotification_ShouldReturnValidationException_WhenNotValidQuantity(){
        Mono<OrderDto> notValidOrderDto = Mono.just(OrderDto.builder()
                .id(1L)
                .notes("Test notes")
                .orderItems(List.of(new OrderItemDto(1L, 5, -100)))
                .orderStatus(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .userId(777L)
                .build());
        this.webTestClient.post()
                .uri("/api/v1/notifications")
                .body(notValidOrderDto, OrderDto.class)
                .exchange()
                .expectStatus().is4xxClientError()
                .expectBody()
                .jsonPath("$.detail").isEqualTo("OrderItems должны валидными: id, количество, product id не могут быть пустыми и должны быть больше нуля");
    }

    @Test
    public void getNotifications_ShouldReturnNotifications(){
        this.notificationService.send(Mono.just(new OrderDto())).block();
        this.notificationService.send(Mono.just(new OrderDto())).block();
        this.notificationService.send(Mono.just(new OrderDto())).block();

        this.webTestClient.get()
                .uri("/api/v1/notifications")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .returnResult(OrderDto.class)
                .getResponseBody()
                .take(3)
                .collectList()
                .as(StepVerifier::create)
                .assertNext(i -> {
                    Assertions.assertEquals(3, i.size());
                })
                .expectComplete()
                .verify();
    }

}
