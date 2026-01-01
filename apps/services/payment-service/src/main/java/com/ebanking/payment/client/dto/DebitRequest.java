package com.ebanking.payment.client.dto;

import java.math.BigDecimal;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebitRequest {
  private BigDecimal amount;
  private String transactionId;
  private String idempotencyKey;
  private String description;
}
