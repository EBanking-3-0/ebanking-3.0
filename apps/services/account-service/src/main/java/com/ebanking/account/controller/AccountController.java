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
    // todo:k In a real app, extract userId from token or look it up.
    // For simplicity, we trust the request or use a hardcoded/looked-up ID if
    // available in token.
    // Ideally: Long userId = Long.parseLong(authentication.getName()); // if
    // subject is ID
    // Or using JwtAuthConverter to put ID in principal.

    // For this demo, we'll assume userId is passed in request, but verify it
    // matches token if
    // needed.

    Account account =
        accountService.createAccount(request.getUserId(), request.getType(), request.getCurrency());
    return ResponseEntity.ok(accountMapper.mapToDTO(account));
  }

  @GetMapping("/{id}")
  public ResponseEntity<AccountDTO> getAccount(@PathVariable Long id) {
    try {
      Account account = accountService.getAccountById(id);
      return ResponseEntity.ok(accountMapper.mapToDTO(account));
    } catch (AccountNotFoundException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/lookup")
  public ResponseEntity<AccountDTO> getAccountByNumber(
      @RequestParam("accountNumber") String accountNumber) {
    try {
      Account account = accountService.getAccountByNumber(accountNumber);
      return ResponseEntity.ok(accountMapper.mapToDTO(account));
    } catch (AccountNotFoundException e) {
      return ResponseEntity.notFound().build();
    }
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

  @PutMapping("/{id}")
  public ResponseEntity<?> updateAccount(
      @PathVariable Long id, @RequestBody AccountDTO accountDTO) {
    try {
      Account account = accountService.updateAccount(id, accountDTO);
      return ResponseEntity.ok(accountMapper.mapToDTO(account));
    } catch (AccountNotFoundException e) {
      return ResponseEntity.badRequest().body("Account not found");
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteAccount(@PathVariable Long id) {
    try {
      accountService.deleteAccount(id);
      return ResponseEntity.ok("Account deleted successfully");
    } catch (AccountNotFoundException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping("/{id}/deposit")
  public ResponseEntity<?> deposit(@PathVariable Long id, @RequestBody BigDecimal amount) {
    try {
      accountService.deposit(id, amount);
    } catch (AccountNotFoundException e) {
      return ResponseEntity.badRequest().body("Account not found");
    }
    return ResponseEntity.ok("Deposit successful");
  }

  @PostMapping("/{id}/withdraw")
  public ResponseEntity<?> withdraw(@PathVariable Long id, @RequestBody BigDecimal amount) {
    try {
      accountService.withdraw(id, amount);
    } catch (AccountNotFoundException e) {
      return ResponseEntity.badRequest().body("Account not found");
    } catch (InsufficientBalance e) {
      ResponseEntity.badRequest().body("Insufficient balance");
    }
    return ResponseEntity.ok("Withdrawal successful");
  }

  @PostMapping("/{id}/debit")
  public ResponseEntity<DebitResponse> debit(
      @PathVariable Long id, @RequestBody DebitRequest request) {
    try {
      DebitResponse response = accountService.debit(id, request);
      return ResponseEntity.ok(response);
    } catch (AccountNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (InsufficientBalance e) {
      return ResponseEntity.badRequest()
          .body(
              DebitResponse.builder()
                  .transactionId(request.getTransactionId())
                  .status("FAILED")
                  .message("Insufficient balance")
                  .build());
    }
  }

  @PostMapping("/{id}/credit")
  public ResponseEntity<CreditResponse> credit(
      @PathVariable Long id, @RequestBody CreditRequest request) {
    try {
      CreditResponse response = accountService.credit(id, request);
      return ResponseEntity.ok(response);
    } catch (AccountNotFoundException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/{id}/balance")
  public ResponseEntity<BalanceResponse> getBalance(@PathVariable Long id) {
    try {
      BalanceResponse response = accountService.getBalance(id);
      return ResponseEntity.ok(response);
    } catch (AccountNotFoundException e) {
      return ResponseEntity.notFound().build();
    }
  }
}
