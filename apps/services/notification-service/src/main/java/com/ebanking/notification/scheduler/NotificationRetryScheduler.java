package com.ebanking.notification.scheduler;

import com.ebanking.notification.entity.Notification;
import com.ebanking.notification.service.NotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler for retrying failed notifications. Runs periodically to find failed notifications and
 * retry sending them.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
    prefix = "notification.template",
    name = "retry-enabled",
    havingValue = "true",
    matchIfMissing = true)
public class NotificationRetryScheduler {

  private final NotificationService notificationService;

  @Value("${notification.template.max-retries:3}")
  private int maxRetries;

  @Value("${notification.template.retry-hours-back:24}")
  private int hoursBack;

  @Value("${notification.cleanup.days-old:90}")
  private int daysOld;

  /** Retry failed notifications. Runs every 15 minutes. */
  @Scheduled(fixedDelayString = "${notification.retry.interval:900000}") // 15 minutes
  public void retryFailedNotifications() {
    log.info("Starting failed notification retry job");

    try {
      List<Notification> failedNotifications =
          notificationService.getFailedNotificationsForRetry(maxRetries, hoursBack);

      if (failedNotifications.isEmpty()) {
        log.info("No failed notifications to retry");
        return;
      }

      log.info("Found {} failed notifications to retry", failedNotifications.size());

      int successCount = 0;
      int failureCount = 0;

      for (Notification notification : failedNotifications) {
        try {
          notificationService.retryNotification(notification.getId());
          successCount++;
        } catch (Exception e) {
          log.error("Failed to retry notification: {}", notification.getId(), e);
          failureCount++;
        }
      }

      log.info(
          "Completed failed notification retry job. Success: {}, Failed: {}",
          successCount,
          failureCount);

    } catch (Exception e) {
      log.error("Error in failed notification retry job", e);
    }
  }

  /** Clean up old notifications. Runs daily at midnight. */
  @Scheduled(cron = "${notification.cleanup.cron:0 0 0 * * ?}")
  public void cleanupOldNotifications() {
    log.info("Starting old notification cleanup job");

    try {
      int deletedCount = notificationService.deleteOldNotifications(daysOld);

      log.info("Deleted {} old notifications (older than {} days)", deletedCount, daysOld);

    } catch (Exception e) {
      log.error("Error in old notification cleanup job", e);
    }
  }
}
