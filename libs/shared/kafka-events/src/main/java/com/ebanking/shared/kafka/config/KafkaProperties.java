package com.ebanking.shared.kafka.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Kafka. Can be configured via application.yml:
 *
 * <p>spring: kafka: bootstrap-servers: localhost:9092 producer: key-serializer:
 * org.apache.kafka.common.serialization.StringSerializer value-serializer:
 * org.springframework.kafka.support.serializer.JsonSerializer consumer: group-id:
 * ${spring.application.name} key-deserializer:
 * org.apache.kafka.common.serialization.StringDeserializer value-deserializer:
 * org.springframework.kafka.support.serializer.JsonDeserializer
 */
@Data
@ConfigurationProperties(prefix = "spring.kafka")
public class KafkaProperties {

  private String bootstrapServers = "localhost:9092";

  private Producer producer = new Producer();
  private Consumer consumer = new Consumer();

  @Data
  public static class Producer {
    private String keySerializer = "org.apache.kafka.common.serialization.StringSerializer";
    private String valueSerializer = "org.springframework.kafka.support.serializer.JsonSerializer";
    private Integer retries = 3;
    private String acks = "all";
    private Integer batchSize = 16384;
    private Integer lingerMs = 5;
  }

  @Data
  public static class Consumer {
    private String groupId;
    private String keyDeserializer = "org.apache.kafka.common.serialization.StringDeserializer";
    private String valueDeserializer =
        "org.springframework.kafka.support.serializer.JsonDeserializer";
    private String autoOffsetReset = "earliest";
    private Boolean enableAutoCommit = false;
    private Integer maxPollRecords = 500;
  }
}
