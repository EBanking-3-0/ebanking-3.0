package com.ebanking.graphql.model;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
  private Long paymentId;
  private String transactionId;
  private String status;
  private String paymentType;
  private BigDecimal amount;
  private String currency;
  private BigDecimal fees;
  private String reference;
  private String uetr;
  private String message;
  private String createdAt;
  private String estimatedCompletionDate;
}
