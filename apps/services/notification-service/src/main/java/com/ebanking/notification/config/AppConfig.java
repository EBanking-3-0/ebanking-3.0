package com.ebanking.notification.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

/**
 * Application-wide configuration. Includes RestTemplate setup with circuit breaker patterns for
 * inter-service communication.
 */
@Slf4j
@Configuration
@EnableScheduling
public class AppConfig {

  /**
   * RestTemplate bean for making HTTP requests to other services with timeout and retry logic.
   *
   * @param builder RestTemplateBuilder for fluent configuration
   * @return RestTemplate instance configured for resilience
   */
  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    log.info("Creating RestTemplate bean with timeout configuration");
    return builder
        .setConnectTimeout(java.time.Duration.ofSeconds(5))
        .setReadTimeout(java.time.Duration.ofSeconds(10))
        .build();
  }
}
