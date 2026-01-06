package com.ebanking.notification.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Payment data transfer object for enrichment in notifications. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {

  private Long id;
  private Long userId;
  private BigDecimal amount;
  private String currency;
  private String reason;
  private String errorCode;
  private String status;
  private String description;
}
