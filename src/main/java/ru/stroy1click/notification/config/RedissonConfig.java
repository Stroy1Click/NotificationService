package ru.stroy1click.notification.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Value("${redisson.host}")
    private String host;

    @Value("${redisson.port}")
    private Integer port;

    @Bean(destroyMethod = "shutdown")
    public RedissonReactiveClient redissonClient() {
        Config config = new Config();

        config.useSingleServer()
                .setAddress("redis://%s:%d".formatted(this.host, this.port))
                .setDatabase(0);

        return Redisson.create(config).reactive();
    }
}
