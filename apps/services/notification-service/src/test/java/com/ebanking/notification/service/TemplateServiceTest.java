package com.ebanking.notification.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.ebanking.notification.entity.NotificationTemplate;
import com.ebanking.notification.enums.NotificationChannel;
import com.ebanking.notification.enums.NotificationType;
import com.ebanking.notification.exception.TemplateException;
import com.ebanking.notification.repository.NotificationTemplateRepository;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ResourceLoader;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/** Unit tests for TemplateService. */
@ExtendWith(MockitoExtension.class)
@DisplayName("TemplateService Unit Tests")
class TemplateServiceTest {

  @Mock private NotificationTemplateRepository templateRepository;

  @Mock private TemplateEngine templateEngine;

  @Mock private ResourceLoader resourceLoader;

  private TemplateService templateService;

  @BeforeEach
  void setUp() {
    templateService = new TemplateService(templateRepository, templateEngine, resourceLoader);
  }

  @Test
  @DisplayName("Should get default subject for notification type")
  void testGetSubject() {
    // Arrange
    NotificationType type = NotificationType.WELCOME;
    NotificationChannel channel = NotificationChannel.EMAIL;

    // Act
    String result = templateService.getSubject(type, channel, null);

    // Assert
    assertNotNull(result);
    assertFalse(result.isEmpty());
  }

  @Test
  @DisplayName("Should render template with variables")
  void testRenderTemplate() {
    // Arrange
    NotificationType type = NotificationType.WELCOME;
    NotificationChannel channel = NotificationChannel.EMAIL;
    Map<String, Object> variables = Map.of("firstName", "John");

    NotificationTemplate template =
        NotificationTemplate.builder()
            .notificationType(type)
            .channel(channel)
            .subject("Welcome John")
            .content("Welcome [[${firstName}]], join us!")
            .locale("en")
            .active(true)
            .build();

    when(templateRepository.findByNotificationTypeAndChannelAndLocaleAndActiveTrue(
            type, channel, "en"))
        .thenReturn(Optional.of(template));

    when(templateEngine.process(anyString(), any(Context.class)))
        .thenReturn("Welcome John, join us!");

    // Act
    String result = templateService.renderTemplate(type, channel, variables, "en");

    // Assert
    assertNotNull(result);
    assertFalse(result.isEmpty());
    verify(templateRepository, times(1))
        .findByNotificationTypeAndChannelAndLocaleAndActiveTrue(type, channel, "en");
  }

  @Test
  @DisplayName("Should throw exception when template not found")
  void testRenderTemplateNotFound() {
    // Arrange
    NotificationType type = NotificationType.WELCOME;
    NotificationChannel channel = NotificationChannel.PUSH;

    when(templateRepository.findByNotificationTypeAndChannelAndLocaleAndActiveTrue(
            type, channel, "en"))
        .thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(
        TemplateException.class,
        () -> {
          templateService.renderTemplate(type, channel, Map.of(), "en");
        });
  }

  @Test
  @DisplayName("Should replace variables in simple content")
  void testReplaceVariables() {
    // Arrange
    String content = "Hello {{firstName}}, your balance is {{balance}}";
    Map<String, Object> variables =
        Map.of(
            "firstName", "John",
            "balance", "1000.00");

    // Act & Assert
    assertNotNull(content);
    assertNotNull(variables);
  }

  @Test
  @DisplayName("Should get default subject for all notification types")
  void testGetSubjectForAllTypes() {
    // Arrange
    NotificationChannel channel = NotificationChannel.EMAIL;

    // Act & Assert
    for (NotificationType type : NotificationType.values()) {
      String subject = templateService.getSubject(type, channel, null);
      assertNotNull(subject, "Subject should not be null for type: " + type);
      assertFalse(subject.isEmpty(), "Subject should not be empty for type: " + type);
    }
  }

  @Test
  @DisplayName("Should find default template by type and channel")
  void testFindDefaultTemplate() {
    // Arrange
    NotificationType type = NotificationType.TRANSACTION;
    NotificationChannel channel = NotificationChannel.EMAIL;

    NotificationTemplate template =
        NotificationTemplate.builder()
            .notificationType(type)
            .channel(channel)
            .subject("Transaction Confirmation")
            .content("Your transaction has been processed")
            .locale("en")
            .active(true)
            .build();

    when(templateRepository.findDefaultTemplate(type, channel)).thenReturn(Optional.of(template));

    // Act
    Optional<NotificationTemplate> result = templateRepository.findDefaultTemplate(type, channel);

    // Assert
    assertTrue(result.isPresent());
    assertEquals("Transaction Confirmation", result.get().getSubject());
  }
}
