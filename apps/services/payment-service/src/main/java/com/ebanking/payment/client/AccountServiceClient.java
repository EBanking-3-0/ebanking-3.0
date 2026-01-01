package com.ebanking.payment.client;

import com.ebanking.payment.client.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "account-service", path = "/api/accounts")
public interface AccountServiceClient {

  @GetMapping("/{id}")
  AccountResponse getAccount(@PathVariable("id") Long id);

  @GetMapping("/lookup")
  AccountResponse getAccountByNumber(@RequestParam("accountNumber") String accountNumber);

  @PostMapping("/{id}/debit")
  DebitResponse debit(@PathVariable("id") Long id, @RequestBody DebitRequest request);

  @PostMapping("/{id}/credit")
  CreditResponse credit(@PathVariable("id") Long id, @RequestBody CreditRequest request);
}
