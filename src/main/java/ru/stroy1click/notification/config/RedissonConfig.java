package ru.stroy1click.notification.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Value("${redisson.host:redis}")
    private String host;

    @Value("${redisson.port:6379}")
    private Integer port;

    @Value(value = "${redisson.username}")
    private String username;

    @Value(value = "${redisson.password}")
    private String password;

    @Bean(destroyMethod = "shutdown")
    public RedissonReactiveClient redissonClient() {
        Config config = new Config();

        config.useSingleServer()
                .setAddress("redis://%s:%d".formatted(this.host, this.port))
                .setDatabase(0)
                .setUsername(this.username)
                .setPassword(this.password);

        return Redisson.create(config).reactive();
    }
}
