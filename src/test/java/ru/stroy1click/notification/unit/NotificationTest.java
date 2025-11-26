package ru.stroy1click.notification.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.redisson.api.*;
import org.redisson.api.listener.MessageListener;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.stroy1click.notification.dto.OrderDto;
import ru.stroy1click.notification.model.OrderStatus;
import ru.stroy1click.notification.service.impl.NotificationServiceImpl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationTest {

    @Mock
    private RedissonReactiveClient redissonReactiveClient;

    @Mock
    private RListReactive redisList;

    @Mock
    private RTopicReactive redisTopic;

    private NotificationServiceImpl service;

    private Mono<OrderDto> orderDto;

    @BeforeEach
    void setUp() {
        this.orderDto = Mono.just(OrderDto.builder()
                .id(1L)
                .notes("notes")
                .quantity(5)
                .orderStatus(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .productId(100)
                .userId(777L)
                .build());

        when(this.redissonReactiveClient.getList("notifications:list"))
                .thenReturn(this.redisList);

        when(this.redissonReactiveClient.getTopic("notifications:topic"))
                .thenReturn(this.redisTopic);

        when(this.redisTopic.getMessages(OrderDto.class))
                .thenReturn(Flux.never());

        when(this.redisList.expire(any(Duration.class))).thenReturn(Mono.empty());

        this.service = new NotificationServiceImpl(this.redissonReactiveClient);
    }

    @Test
    void send_ShouldAddOrderToRedisListAndPublishToTopic_WhenCalled() {
        // Моки реактивных методов
        when(this.redisList.add(this.orderDto)).thenReturn(Mono.just(true));
        when(this.redisTopic.publish(this.orderDto)).thenReturn(Mono.just(1L));

        StepVerifier.create(this.service.send(this.orderDto))
                .verifyComplete();

        verify(this.redisList, times(1)).add(this.orderDto);
        verify(this.redisTopic, times(1)).publish(this.orderDto);
    }

    @Test
    void loadHistory_ShouldEmitOrdersIntoSink_WhenHistoryExists() {
        OrderDto o1 = OrderDto.builder()
                .id(2L)
                .notes("more")
                .quantity(3)
                .orderStatus(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .productId(200)
                .userId(888L)
                .build();
        OrderDto o2 = OrderDto.builder()
                .id(2L)
                .notes("more")
                .quantity(3)
                .orderStatus(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .productId(200)
                .userId(888L)
                .build();

        when(this.redisList.readAll()).thenReturn(Mono.just(List.of(o1, o2)));

        this.service.init(); // loadHistory() вызывается в init()

        StepVerifier.create(this.service.getOrders().take(2))
                .expectNext(o1)
                .expectNext(o2)
                .verifyComplete();
    }
}
