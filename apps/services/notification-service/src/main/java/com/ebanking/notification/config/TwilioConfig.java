package com.ebanking.notification.config;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Twilio SMS configuration for sending SMS notifications.
 */
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "notification.twilio")
@Data
public class TwilioConfig {

    private String accountSid;
    private String authToken;
    private String phoneNumber;
    private boolean enabled;

    @PostConstruct
    public void init() {
        if (enabled && accountSid != null && authToken != null) {
            try {
                Twilio.init(accountSid, authToken);
                log.info("Twilio SMS service initialized successfully");
            } catch (Exception e) {
                log.error("Failed to initialize Twilio: {}", e.getMessage());
                enabled = false;
            }
        } else {
            log.info("Twilio SMS service is disabled or not configured");
        }
    }
}
