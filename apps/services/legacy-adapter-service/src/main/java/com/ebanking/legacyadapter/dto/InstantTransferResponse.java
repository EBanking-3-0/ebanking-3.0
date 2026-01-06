package com.ebanking.legacyadapter.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstantTransferResponse {
  private String status; // ACK, NACK, TIMEOUT
  private String externalTransactionId;
  private String iso20022Reference;
  private String rejectionReason;
  private String errorCode;
  private String message;
}
