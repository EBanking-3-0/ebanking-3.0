package com.ebanking.payment.client;

import com.ebanking.payment.client.dto.InstantTransferRequest;
import com.ebanking.payment.client.dto.InstantTransferResponse;
import com.ebanking.payment.client.dto.SepaTransferRequest;
import com.ebanking.payment.client.dto.SepaTransferResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "legacy-adapter", path = "/api/legacy")
public interface LegacyAdapterClient {

  @PostMapping("/sepa")
  SepaTransferResponse initiateSepaTransfer(@RequestBody SepaTransferRequest request);

  @PostMapping("/instant")
  InstantTransferResponse initiateInstantTransfer(@RequestBody InstantTransferRequest request);
}
