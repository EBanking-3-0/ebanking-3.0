package com.ebanking.payment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.ebanking.payment.dto.request.PaymentRequest;
import com.ebanking.payment.dto.response.PaymentResult;
import com.ebanking.payment.entity.Payment;
import com.ebanking.payment.entity.PaymentStatus;
import com.ebanking.payment.entity.PaymentType;
import com.ebanking.payment.repository.PaymentRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PaymentTypeCoverageTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private InternalTransferService internalTransferService;
    @Mock
    private SepaTransferService sepaTransferService;
    @Mock
    private InstantTransferService instantTransferService;
    @Mock
    private MobileRechargeService mobileRechargeService;
    @Mock
    private PaymentSagaOrchestrator sagaOrchestrator;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentRequest createRequest(String type) {
        return PaymentRequest.builder()
                .fromAccountId(1L)
                .toAccountId(2L)
                .amount(new BigDecimal("100.00"))
                .currency("EUR")
                .type(type)
                .idempotencyKey("key-" + type)
                .build();
    }

    private PaymentResult successResult() {
        return PaymentResult.success(Payment.builder().id(123L).status(PaymentStatus.COMPLETED).build());
    }

    @Test
    void testInternalTransferDelegation() {
        PaymentRequest request = createRequest("INTERNAL_TRANSFER");
        when(internalTransferService.executeInternalTransfer(any(), eq(1L))).thenReturn(successResult());

        PaymentResult result = paymentService.initiatePayment(request, 1L);

        assertTrue(result.isSuccess());
        verify(internalTransferService).executeInternalTransfer(request, 1L);
        verifyNoInteractions(sepaTransferService, instantTransferService, mobileRechargeService, sagaOrchestrator);
    }

    @Test
    void testSepaTransferDelegation() {
        PaymentRequest request = createRequest("SEPA_TRANSFER");
        when(sepaTransferService.executeSepaTransfer(any(), eq(1L))).thenReturn(successResult());

        PaymentResult result = paymentService.initiatePayment(request, 1L);

        assertTrue(result.isSuccess());
        verify(sepaTransferService).executeSepaTransfer(request, 1L);
        verifyNoInteractions(internalTransferService, instantTransferService, mobileRechargeService, sagaOrchestrator);
    }

    @Test
    void testInstantTransferDelegation() {
        PaymentRequest request = createRequest("SCT_INSTANT");
        when(instantTransferService.executeInstantTransfer(any(), eq(1L))).thenReturn(successResult());

        PaymentResult result = paymentService.initiatePayment(request, 1L);

        assertTrue(result.isSuccess());
        verify(instantTransferService).executeInstantTransfer(request, 1L);
        verifyNoInteractions(internalTransferService, sepaTransferService, mobileRechargeService, sagaOrchestrator);
    }

    @Test
    void testMobileRechargeDelegation() {
        PaymentRequest request = createRequest("MOBILE_RECHARGE");
        when(mobileRechargeService.executeMobileRecharge(any(), eq(1L))).thenReturn(successResult());

        PaymentResult result = paymentService.initiatePayment(request, 1L);

        assertTrue(result.isSuccess());
        verify(mobileRechargeService).executeMobileRecharge(request, 1L);
        verifyNoInteractions(internalTransferService, sepaTransferService, instantTransferService, sagaOrchestrator);
    }

    @Test
    void testSwiftTransferFallbackToSaga() {
        PaymentRequest request = createRequest("SWIFT_TRANSFER");
        when(sagaOrchestrator.executePayment(any(Payment.class))).thenReturn(successResult());

        PaymentResult result = paymentService.initiatePayment(request, 1L);

        assertTrue(result.isSuccess());
        verify(sagaOrchestrator).executePayment(any(Payment.class));
        verifyNoInteractions(internalTransferService, sepaTransferService, instantTransferService,
                mobileRechargeService);
    }

    @Test
    void testMerchantPaymentFallbackToSaga() {
        PaymentRequest request = createRequest("MERCHANT_PAYMENT");
        when(sagaOrchestrator.executePayment(any(Payment.class))).thenReturn(successResult());

        PaymentResult result = paymentService.initiatePayment(request, 1L);

        assertTrue(result.isSuccess());
        verify(sagaOrchestrator).executePayment(any(Payment.class));
        verifyNoInteractions(internalTransferService, sepaTransferService, instantTransferService,
                mobileRechargeService);
    }
}
