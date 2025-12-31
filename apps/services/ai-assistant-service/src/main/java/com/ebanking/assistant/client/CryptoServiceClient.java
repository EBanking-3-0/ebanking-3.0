package com.ebanking.assistant.client;

import java.util.List;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "crypto-service")
public interface CryptoServiceClient {

  @GetMapping("/api/crypto/portfolio/{userId}")
  Map<String, Object> getPortfolioByUserId(@PathVariable("userId") Long userId);

  @GetMapping("/api/crypto/holdings/{userId}")
  List<Map<String, Object>> getHoldingsByUserId(@PathVariable("userId") Long userId);
}
