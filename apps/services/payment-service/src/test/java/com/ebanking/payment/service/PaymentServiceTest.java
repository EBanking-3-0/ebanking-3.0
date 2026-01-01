package com.ebanking.payment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.ebanking.payment.dto.request.PaymentRequest;
import com.ebanking.payment.dto.response.PaymentResult;
import com.ebanking.payment.entity.Payment;
import com.ebanking.payment.repository.PaymentRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

  @Mock private PaymentRepository paymentRepository;
  @Mock private PaymentSagaOrchestrator sagaOrchestrator;
  @Mock private PaymentValidationService validationService;

  @InjectMocks private PaymentService paymentService;

  @Test
  void testInitiatePaymentScaRequired() {
    PaymentRequest request =
        PaymentRequest.builder()
            .fromAccountId(1L)
            .amount(new BigDecimal("500.00")) // > 100 threshold
            .currency("EUR")
            .type("INTERNAL_TRANSFER")
            .idempotencyKey("unique-key")
            .build();

    when(paymentRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
    when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArguments()[0]);

    PaymentResult result = paymentService.initiatePayment(request, 1L);

    assertTrue(result.isSuccess());
    assertEquals("SCA_REQUIRED", result.getMessage());
    verify(sagaOrchestrator, never()).executePayment(any());
  }

  @Test
  void testAuthorizePaymentSuccess() {
    Payment payment = Payment.builder().id(1L).userId(1L).amount(new BigDecimal("500.00")).build();

    when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
    when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
    when(sagaOrchestrator.executePayment(any())).thenReturn(PaymentResult.success(payment));

    PaymentResult result = paymentService.authorizePayment(1L, "123456", 1L);

    assertTrue(result.isSuccess());
    verify(sagaOrchestrator, times(1)).executePayment(payment);
  }
}
