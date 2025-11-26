package ru.stroy1click.notification.integration;

import org.springframework.boot.SpringApplication;
import ru.stroy1click.notification.Stroy1ClickNotificationServiceApplication;

public class TestStroy1ClickNotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(Stroy1ClickNotificationServiceApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}
