package com.ebanking.payment.event;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentFailedEvent {
  private Long transactionId;
  private Long accountId;
  private String accountNumber;
  private BigDecimal amount;
  private String currency;
  private String failureReason;
  private String errorCode;
  private Instant timestamp;
  private String source;
}
