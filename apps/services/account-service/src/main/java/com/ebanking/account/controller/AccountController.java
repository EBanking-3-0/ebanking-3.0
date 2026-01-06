package com.ebanking.account.controller;

import com.ebanking.account.dto.*;
import com.ebanking.account.exception.AccountNotFoundException;
import com.ebanking.account.exception.InsufficientBalance;
import com.ebanking.account.mappers.account.AccountMapper;
import com.ebanking.account.model.Account;
import com.ebanking.account.service.AccountService;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

  private final AccountService accountService;
  private final AccountMapper accountMapper;

  @PostMapping
  @PreAuthorize("hasRole('user')")
  public ResponseEntity<AccountDTO> createAccount(
      @RequestBody AccountDTO request, Authentication authentication) {
    // todo: In a real app, extract userId from token or look it up.
    // For simplicity, we trust the request or use a hardcoded/looked-up ID if
    // available in token.
    // Ideally: Long userId = Long.parseLong(authentication.getName()); // if
    // subject is ID
    // Or using JwtAuthConverter to put ID in principal.

    // For this demo, we'll assume userId is passed in request, but verify it
    // matches token if needed.

    Account account =
        accountService.createAccount(
            request.getUserId(), request.getType(), request.getCurrency(), request.getNickname());
    return ResponseEntity.ok(mapToDTO(account));
  }

  @GetMapping("/my-accounts")
  @PreAuthorize("hasRole('user')")
  public ResponseEntity<List<AccountDTO>> getMyAccounts(@RequestParam Long userId) {
    // Again, verify userId matches token in production
    return ResponseEntity.ok(
        accountService.getAccountsByUserId(userId).stream()
            .map(accountMapper::mapToDTO)
            .collect(Collectors.toList()));
  }

  @PostMapping("/{id}/deposit")
  @PreAuthorize("hasRole('user')")
  public ResponseEntity<Void> deposit(@PathVariable Long id, @RequestBody BigDecimal amount)
      throws AccountNotFoundException {
    accountService.deposit(id, amount);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{id}/withdraw")
  @PreAuthorize("hasRole('user')")
  public ResponseEntity<Void> withdraw(@PathVariable Long id, @RequestBody BigDecimal amount)
      throws AccountNotFoundException, InsufficientBalance {
    accountService.withdraw(id, amount);
    return ResponseEntity.ok().build();
  }

  private AccountDTO mapToDTO(Account account) {
    return AccountDTO.builder()
        .id(account.getId())
        .accountNumber(account.getAccountNumber())
        .userId(account.getUserId())
        .balance(account.getBalance())
        .currency(account.getCurrency())
        .type(account.getType().name())
        .status(account.getStatus())
        .nickname(account.getNickname())
        .createdAt(account.getCreatedAt())
        .build();
  }
}
