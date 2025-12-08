FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/ai-assistant-service-*.jar app.jar

EXPOSE 8090

ENTRYPOINT ["java", "-jar", "app.jar"]
