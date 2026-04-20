FROM eclipse-temurin:21-jre-alpine
LABEL authors="egorm"

WORKDIR /app
COPY target/notification-service-0.0.1-SNAPSHOT.jar /app/notification.jar
EXPOSE 2020
ENTRYPOINT ["java", "-jar", "notification.jar"]