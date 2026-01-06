package com.ebanking.payment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.ebanking.payment.dto.request.PaymentRequest;
import com.ebanking.payment.dto.response.PaymentResult;
import com.ebanking.payment.entity.Payment;
import com.ebanking.payment.entity.PaymentStatus;
import com.ebanking.payment.entity.PaymentType;
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
  @Mock private InternalTransferService internalTransferService;
  @Mock private SepaTransferService sepaTransferService;
  @Mock private InstantTransferService instantTransferService;
  @Mock private MobileRechargeService mobileRechargeService;
  @Mock private PaymentSagaOrchestrator sagaOrchestrator;

  @InjectMocks private PaymentService paymentService;

  @Test
  void testInitiatePaymentInternalTransfer() {
    PaymentRequest request =
        PaymentRequest.builder()
            .fromAccountId(1L)
            .toAccountId(2L)
            .amount(new BigDecimal("100.00"))
            .currency("EUR")
            .type("INTERNAL_TRANSFER")
            .idempotencyKey("unique-key")
            .build();

    Payment payment = Payment.builder().id(1L).status(PaymentStatus.COMPLETED).build();
    PaymentResult expectedResult = PaymentResult.success(payment);

    when(internalTransferService.executeInternalTransfer(request, "user-123"))
        .thenReturn(expectedResult);

    PaymentResult result = paymentService.initiatePayment(request, "user-123");

    assertTrue(result.isSuccess());
    verify(internalTransferService, times(1)).executeInternalTransfer(request, "user-123");
  }

  @Test
  void testAuthorizePaymentSuccess() {
    Payment payment =
        Payment.builder()
            .id(1L)
            .userId("user-123")
            .amount(new BigDecimal("500.00"))
            .paymentType(PaymentType.INTERNAL_TRANSFER)
            .fromAccountId(1L)
            .toAccountId(2L)
            .currency("EUR")
            .status(PaymentStatus.CREATED)
            .build();

    when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
    when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArguments()[0]);

    Payment paymentCompleted = Payment.builder().id(1L).status(PaymentStatus.COMPLETED).build();
    PaymentResult expectedResult = PaymentResult.success(paymentCompleted);
    when(internalTransferService.executeInternalTransfer(any(PaymentRequest.class), eq("user-123")))
        .thenReturn(expectedResult);

    PaymentResult result = paymentService.authorizePayment(1L, "123456", "user-123");

    assertTrue(result.isSuccess());
    verify(internalTransferService, times(1)).executeInternalTransfer(any(), eq("user-123"));
  }
}
