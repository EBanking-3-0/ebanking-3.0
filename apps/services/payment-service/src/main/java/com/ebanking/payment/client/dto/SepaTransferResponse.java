package com.ebanking.payment.client.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SepaTransferResponse {
  private String status; // ACCEPTED, SENT, PENDING, REJECTED
  private String externalTransactionId;
  private String iso20022Reference; // Added for ISO 20022 message reference
  private String rejectionReason;
  private String estimatedCompletionDate;
  private String message;
}
