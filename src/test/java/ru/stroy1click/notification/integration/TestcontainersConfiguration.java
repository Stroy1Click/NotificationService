package ru.stroy1click.notification.integration;

import jakarta.annotation.PreDestroy;
import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.GenericContainer;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    private static final GenericContainer<?> REDIS;

    static {
        REDIS = new GenericContainer<>("redis:6.2")
                .withExposedPorts(6379);

        REDIS.start();

        System.setProperty("redisson.host", REDIS.getHost());
        System.setProperty("redisson.port", REDIS.getMappedPort(6379).toString());
    }

    @PreDestroy
    public void cleanup() {
        REDIS.stop();
    }

}
