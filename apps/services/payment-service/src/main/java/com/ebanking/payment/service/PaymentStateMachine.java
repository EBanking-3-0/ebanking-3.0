package com.ebanking.payment.service;

import com.ebanking.payment.entity.Payment;
import com.ebanking.payment.entity.PaymentStatus;
import com.ebanking.payment.exception.InvalidStateTransitionException;
import com.ebanking.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentStateMachine {

  private final PaymentRepository paymentRepository;

  public void transition(Payment payment, PaymentStatus newStatus) {
    PaymentStatus oldStatus = payment.getStatus();

    if (!isValidTransition(oldStatus, newStatus)) {
      throw new InvalidStateTransitionException(
          "Invalid transition from " + oldStatus + " to " + newStatus);
    }

    log.info("Payment {} transitioning from {} to {}", payment.getId(), oldStatus, newStatus);

    payment.setStatus(newStatus);
    paymentRepository.save(payment);
  }

  private boolean isValidTransition(PaymentStatus from, PaymentStatus to) {
    if (from == null && to == PaymentStatus.CREATED) {
      return true;
    }

    return switch (from) {
      case CREATED ->
          to == PaymentStatus.VALIDATED
              || to == PaymentStatus.REJECTED
              || to == PaymentStatus.CANCELLED;
      case VALIDATED ->
          to == PaymentStatus.AUTHORIZED
              || to == PaymentStatus.REJECTED
              || to == PaymentStatus.FAILED;
      case AUTHORIZED ->
          to == PaymentStatus.RESERVED
              || to == PaymentStatus.SENT
              || to == PaymentStatus.REJECTED
              || to == PaymentStatus.FAILED
              || to == PaymentStatus.COMPLETED; // For simple/simulated flows
      case RESERVED ->
          to == PaymentStatus.SENT || to == PaymentStatus.FAILED || to == PaymentStatus.COMPENSATED;
      case SENT ->
          to == PaymentStatus.SETTLED
              || to == PaymentStatus.FAILED
              || to == PaymentStatus.COMPENSATED
              || to == PaymentStatus.COMPLETED; // For synchronous/internal flows
      case SETTLED -> to == PaymentStatus.COMPLETED;
      default -> false;
    };
  }
}
