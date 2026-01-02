package com.ebanking.notification.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for file-based notification templates and retry logic.
 * Templates are resolved from classpath:/templates/{basePath}
 */
@Configuration
@ConfigurationProperties(prefix = "notification.template")
@Data
public class TemplateConfig {

  private String basePath;
  private String defaultLocale;
  private boolean cacheEnabled;

  // Retry configuration
  private int maxRetries;
  private long retryDelayMillis;
  private boolean retryEnabled;
}
