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
public class BalanceResponse {
  private BigDecimal availableBalance;
  private BigDecimal currentBalance;
  private String currency;
  private String status;
}
