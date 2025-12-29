package com.ebanking.notification.scheduler;

import com.ebanking.notification.config.TemplateConfig;
import com.ebanking.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler for retrying failed notifications based on TemplateConfig settings.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "notification.template.retry-enabled", havingValue = "true")
public class NotificationRetryScheduler {

    private final NotificationService notificationService;
    private final TemplateConfig templateConfig;

    /**
     * Scheduled task to retry failed notifications.
     * Runs based on the retry delay configuration in milliseconds.
     */
    @Scheduled(fixedDelayString = "${notification.template.retry-delay-millis:300000}", initialDelayString = "60000")
    public void retryFailedNotifications() {
        if (!templateConfig.isRetryEnabled()) {
            log.debug("Retry mechanism is disabled");
            return;
        }

        try {
            log.debug("Running notification retry scheduler - Max retries: {}, Delay: {}ms", 
                templateConfig.getMaxRetries(), templateConfig.getRetryDelayMillis());
            
            notificationService.retryFailedNotifications();
            
        } catch (Exception e) {
            log.error("Error occurred during notification retry process", e);
        }
    }
}