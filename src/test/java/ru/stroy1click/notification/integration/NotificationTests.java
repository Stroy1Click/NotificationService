package ru.stroy1click.notification.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.stroy1click.notification.dto.OrderDto;
import ru.stroy1click.notification.model.OrderStatus;

import java.time.LocalDateTime;

@Import({TestcontainersConfiguration.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class NotificationTests {

    @Autowired
    private WebTestClient webTestClient;

    private OrderDto orderDto;

    @BeforeEach
    void setUp() {
        this.orderDto = OrderDto.builder()
                .id(1L)
                .notes("Test notes")
                .quantity(5)
                .orderStatus(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .productId(100)
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
                .quantity(-5)
                .orderStatus(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .productId(100)
                .userId(777L)
                .build());
        this.webTestClient.post()
                .uri("/api/v1/notifications")
                .body(notValidOrderDto, OrderDto.class)
                .exchange()
                .expectStatus().is4xxClientError()
                .expectBody()
                .jsonPath("$.detail").isEqualTo("Количество единиц товара не может быть пустым или быть меньше нуля");
    }

    @Test
    public void getNotifications_ShouldReturnNotifications(){
        this.webTestClient.get()
                .uri("/api/v1/notifications")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM);
    }

}
