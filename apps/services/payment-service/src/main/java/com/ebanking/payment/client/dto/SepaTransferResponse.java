package com.ebanking.payment.client.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SepaTransferResponse {
  private String status; // SENT, PENDING, REJECTED
  private String externalTransactionId;
  private String rejectionReason;
  private String estimatedCompletionDate;
  private String message;
}
