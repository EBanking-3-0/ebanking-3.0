FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/user-service-*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]
