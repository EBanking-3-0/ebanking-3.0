package com.ebanking.account.controller;

import com.ebanking.account.dto.AccountDTO;
import com.ebanking.account.model.Account;
import com.ebanking.account.service.AccountService;
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

  @PostMapping
  @PreAuthorize("hasRole('user')")
  public ResponseEntity<AccountDTO> createAccount(
      @RequestBody AccountDTO request, Authentication authentication) {
    // todo:k In a real app, extract userId from token or look it up.
    // For simplicity, we trust the request or use a hardcoded/looked-up ID if available in token.
    // Ideally: Long userId = Long.parseLong(authentication.getName()); // if subject is ID
    // Or using JwtAuthConverter to put ID in principal.

    // For this demo, we'll assume userId is passed in request, but verify it matches token if
    // needed.

    Account account =
        accountService.createAccount(request.getUserId(), request.getType(), request.getCurrency());
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
}
