FROM openjdk:21
LABEL authors="egorm"

WORKDIR /app
ADD maven/Stroy1Click-NotificationService-0.0.1-SNAPSHOT.jar /app/notification.jar
EXPOSE 2020
ENTRYPOINT ["java", "-jar", "notification.jar"]