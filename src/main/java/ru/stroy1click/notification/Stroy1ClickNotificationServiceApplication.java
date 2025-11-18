package ru.stroy1click.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class Stroy1ClickNotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(Stroy1ClickNotificationServiceApplication.class, args);
    }

}
