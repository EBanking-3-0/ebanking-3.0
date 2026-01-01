package com.ebanking.payment.client.dto;

import java.math.BigDecimal;
import lombok.*;

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
