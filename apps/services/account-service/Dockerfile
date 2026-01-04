# syntax=docker/dockerfile:1

FROM eclipse-temurin:21-jre AS builder
WORKDIR /builder
ARG JAR_FILE=apps/services/account-service/build/libs/account-service.jar
COPY ${JAR_FILE} app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring
COPY --from=builder /builder/dependencies/ ./
COPY --from=builder /builder/spring-boot-loader/ ./
COPY --from=builder /builder/snapshot-dependencies/ ./
COPY --from=builder /builder/application/ ./
EXPOSE 8084
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]