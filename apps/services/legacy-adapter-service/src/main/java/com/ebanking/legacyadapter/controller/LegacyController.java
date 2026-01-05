package com.ebanking.legacyadapter.controller;

import com.ebanking.legacyadapter.dto.*;
import com.ebanking.legacyadapter.service.LegacyMockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/legacy")
@RequiredArgsConstructor
public class LegacyController {

  private final LegacyMockService mockService;

  @PostMapping("/sepa/transfer")
  public ResponseEntity<SepaTransferResponse> executeSepaTransfer(
      @RequestBody SepaTransferRequest request) {
    log.info("Received SEPA transfer request: {}", request.getTransactionId());
    return ResponseEntity.ok(mockService.processSepaTransfer(request));
  }

  @PostMapping("/instant/transfer")
  public ResponseEntity<InstantTransferResponse> executeInstantTransfer(
      @RequestBody InstantTransferRequest request) {
    log.info("Received Instant transfer request: {}", request.getTransactionId());
    return ResponseEntity.ok(mockService.processInstantTransfer(request));
  }
}
