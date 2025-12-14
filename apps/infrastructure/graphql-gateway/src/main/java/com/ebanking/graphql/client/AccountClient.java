package com.ebanking.graphql.client;

import com.ebanking.graphql.model.AccountDTO;
import com.ebanking.graphql.model.CreateAccountInput;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "account-service")
public interface AccountClient {

    @PostMapping("/api/accounts")
    AccountDTO createAccount(@RequestBody CreateAccountInput input);

    @GetMapping("/api/accounts/my-accounts")
    List<AccountDTO> getMyAccounts(@RequestParam("userId") Long userId);
}
