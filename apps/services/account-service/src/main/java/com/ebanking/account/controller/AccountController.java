package com.ebanking.account.controller;

import com.ebanking.account.dto.AccountDTO;
import com.ebanking.account.exception.AccountNotFoundException;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

  private final AccountService accountService;

  @PostMapping
  @PreAuthorize("hasRole('user')")
  public ResponseEntity<AccountDTO> createAccount(
      @RequestBody AccountDTO request, Authentication authentication) {
    // todo:k In a real app, extract userId from token or look it up.
    // For simplicity, we trust the request or use a hardcoded/looked-up ID if
    // available in token.
    // Ideally: Long userId = Long.parseLong(authentication.getName()); // if
    // subject is ID
    // Or using JwtAuthConverter to put ID in principal.

    // For this demo, we'll assume userId is passed in request, but verify it
    // matches token if
    // needed.

    Account account = accountService.createAccount(request.getUserId(), request.getType(), request.getCurrency());
    return ResponseEntity.ok(mapToDTO(account));
  }

  @GetMapping("/my-accounts")
  @PreAuthorize("hasRole('user')")
  public ResponseEntity<List<AccountDTO>> getMyAccounts(@RequestParam Long userId) {
    // Again, verify userId matches token in production
    return ResponseEntity.ok(
        accountService.getAccountsByUserId(userId).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList()));
  }

  @PutMapping("/{id}")
  public ResponseEntity<AccountDTO> updateAccount(@PathVariable Long id, @RequestBody AccountDTO accountDTO) {
    try {
      Account account = accountService.updateAccount(id, accountDTO);
      return ResponseEntity.ok(mapToDTO(account));
    } catch (AccountNotFoundException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteAccount(@PathVariable Long id) {
    boolean deleted = accountService.deleteAccount(id);
    if (!deleted) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok("Account deleted successfully");
  }

  private AccountDTO mapToDTO(Account account) {
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

  @PostMapping("/{id}/deposit")
  public ResponseEntity<?> deposit(@PathVariable Long id, @RequestBody BigDecimal amount) {
    accountService.deposit(id, amount);
    return ResponseEntity.ok("Deposit successful");
  }

  @PostMapping("/{id}/withdraw")
  public ResponseEntity<?> withdraw(@PathVariable Long id, @RequestBody BigDecimal amount) {
    accountService.withdraw(id, amount);
    return ResponseEntity.ok("Withdrawal successful");
  }
}
