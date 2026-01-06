package com.ebanking.legacyadapter.dto;

import java.math.BigDecimal;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SepaTransferRequest {
  private String fromIban;
  private String toIban;
  private BigDecimal amount;
  private String currency;
  private String beneficiaryName;
  private String description;
  private String transactionId;
  private String idempotencyKey;
  private String executionDate;
}
