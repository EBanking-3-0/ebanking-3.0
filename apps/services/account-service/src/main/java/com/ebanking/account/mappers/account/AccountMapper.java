package com.ebanking.account.mappers.account;

import org.springframework.stereotype.Component;

import com.ebanking.account.dto.AccountDTO;
import com.ebanking.account.model.Account;

@Component
public class AccountMapper {

    public AccountDTO mapToDTO(Account account) {
        return AccountDTO.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .userId(account.getUserId())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .type(account.getType())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .build();
    }

}
