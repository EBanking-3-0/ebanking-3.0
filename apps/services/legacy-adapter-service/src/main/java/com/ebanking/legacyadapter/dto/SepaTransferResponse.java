package com.ebanking.legacyadapter.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SepaTransferResponse {
  private String status; // ACCEPTED, SENT, PENDING, REJECTED
  private String externalTransactionId;
  private String iso20022Reference;
  private String rejectionReason;
  private String estimatedCompletionDate;
  private String message;
}
