package com.ebanking.notification.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.ebanking.notification.config.TemplateConfig;
import com.ebanking.notification.dto.SendNotificationRequest;
import com.ebanking.notification.entity.Notification;
import com.ebanking.notification.repository.NotificationPreferenceRepository;
import com.ebanking.notification.repository.NotificationRepository;
import com.ebanking.notification.repository.NotificationTemplateRepository;
import com.ebanking.shared.kafka.producer.TypedEventProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for NotificationService */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock private NotificationRepository notificationRepository;

  @Mock private NotificationTemplateRepository templateRepository;

  @Mock private NotificationPreferenceRepository preferenceRepository;

  @Mock private EmailService emailService;

  @Mock private SmsService smsService;

  @Mock private TemplateService templateService;

  @Mock private TypedEventProducer eventProducer;

  @Mock private TemplateConfig templateConfig;

  private NotificationService notificationService;

  @BeforeEach
  void setUp() {
    notificationService =
        new NotificationService(
            notificationRepository,
            templateRepository,
            preferenceRepository,
            emailService,
            smsService,
            templateService,
            eventProducer,
            templateConfig);
  }

  @Test
  void testSendSimpleEmailNotification() {
    // Given
    SendNotificationRequest request =
        SendNotificationRequest.builder()
            .userId(1L)
            .recipient("test@example.com")
            .notificationType(Notification.NotificationType.GENERIC)
            .channel(Notification.NotificationChannel.EMAIL)
            .subject("Test Subject")
            .content("Test Content")
            .build();

    Notification savedNotification =
        Notification.builder()
            .id(1L)
            .userId(1L)
            .recipient("test@example.com")
            .status(Notification.NotificationStatus.PENDING)
            .build();

    when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);
    when(preferenceRepository.findByUserIdAndNotificationType(any(), any()))
        .thenReturn(java.util.Optional.empty());

    // When
    Notification result = notificationService.sendNotification(request);

    // Then
    assertNotNull(result);
    assertEquals(1L, result.getUserId());
    verify(emailService).sendHtmlEmail("test@example.com", "Test Subject", "Test Content");
    verify(notificationRepository, times(2)).save(any(Notification.class));
  }

  @Test
  void testSendSmsNotification() {
    // Given
    SendNotificationRequest request =
        SendNotificationRequest.builder()
            .userId(1L)
            .recipient("+1234567890")
            .notificationType(Notification.NotificationType.GENERIC)
            .channel(Notification.NotificationChannel.SMS)
            .content("Test SMS Content")
            .build();

    Notification savedNotification =
        Notification.builder()
            .id(1L)
            .userId(1L)
            .recipient("+1234567890")
            .status(Notification.NotificationStatus.PENDING)
            .build();

    when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);
    when(preferenceRepository.findByUserIdAndNotificationType(any(), any()))
        .thenReturn(java.util.Optional.empty());

    // When
    Notification result = notificationService.sendNotification(request);

    // Then
    assertNotNull(result);
    assertEquals(1L, result.getUserId());
    verify(smsService).sendSms("+1234567890", "Test SMS Content");
    verify(notificationRepository, times(2)).save(any(Notification.class));
  }

  @Test
  void testRetryFailedNotifications() {
    // Given
    when(templateConfig.isRetryEnabled()).thenReturn(true);
    when(templateConfig.getMaxRetries()).thenReturn(3);
    when(templateConfig.getRetryDelayMillis()).thenReturn(5000L);

    Notification failedNotification =
        Notification.builder()
            .id(1L)
            .userId(1L)
            .recipient("test@example.com")
            .status(Notification.NotificationStatus.FAILED)
            .retryCount(1)
            .updatedAt(java.time.LocalDateTime.now().minusMinutes(10))
            .build();

    when(notificationRepository.findFailedNotificationsForRetry(3))
        .thenReturn(java.util.List.of(failedNotification));

    // When
    notificationService.retryFailedNotifications();

    // Then
    verify(notificationRepository).findFailedNotificationsForRetry(3);
  }

  @Test
  void testGetNotificationHistory() {
    // Given
    Long userId = 1L;
    when(notificationRepository.findByUserIdOrderByCreatedAtDesc(userId))
        .thenReturn(java.util.List.of(new Notification()));

    // When
    var result = notificationService.getNotificationHistory(userId);

    // Then
    assertNotNull(result);
    verify(notificationRepository).findByUserIdOrderByCreatedAtDesc(userId);
  }
}
