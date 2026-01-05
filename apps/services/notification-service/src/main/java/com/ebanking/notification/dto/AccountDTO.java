package com.ebanking.notification.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Account data transfer object for enrichment in notifications. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {

  private Long id;
  private Long userId;
  private String accountNumber;
  private String accountType;
  private String currency;
  private BigDecimal balance;
  private String status;
}
