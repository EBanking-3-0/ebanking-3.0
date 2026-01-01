package com.ebanking.payment.service;

import com.ebanking.payment.entity.Payment;
import com.ebanking.payment.exception.PaymentNotFoundException;
import com.ebanking.payment.repository.PaymentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentQueryService {

  private final PaymentRepository paymentRepository;

  public Payment getPaymentById(Long id) {
    return paymentRepository
        .findById(id)
        .orElseThrow(() -> new PaymentNotFoundException("Payment not found for ID: " + id));
  }

  public Payment getPaymentByTransactionId(String transactionId) {
    return paymentRepository
        .findByTransactionId(transactionId)
        .orElseThrow(
            () ->
                new PaymentNotFoundException(
                    "Payment not found for Transaction ID: " + transactionId));
  }

  public List<Payment> getPaymentsByUserId(Long userId) {
    return paymentRepository.findByUserId(userId);
  }
}
