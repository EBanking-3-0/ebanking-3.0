package com.ebanking.assistant.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "payment-service")
public interface PaymentServiceClient {

    @GetMapping("/api/transactions/account/{accountId}")
    List<Map<String, Object>> getTransactionsByAccountId(
            @PathVariable("accountId") Long accountId,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit
    );

    @GetMapping("/api/transactions/user/{userId}")
    List<Map<String, Object>> getTransactionsByUserId(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit
    );
}
