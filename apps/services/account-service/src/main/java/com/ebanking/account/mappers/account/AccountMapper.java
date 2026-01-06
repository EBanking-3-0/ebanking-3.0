package com.ebanking.account.mappers.account;

import com.ebanking.account.dto.AccountDTO;
import com.ebanking.account.model.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

  public AccountDTO mapToDTO(Account account) {
    return AccountDTO.builder()
        .id(account.getId())
        .accountNumber(account.getAccountNumber())
        .iban(account.getIban())
        .userId(account.getUserId())
        .balance(account.getBalance())
        .currency(account.getCurrency())
        .type(account.getType().toString())
        .status(account.getStatus())
        .createdAt(account.getCreatedAt())
        .build();
  }
}
