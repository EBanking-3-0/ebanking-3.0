package com.ebanking.payment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.ebanking.payment.client.AccountServiceClient;
import com.ebanking.payment.client.dto.CreditRequest;
import com.ebanking.payment.client.dto.CreditResponse;
import com.ebanking.payment.client.dto.DebitRequest;
import com.ebanking.payment.client.dto.DebitResponse;
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
  @Mock private PaymentEventProducer eventProducer;

  @InjectMocks private PaymentSagaOrchestrator sagaOrchestrator;

  private Payment payment;

  @BeforeEach
  void setUp() {
    payment =
        Payment.builder()
            .id(1L)
            .transactionId(UUID.randomUUID().toString())
            .paymentType(PaymentType.INTERNAL_TRANSFER)
            .status(PaymentStatus.AUTHORIZED)
            .fromAccountId(1L)
            .toAccountId(2L)
            .amount(new BigDecimal("100.00"))
            .currency("EUR")
            .userId(1L)
            .build();
  }

  @Test
  void testExecuteInternalPaymentSuccess() {
    when(accountClient.debit(eq(1L), any(DebitRequest.class)))
        .thenReturn(DebitResponse.builder().transactionId("DEBIT123").build());
    when(accountClient.credit(eq(2L), any(CreditRequest.class)))
        .thenReturn(CreditResponse.builder().transactionId("CREDIT123").build());

    PaymentResult result = sagaOrchestrator.executePayment(payment);

    assertTrue(result.isSuccess());
    assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
    verify(accountClient, times(1)).debit(eq(1L), any(DebitRequest.class));
    verify(accountClient, times(1)).credit(eq(2L), any(CreditRequest.class));
  }

  @Test
  void testExecutePaymentWithReservationFailure() {
    when(accountClient.debit(eq(1L), any(DebitRequest.class)))
        .thenThrow(new RuntimeException("Balance low"));

    PaymentResult result = sagaOrchestrator.executePayment(payment);

    assertFalse(result.isSuccess());
    assertEquals(PaymentStatus.FAILED, payment.getStatus());
    verify(accountClient, never()).credit(any(), any());
  }
}
