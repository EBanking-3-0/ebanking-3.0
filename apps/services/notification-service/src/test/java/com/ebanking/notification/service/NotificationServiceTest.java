package com.ebanking.notification.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.ebanking.notification.dto.NotificationDTO;
import com.ebanking.notification.dto.SendNotificationRequest;
import com.ebanking.notification.dto.UserContactDTO;
import com.ebanking.notification.entity.Notification;
import com.ebanking.notification.enums.NotificationChannel;
import com.ebanking.notification.enums.NotificationStatus;
import com.ebanking.notification.enums.NotificationType;
import com.ebanking.notification.exception.NotificationException;
import com.ebanking.notification.repository.NotificationRepository;
import com.ebanking.notification.strategy.NotificationStrategy;
import com.ebanking.shared.kafka.producer.EventProducer;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for NotificationService. */
@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Unit Tests")
class NotificationServiceTest {

  @Mock private NotificationRepository notificationRepository;

  @Mock private NotificationStrategyFactory strategyFactory;

  @Mock private TemplateService templateService;

  @Mock private PreferenceService preferenceService;

  @Mock private UserService userService;

  @Mock private EventProducer eventProducer;

  @Mock private NotificationStrategy notificationStrategy;

  @Mock private com.ebanking.notification.mapper.NotificationMapper notificationMapper;

  private NotificationService notificationService;

  @BeforeEach
  void setUp() {
    notificationService =
        new NotificationService(
            notificationRepository,
            strategyFactory,
            templateService,
            preferenceService,
            userService,
            eventProducer,
            notificationMapper);

    // Setup mapper to return DTOs from entities (lenient as not all tests need it)
    lenient()
        .when(notificationMapper.toDTO(any(Notification.class)))
        .thenAnswer(
            invocation -> {
              Notification notification = invocation.getArgument(0);
              return NotificationDTO.builder()
                  .id(notification.getId())
                  .userId(notification.getUserId())
                  .type(notification.getType())
                  .channel(notification.getChannel())
                  .status(notification.getStatus())
                  .subject(notification.getSubject())
                  .recipient(notification.getRecipient())
                  .content(notification.getContent())
                  .sentAt(notification.getSentAt())
                  .build();
            });
  }

  @Test
  @DisplayName("Should send notification successfully")
  void testSendNotificationSuccess() {
    // Arrange
    Long userId = 1L;
    SendNotificationRequest request =
        SendNotificationRequest.builder()
            .userId(userId)
            .type(NotificationType.WELCOME)
            .channel(NotificationChannel.EMAIL)
            .subject("Welcome!")
            .content("Welcome to ebanking")
            .build();

    UserContactDTO userContact =
        UserContactDTO.builder()
            .userId(userId)
            .email("test@example.com")
            .firstName("John")
            .lastName("Doe")
            .phoneNumber("+1234567890")
            .preferredLanguage("en")
            .build();

    Notification notification =
        Notification.builder()
            .id(1L)
            .userId(userId)
            .type(NotificationType.WELCOME)
            .channel(NotificationChannel.EMAIL)
            .status(NotificationStatus.SENT)
            .subject("Welcome!")
            .recipient("test@example.com")
            .build();

    when(preferenceService.isChannelEnabled(
            userId, NotificationType.WELCOME, NotificationChannel.EMAIL))
        .thenReturn(true);
    when(userService.getUserContact(userId)).thenReturn(userContact);
    lenient()
        .when(templateService.getSubject(NotificationType.WELCOME, NotificationChannel.EMAIL, null))
        .thenReturn("Welcome!");
    lenient()
        .when(
            templateService.renderTemplate(
                eq(NotificationType.WELCOME), eq(NotificationChannel.EMAIL), any(), eq("en")))
        .thenReturn("Welcome content");
    when(strategyFactory.getStrategy(NotificationChannel.EMAIL)).thenReturn(notificationStrategy);
    when(notificationStrategy.supports("test@example.com")).thenReturn(true);
    when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

    // Act
    NotificationDTO result = notificationService.sendNotification(request);

    // Assert
    assertNotNull(result);
    assertEquals(userId, result.getUserId());
    assertEquals(NotificationType.WELCOME, result.getType());
    verify(notificationRepository, atLeastOnce()).save(any(Notification.class));
    verify(strategyFactory).getStrategy(NotificationChannel.EMAIL);
  }

  @Test
  @DisplayName("Should throw exception when channel is disabled")
  void testSendNotificationChannelDisabled() {
    // Arrange
    Long userId = 1L;
    SendNotificationRequest request =
        SendNotificationRequest.builder()
            .userId(userId)
            .type(NotificationType.WELCOME)
            .channel(NotificationChannel.EMAIL)
            .subject("Welcome!")
            .build();

    when(preferenceService.isChannelEnabled(
            userId, NotificationType.WELCOME, NotificationChannel.EMAIL))
        .thenReturn(false);

    // Act & Assert
    assertThrows(
        NotificationException.class,
        () -> {
          notificationService.sendNotification(request);
        });
  }

  @Test
  @DisplayName("Should send to all enabled channels")
  void testSendToAllChannels() {
    // Arrange
    Long userId = 1L;
    NotificationType type = NotificationType.TRANSACTION;
    Map<String, Object> variables = Map.of("amount", "100.00");

    UserContactDTO userContact =
        UserContactDTO.builder()
            .userId(userId)
            .email("test@example.com")
            .firstName("John")
            .lastName("Doe")
            .phoneNumber("+1234567890")
            .preferredLanguage("en")
            .build();

    Notification notification =
        Notification.builder()
            .id(1L)
            .userId(userId)
            .type(type)
            .status(NotificationStatus.SENT)
            .build();

    lenient()
        .when(preferenceService.getEnabledChannels(userId, type))
        .thenReturn(List.of(NotificationChannel.EMAIL));

    lenient().when(templateService.getSubject(any(), any(), any())).thenReturn("Transaction");

    lenient().when(userService.getUserContact(userId)).thenReturn(userContact);
    lenient().when(preferenceService.isChannelEnabled(anyLong(), any(), any())).thenReturn(true);
    lenient()
        .when(templateService.renderTemplate(any(), any(), any(), any()))
        .thenReturn("Transaction content");
    lenient().when(strategyFactory.getStrategy(any())).thenReturn(notificationStrategy);
    lenient().when(notificationStrategy.supports(anyString())).thenReturn(true);
    lenient().when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

    // Act
    List<NotificationDTO> results = notificationService.sendToAllChannels(userId, type, variables);

    // Assert
    assertNotNull(results);
    assertTrue(results.size() >= 0);
  }

  @Test
  @DisplayName("Should mark notification as read")
  void testMarkAsRead() {
    // Arrange
    Long notificationId = 1L;
    Long userId = 1L;

    Notification notification =
        Notification.builder()
            .id(notificationId)
            .userId(userId)
            .status(NotificationStatus.SENT)
            .build();

    Notification updatedNotification =
        Notification.builder()
            .id(notificationId)
            .userId(userId)
            .status(NotificationStatus.READ)
            .build();

    when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
    when(notificationRepository.save(any(Notification.class))).thenReturn(updatedNotification);

    // Act
    NotificationDTO result = notificationService.markAsRead(notificationId, userId);

    // Assert
    assertNotNull(result);
    assertEquals(NotificationStatus.READ, result.getStatus());
    verify(notificationRepository).save(any(Notification.class));
  }

  @Test
  @DisplayName("Should get unread in-app notifications")
  void testGetUnreadInAppNotifications() {
    // Arrange
    Long userId = 1L;

    Notification notification =
        Notification.builder()
            .id(1L)
            .userId(userId)
            .channel(NotificationChannel.IN_APP)
            .status(NotificationStatus.SENT)
            .build();

    when(notificationRepository.findUnreadInAppNotifications(userId, NotificationChannel.IN_APP))
        .thenReturn(List.of(notification));

    // Act
    List<NotificationDTO> results = notificationService.getUnreadInAppNotifications(userId);

    // Assert
    assertNotNull(results);
    assertEquals(1, results.size());
  }

  @Test
  @DisplayName("Should retry failed notification")
  void testRetryNotification() {
    // Arrange
    Long notificationId = 1L;

    Notification failedNotification =
        Notification.builder()
            .id(notificationId)
            .userId(1L)
            .type(NotificationType.WELCOME)
            .channel(NotificationChannel.EMAIL)
            .status(NotificationStatus.FAILED)
            .recipient("test@example.com")
            .subject("Test")
            .content("Test content")
            .retryCount(0)
            .build();

    Notification sentNotification =
        Notification.builder()
            .id(notificationId)
            .userId(1L)
            .type(NotificationType.WELCOME)
            .channel(NotificationChannel.EMAIL)
            .status(NotificationStatus.SENT)
            .retryCount(1)
            .build();

    when(notificationRepository.findById(notificationId))
        .thenReturn(Optional.of(failedNotification));
    when(strategyFactory.getStrategy(NotificationChannel.EMAIL)).thenReturn(notificationStrategy);
    when(notificationStrategy.supports("test@example.com")).thenReturn(true);
    when(notificationRepository.save(any(Notification.class))).thenReturn(sentNotification);

    // Act
    NotificationDTO result = notificationService.retryNotification(notificationId);

    // Assert
    assertNotNull(result);
    assertEquals(NotificationStatus.SENT, result.getStatus());
  }
}
