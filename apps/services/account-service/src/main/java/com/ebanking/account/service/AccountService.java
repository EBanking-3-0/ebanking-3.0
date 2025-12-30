package com.ebanking.account.service;

import com.ebanking.account.dto.AccountDTO;
import com.ebanking.account.exception.AccountNotFoundException;
import com.ebanking.account.model.Account;
import com.ebanking.account.repository.AccountRepository;
import com.ebanking.shared.kafka.events.AccountCreatedEvent;
import com.ebanking.shared.kafka.producer.TypedEventProducer;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

  private final AccountRepository accountRepository;
  private final TypedEventProducer eventProducer;

  @Transactional
  public Account createAccount(Long userId, String accountType, String currency) {
    String accountNumber = generateAccountNumber();

    Account account = Account.builder()
        .userId(userId)
        .accountNumber(accountNumber)
        .type(accountType)
        .currency(currency)
        .balance(BigDecimal.ZERO)
        .status("ACTIVE")
        .build();

    Account savedAccount = accountRepository.save(account);
    log.info("Created account: {} for user: {}", accountNumber, userId);

    AccountCreatedEvent event = AccountCreatedEvent.builder()
        .accountId(savedAccount.getId())
        .userId(userId)
        .accountNumber(accountNumber)
        .accountType(accountType)
        .currency(currency)
        .initialBalance(BigDecimal.ZERO)
        .source("account-service")
        .build();

    eventProducer.publishAccountCreated(event);

    return savedAccount;
  }

  public Account updateAccount(Long id, AccountDTO account) {
    Optional<Account> existingAccount = accountRepository.findById(id);
    if (existingAccount.isEmpty()) {
      return null;
    }
    existingAccount.get().setBalance(account.getBalance());
    existingAccount.get().setStatus(account.getStatus());
    existingAccount.get().setUpdatedAt(LocalDateTime.now());
    return accountRepository.save(existingAccount.get());
  }

  public boolean deleteAccount(Long id) {
    Optional<Account> existingAccount = accountRepository.findById(id);
    if (existingAccount.isEmpty()) {
      return false;
    }
    accountRepository.delete(existingAccount.get());
    return true;
  }

  public List<Account> getAccountsByUserId(Long userId) {
    return accountRepository.findByUserId(userId);
  }

  public Account getAccountByNumber(String accountNumber) {
    return accountRepository
        .findByAccountNumber(accountNumber)
        .orElseThrow(() -> new RuntimeException("Account not found"));
  }

  private String generateAccountNumber() {
    return UUID.randomUUID().toString().substring(0, 10).toUpperCase();
  }
}
