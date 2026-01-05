package com.ebanking.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main application class for the Notification Service. Handles sending notifications through
 * multiple channels (Email, SMS, In-App, Push) based on user preferences.
 *
 * <p>Features: - Event-driven notification processing via Kafka - Multiple notification channels
 * with Strategy pattern - User preference management - Template-based content rendering - Automatic
 * retry mechanism for failed notifications - In-app notification persistence
 *
 * @author E-Banking Team
 * @version 1.0
 */
@SpringBootApplication(scanBasePackages = {"com.ebanking.notification", "com.ebanking.shared"})
@EnableDiscoveryClient
@EnableJpaRepositories
public class NotificationServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(NotificationServiceApplication.class, args);
  }
}
