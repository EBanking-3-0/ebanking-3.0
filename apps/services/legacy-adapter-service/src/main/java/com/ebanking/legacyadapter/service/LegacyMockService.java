package com.ebanking.legacyadapter.service;

import com.ebanking.legacyadapter.dto.*;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LegacyMockService {

    public SepaTransferResponse processSepaTransfer(SepaTransferRequest request) {
        log.info("MOCK: Processing SEPA transfer for {}", request.getAmount());

        // Simulate processing time
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Default: Success
        return SepaTransferResponse.builder()
                .status("ACCEPTED")
                .externalTransactionId("CBS-SEPA-" + UUID.randomUUID().toString().substring(0, 8))
                .iso20022Reference("MSG-SEPA-" + System.currentTimeMillis())
                .estimatedCompletionDate(LocalDate.now().plusDays(1).toString())
                .message("Transfer accepted by Core Banking System")
                .build();
    }

    public InstantTransferResponse processInstantTransfer(InstantTransferRequest request) {
        log.info("MOCK: Processing Instant transfer for {}", request.getAmount());

        // Instant payments must be fast (< 10s)
        try {
            // Random delay 100-500ms
            Thread.sleep(ThreadLocalRandom.current().nextLong(100, 500));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Simulate Random Rejection (1% chance)
        if (ThreadLocalRandom.current().nextInt(100) < 1) {
            return InstantTransferResponse.builder()
                    .status("NACK")
                    .rejectionReason("Simulated random rejection")
                    .errorCode("CBS_ERR_999")
                    .message("Transfer rejected by Core Banking System")
                    .build();
        }

        return InstantTransferResponse.builder()
                .status("ACK")
                .externalTransactionId("CBS-INST-" + UUID.randomUUID().toString().substring(0, 8))
                .iso20022Reference("MSG-INST-" + System.currentTimeMillis())
                .message("Instant Transfer settled by Core Banking System")
                .build();
    }
}
