package com.ebanking.notification.consumer;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.ebanking.notification.client.AccountServiceClient;
import com.ebanking.notification.client.PaymentServiceClient;
import com.ebanking.notification.enums.NotificationType;
import com.ebanking.notification.service.NotificationService;
import com.ebanking.notification.service.PreferenceService;
import com.ebanking.shared.kafka.events.AccountCreatedEvent;
import com.ebanking.shared.kafka.events.AlertTriggeredEvent;
import com.ebanking.shared.kafka.events.AuthLoginEvent;
import com.ebanking.shared.kafka.events.FraudDetectedEvent;
import com.ebanking.shared.kafka.events.PaymentFailedEvent;
import com.ebanking.shared.kafka.events.TransactionCompletedEvent;
import com.ebanking.shared.kafka.events.UserCreatedEvent;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

/** Unit tests for NotificationConsumer. */
@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationConsumer Unit Tests")
class NotificationConsumerTest {

  @Mock private NotificationService notificationService;

  @Mock private PreferenceService preferenceService;

  @Mock private AccountServiceClient accountServiceClient;

  @Mock private PaymentServiceClient paymentServiceClient;

  @Mock private Acknowledgment acknowledgment;

  private NotificationConsumer notificationConsumer;

  @BeforeEach
  void setUp() {
    notificationConsumer =
        new NotificationConsumer(
            notificationService, preferenceService, accountServiceClient, paymentServiceClient);
  }

  @Test
  @DisplayName("Should handle user created event")
  void testHandleUserCreated() {
    // Arrange
    UserCreatedEvent event =
        UserCreatedEvent.builder()
            .userId("1")
            .username("john.doe")
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .build();

    // Act
    notificationConsumer.handleUserCreated(event, "user.created", acknowledgment);

    // Assert
    verify(preferenceService).initializeDefaultPreferences("1");
    verify(notificationService)
        .sendToAllChannels(
            eq("1"),
            eq(NotificationType.WELCOME),
            argThat(map -> map.containsKey("username") && map.containsKey("firstName")));
    verify(acknowledgment).acknowledge();
  }

  @Test
  @DisplayName("Should handle transaction completed event")
  void testHandleTransactionCompleted() {
    // Arrange
    TransactionCompletedEvent event =
        TransactionCompletedEvent.builder()
            .transactionId(100L)
            .fromAccountId(1L)
            .fromAccountNumber("ACC123")
            .toAccountNumber("ACC456")
            .amount(BigDecimal.valueOf(100.00))
            .currency("USD")
            .transactionType("TRANSFER")
            .description("Transfer payment")
            .status("COMPLETED")
            .build();

    when(accountServiceClient.getAccountOwnerId(1L)).thenReturn("1");

    // Act
    notificationConsumer.handleTransactionCompleted(event, "transaction.completed", acknowledgment);

    // Assert
    verify(accountServiceClient).getAccountOwnerId(1L);
    verify(notificationService)
        .sendToAllChannels(
            eq("1"),
            eq(NotificationType.TRANSACTION),
            argThat(map -> map.containsKey("transactionId") && map.containsKey("amount")));
    verify(acknowledgment).acknowledge();
  }

  @Test
  @DisplayName("Should handle fraud detected event")
  void testHandleFraudDetected() {
    // Arrange
    FraudDetectedEvent event =
        FraudDetectedEvent.builder()
            .userId("1")
            .transactionId(100L)
            .accountId(1L)
            .amount(BigDecimal.valueOf(5000.00))
            .currency("USD")
            .fraudType("UNUSUAL_AMOUNT")
            .severity("HIGH")
            .description("Unusual transaction amount detected")
            .build();

    // Act
    notificationConsumer.handleFraudDetected(event, "fraud.detected", acknowledgment);

    // Assert
    verify(notificationService)
        .sendToAllChannels(
            eq("1"),
            eq(NotificationType.FRAUD_ALERT),
            argThat(map -> map.containsKey("fraudType") && map.containsKey("severity")));
    verify(acknowledgment).acknowledge();
  }

  @Test
  @DisplayName("Should handle alert triggered event")
  void testHandleAlertTriggered() {
    // Arrange
    AlertTriggeredEvent event =
        AlertTriggeredEvent.builder()
            .userId("1")
            .accountNumber("ACC123")
            .alertType("LOW_BALANCE")
            .severity("WARNING")
            .message("Account balance is low")
            .threshold(BigDecimal.valueOf(1000.00))
            .currentValue(BigDecimal.valueOf(500.00))
            .build();

    // Act
    notificationConsumer.handleAlertTriggered(event, "alert.triggered", acknowledgment);

    // Assert
    verify(notificationService)
        .sendToAllChannels(
            eq("1"),
            eq(NotificationType.ALERT),
            argThat(map -> map.containsKey("alertType") && map.containsKey("threshold")));
    verify(acknowledgment).acknowledge();
  }

  @Test
  @DisplayName("Should handle account created event")
  void testHandleAccountCreated() {
    // Arrange
    AccountCreatedEvent event =
        AccountCreatedEvent.builder()
            .accountId(1L)
            .userId("1")
            .accountNumber("ACC123")
            .accountType("SAVINGS")
            .currency("USD")
            .initialBalance(BigDecimal.valueOf(1000.00))
            .build();

    // Act
    notificationConsumer.handleAccountCreated(event, "account.created", acknowledgment);

    // Assert
    verify(notificationService)
        .sendToAllChannels(
            eq("1"),
            eq(NotificationType.ACCOUNT_CREATED),
            argThat(map -> map.containsKey("accountNumber") && map.containsKey("accountType")));
    verify(acknowledgment).acknowledge();
  }

  @Test
  @DisplayName("Should handle payment failed event")
  void testHandlePaymentFailed() {
    // Arrange
    PaymentFailedEvent event =
        PaymentFailedEvent.builder()
            .transactionId(100L)
            .userId("1")
            .accountId(1L)
            .accountNumber("ACC123")
            .amount(BigDecimal.valueOf(500.00))
            .currency("USD")
            .failureReason("Insufficient funds")
            .errorCode("INSUFFICIENT_FUNDS")
            .build();

    // Act
    notificationConsumer.handlePaymentFailed(event, "payment.failed", acknowledgment);

    // Assert
    verify(notificationService)
        .sendToAllChannels(
            eq("1"),
            eq(NotificationType.PAYMENT_FAILED),
            argThat(map -> map.containsKey("paymentId") && map.containsKey("reason")));
    verify(acknowledgment).acknowledge();
  }

  @Test
  @DisplayName("Should handle auth login event")
  void testHandleAuthLogin() {
    // Arrange
    AuthLoginEvent event =
        AuthLoginEvent.builder()
            .userId("1")
            .email("john.doe@example.com")
            .ipAddress("192.168.1.1")
            .userAgent("Mozilla/5.0")
            .loginMethod("PASSWORD")
            .success(true)
            .build();

    // Act
    notificationConsumer.handleAuthLogin(event, "auth.login", acknowledgment);

    // Assert
    verify(notificationService)
        .sendToAllChannels(
            eq("1"),
            eq(NotificationType.LOGIN),
            argThat(map -> map.containsKey("ipAddress") && map.containsKey("userAgent")));
    verify(acknowledgment).acknowledge();
  }

  @Test
  @DisplayName("Should handle consumer errors gracefully")
  void testHandleConsumerError() {
    // Arrange
    UserCreatedEvent event =
        UserCreatedEvent.builder()
            .userId("1")
            .username("john.doe")
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .build();

    doThrow(new RuntimeException("Service error"))
        .when(preferenceService)
        .initializeDefaultPreferences(anyString());

    // Act - Should not throw, just log error
    notificationConsumer.handleUserCreated(event, "user.created", acknowledgment);

    // Assert - Acknowledgment not called on error
    verify(acknowledgment, never()).acknowledge();
  }
}
