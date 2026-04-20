package ru.stroy1click.notification.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.GenericContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    public static GenericContainer<?> redisContainer() {
        var redis = new GenericContainer<>("redis:6.2").withExposedPorts(6379);
        redis.start();
        return redis;
    }

    @Bean
    public DynamicPropertyRegistrar properties(GenericContainer<?> redisContainer) {
        return (registry) -> {
            String redissonHost = redisContainer.getHost();
            Integer redissonPort =  redisContainer.getMappedPort(6379);

            registry.add("redisson.host", () -> redissonHost);
            registry.add("redisson.port", () -> redissonPort);
            registry.add("spring.data.redis.host", redisContainer::getHost);
            registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379));
        };
    }

}
