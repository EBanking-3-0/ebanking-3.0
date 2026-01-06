package com.ebanking.payment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.ebanking.payment.client.AccountServiceClient;
import com.ebanking.payment.client.AuthServiceClient;
import com.ebanking.payment.client.dto.*;
import com.ebanking.payment.dto.response.PaymentResult;
import com.ebanking.payment.entity.Payment;
import com.ebanking.payment.entity.PaymentStatus;
import com.ebanking.payment.entity.PaymentType;
import com.ebanking.payment.repository.PaymentRepository;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PaymentSagaOrchestratorTest {

  @Mock private PaymentRepository paymentRepository;
  @Mock private AccountServiceClient accountClient;
  @Mock private AuthServiceClient authClient;
  @Mock private PaymentEventProducer eventProducer;
  @Mock private PaymentStateMachine stateMachine;
  @Mock private FraudDetectionService fraudDetection;
  @Mock private PaymentLimitService limitService;

  @InjectMocks private PaymentSagaOrchestrator sagaOrchestrator;

  private Payment payment;

  @BeforeEach
  void setUp() {
    payment =
        Payment.builder()
            .id(1L)
            .transactionId(UUID.randomUUID().toString())
            .paymentType(PaymentType.INTERNAL_TRANSFER)
            .status(PaymentStatus.CREATED)
            .fromAccountId(1L)
            .toAccountId(2L)
            .amount(new BigDecimal("100.00"))
            .currency("EUR")
            .userId(1L)
            .build();
  }

  @Test
  void testExecuteInternalPaymentSuccess() {
    // Mock account response for validation
    AccountResponse accountResponse =
        AccountResponse.builder()
            .id(1L)
            .balance(new BigDecimal("1000.00"))
            .status("ACTIVE")
            .build();
    when(accountClient.getAccount(eq(1L))).thenReturn(accountResponse);

    // Mock fraud detection
    when(fraudDetection.checkFraud(any(Payment.class)))
        .thenReturn(FraudDetectionService.FraudCheckResult.allowed());

    // Mock debit and credit
    when(accountClient.debit(eq(1L), any(DebitRequest.class)))
        .thenReturn(DebitResponse.builder().transactionId("DEBIT123").build());
    when(accountClient.credit(eq(2L), any(CreditRequest.class)))
        .thenReturn(CreditResponse.builder().transactionId("CREDIT123").build());

    // Mock repository save to return the saved payment
    when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArguments()[0]);

    PaymentResult result = sagaOrchestrator.executePayment(payment);

    assertTrue(result.isSuccess());
    assertNotNull(result.getPayment());
    // Verify that the payment was saved (status will be updated during execution)
    verify(paymentRepository, atLeastOnce()).save(any(Payment.class));
    verify(accountClient, times(1)).debit(eq(1L), any(DebitRequest.class));
    verify(accountClient, times(1)).credit(eq(2L), any(CreditRequest.class));
  }

  @Test
  void testExecutePaymentWithReservationFailure() {
    // Mock account response for validation
    AccountResponse accountResponse =
        AccountResponse.builder()
            .id(1L)
            .balance(new BigDecimal("1000.00"))
            .status("ACTIVE")
            .build();
    when(accountClient.getAccount(eq(1L))).thenReturn(accountResponse);

    // Mock fraud detection
    when(fraudDetection.checkFraud(any(Payment.class)))
        .thenReturn(FraudDetectionService.FraudCheckResult.allowed());

    // Mock debit to throw exception
    when(accountClient.debit(eq(1L), any(DebitRequest.class)))
        .thenThrow(new RuntimeException("Balance low"));

    // Mock repository save
    when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArguments()[0]);

    PaymentResult result = sagaOrchestrator.executePayment(payment);

    assertFalse(result.isSuccess());
    verify(accountClient, never()).credit(any(), any());
  }
}
