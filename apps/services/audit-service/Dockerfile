FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/audit-service-*.jar app.jar

EXPOSE 8091

ENTRYPOINT ["java", "-jar", "app.jar"]
