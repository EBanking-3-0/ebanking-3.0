package com.ebanking.assistant.client;

import java.util.List;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "account-service")
public interface AccountServiceClient {

  @GetMapping("/api/accounts/{id}")
  Map<String, Object> getAccountById(@PathVariable("id") Long id);

  @GetMapping("/api/accounts/user/{userId}")
  List<Map<String, Object>> getAccountsByUserId(@PathVariable("userId") Long userId);

  @GetMapping("/api/accounts/{id}/balance")
  Map<String, Object> getAccountBalance(@PathVariable("id") Long id);
}
