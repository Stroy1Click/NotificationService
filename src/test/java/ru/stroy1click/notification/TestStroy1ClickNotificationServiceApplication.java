package ru.stroy1click.notification;

import org.springframework.boot.SpringApplication;
import ru.stroy1click.notification.config.TestcontainersConfiguration;

public class TestStroy1ClickNotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(Stroy1ClickNotificationServiceApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}
