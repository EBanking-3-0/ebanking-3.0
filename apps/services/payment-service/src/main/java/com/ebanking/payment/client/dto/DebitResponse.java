package com.ebanking.payment.client.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebitResponse {
  private String transactionId;
  private String status;
  private String message;
}
