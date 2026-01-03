package com.ebanking.account.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
