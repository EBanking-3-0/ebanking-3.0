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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

  private final AccountService accountService;
  private final AccountMapper accountMapper;

  @GetMapping("/test")
  public ResponseEntity<?> test() {

    return ResponseEntity.ok().body("message");
  }

  @PostMapping
  // @PreAuthorize("hasRole('user')")
  public ResponseEntity<AccountDTO> createAccount(
      @RequestBody AccountDTO request, Authentication authentication) {
    org.springframework.security.oauth2.jwt.Jwt jwt =
        (org.springframework.security.oauth2.jwt.Jwt) authentication.getPrincipal();
    String userId = jwt.getClaimAsString("sub");

    Account account =
        accountService.createAccount(userId, request.getType(), request.getCurrency(), request.getNickname());
    return ResponseEntity.ok(accountMapper.mapToDTO(account));
  }

  @GetMapping("/{id}")
  public ResponseEntity<AccountDTO> getAccount(
      @PathVariable Long id, Authentication authentication) {
    org.springframework.security.oauth2.jwt.Jwt jwt =
        (org.springframework.security.oauth2.jwt.Jwt) authentication.getPrincipal();
    String userId = jwt.getClaimAsString("sub");

    try {
      Account account = accountService.getAccountById(id);
      if (!account.getUserId().equals(userId)) {
        return ResponseEntity.status(403).build();
      }
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
  // @PreAuthorize("hasRole('user')")
  public ResponseEntity<List<AccountDTO>> getMyAccounts(Authentication authentication) {
    org.springframework.security.oauth2.jwt.Jwt jwt =
        (org.springframework.security.oauth2.jwt.Jwt) authentication.getPrincipal();
    String userId = jwt.getClaimAsString("sub");

    return ResponseEntity.ok(
        accountService.getAccountsByUserId(userId).stream()
            .map(accountMapper::mapToDTO)
            .collect(Collectors.toList()));
  }

  @PostMapping("/{id}/deposit")
  // @PreAuthorize("hasRole('user')")
  public ResponseEntity<Void> deposit(@PathVariable Long id, @RequestBody BigDecimal amount)
      throws AccountNotFoundException {
    accountService.deposit(id, amount);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{id}/withdraw")
  // @PreAuthorize("hasRole('user')")
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
