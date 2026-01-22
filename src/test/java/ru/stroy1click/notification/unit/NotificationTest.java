package ru.stroy1click.notification.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.redisson.api.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.stroy1click.notification.dto.OrderDto;
import ru.stroy1click.notification.dto.OrderItemDto;
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
    private OrderDto testOrder;

    @BeforeEach
    void setUp() {
        this.testOrder = OrderDto.builder()
                .id(1L)
                .notes("notes")
                .orderItems(List.of(new OrderItemDto(null, 5, 100)))
                .orderStatus(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .userId(777L)
                .build();

        when(redissonReactiveClient.getList("notifications:list")).thenReturn(redisList);
        when(redissonReactiveClient.getTopic("notifications:topic")).thenReturn(redisTopic);

        when(redisList.expire(Mockito.<Duration>any())).thenReturn(Mono.empty());
        lenient().when(redisList.readAll()).thenReturn(Mono.just(List.of()));
        when(redisTopic.getMessages(OrderDto.class)).thenReturn(Flux.never());

        this.service = new NotificationServiceImpl(this.redissonReactiveClient);
    }

    @Test
    void send_ShouldAddOrderToRedisListAndPublishToTopic_WhenCalled() {
        when(redisList.add(any(OrderDto.class))).thenReturn(Mono.just(true));
        when(redisTopic.publish(any(OrderDto.class))).thenReturn(Mono.just(1L));

        StepVerifier.create(service.send(Mono.just(testOrder)))
                .verifyComplete();

        verify(redisList, times(1)).add(any(OrderDto.class));
        verify(redisTopic, times(1)).publish(any(OrderDto.class));
    }

    @Test
    void send_ShouldEmitToSink_WhenOrderProcessed() {
        when(redisList.add(any(OrderDto.class))).thenReturn(Mono.just(true));
        when(redisTopic.publish(any(OrderDto.class))).thenReturn(Mono.just(1L));

        StepVerifier.create(service.send(Mono.just(testOrder)).thenMany(service.getNewOrders().next()))
                .expectNext(testOrder)
                .verifyComplete();

        verify(redisList, times(1)).add(any(OrderDto.class));
    }

    @Test
    void loadHistory_ShouldEmitOrdersIntoSink_WhenHistoryExists() {
        OrderDto o1 = OrderDto.builder().id(2L).notes("first").build();
        OrderDto o2 = OrderDto.builder().id(3L).notes("second").build();

        when(redisList.readAll()).thenReturn(Mono.just(List.of(o1, o2)));

        NotificationServiceImpl service = new NotificationServiceImpl(redissonReactiveClient);

        StepVerifier.create(service.loadHistory().thenMany(service.getNewOrders().take(2)))
                .expectNext(o1)
                .expectNext(o2)
                .verifyComplete();
    }

    @Test
    void getOrders_ShouldReturnFlux_WhenSubscribed() {
        when(redisList.add(any(OrderDto.class))).thenReturn(Mono.just(true));
        when(redisTopic.publish(any(OrderDto.class))).thenReturn(Mono.just(1L));

        StepVerifier.create(service.send(Mono.just(testOrder))
                        .thenMany(service.getNewOrders().take(1)))
                .expectNext(testOrder)
                .verifyComplete();
    }
}