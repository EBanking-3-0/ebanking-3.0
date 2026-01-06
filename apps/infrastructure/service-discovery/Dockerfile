FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy JAR from Gradle build output (relative to the context provided in docker compose)
COPY build/libs/service-discovery-*.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "app.jar"]