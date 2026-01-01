package com.ebanking.payment.client.dto;

import java.math.BigDecimal;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstantTransferRequest {
  private String fromIban;
  private String toIban;
  private BigDecimal amount;
  private String currency;
  private String beneficiaryName;
  private String description;
  private String transactionId;
  private String idempotencyKey;
}
