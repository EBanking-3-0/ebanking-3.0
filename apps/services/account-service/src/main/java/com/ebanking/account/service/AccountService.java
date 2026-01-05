package com.ebanking.account.service;

import com.ebanking.account.dto.*;
import com.ebanking.account.enums.AccountType;
import com.ebanking.account.exception.AccountNotFoundException;
import com.ebanking.account.exception.InsufficientBalance;
import com.ebanking.account.kafka.producer.AccountProducer;
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
  private final AccountProducer accountProducer;
  private final CurrencyService currencyService;

  @Transactional
  public Account createAccount(Long userId, String accountType, String currency) {
    String accountNumber = generateAccountNumber();

    String iban = generateIban(accountNumber);

    Account account = Account.builder()
        .userId(userId)
        .accountNumber(accountNumber)
        .iban(iban)
        .type(AccountType.valueOf(accountType))
        .currency(currency)
        .balance(BigDecimal.ZERO)
        .status("ACTIVE")
        .build();

    Account savedAccount = accountRepository.save(account);
    log.info("Created account: {} for user: {}", accountNumber, userId);
    AccountCreatedEvent createdAccountEvent = AccountCreatedEvent.builder()
        .accountId(savedAccount.getId())
        .userId(userId)
        .accountNumber(accountNumber)
        .accountType(accountType)
        .currency(currency)
        .initialBalance(BigDecimal.ZERO)
        .source("account-service")
        .build();

    accountProducer.sendAccountCreatedEvent(createdAccountEvent);

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

  public Account updateAccount(Long id, AccountDTO account) throws AccountNotFoundException {
    Optional<Account> existingAccount = accountRepository.findById(id);
    if (existingAccount.isEmpty()) {
      log.error("Account not found: {}", id);
      throw new AccountNotFoundException("Account not found");
    }
    existingAccount.get().setBalance(account.getBalance());
    existingAccount.get().setStatus(account.getStatus());
    existingAccount.get().setUpdatedAt(LocalDateTime.now());

    log.info(
        "Updated account: {} for user: {}",
        existingAccount.get().getAccountNumber(),
        existingAccount.get().getUserId());
    return accountRepository.save(existingAccount.get());
  }

  public boolean deleteAccount(Long id) throws AccountNotFoundException {
    Optional<Account> existingAccount = accountRepository.findById(id);
    if (existingAccount.isEmpty()) {
      log.error("Account not found: {}", id);
      throw new AccountNotFoundException("Account not found");
    }
    accountRepository.delete(existingAccount.get());
    log.info(
        "Deleted account: {} for user: {}",
        existingAccount.get().getAccountNumber(),
        existingAccount.get().getUserId());
    return true;
  }

  public List<Account> getAccountsByUserId(Long userId) {
    return accountRepository.findByUserId(userId);
  }

  public Account getAccountByNumber(String accountNumber) throws AccountNotFoundException {
    return accountRepository
        .findByAccountNumber(accountNumber)
        .orElseThrow(
            () -> {
              log.error("Account not found: {}", accountNumber);
              return new AccountNotFoundException("Account not found");
            });
  }

  private String generateAccountNumber() {
    return UUID.randomUUID().toString().substring(0, 10).toUpperCase();
  }

  private String generateIban(String accountNumber) {
    // Génération simplifiée d'IBAN français (FR + 2 chiffres de contrôle + 23
    // caractères)
    // Format: FR76 XXXX XXXX XXXX XXXX XXXX XXX
    // En production, utiliser une bibliothèque spécialisée pour générer des IBAN
    // valides
    String countryCode = "FR";
    String checkDigits = "76"; // Valeur par défaut pour la démo
    String bankCode = "20041"; // Code banque fictif
    String branchCode = "01005"; // Code agence fictif
    String accountCode = accountNumber.replaceAll("[^0-9]", "").substring(0, Math.min(11, accountNumber.length()));
    // Compléter avec des zéros si nécessaire
    while (accountCode.length() < 11) {
      accountCode += "0";
    }
    String nationalCheck = "26"; // Clé RIB fictive
    return countryCode + checkDigits + bankCode + branchCode + accountCode + nationalCheck;
  }

  public Account getAccountById(Long id) throws AccountNotFoundException {
    return accountRepository
        .findById(id)
        .orElseThrow(
            () -> {
              log.error("Account not found: {}", id);
              return new AccountNotFoundException("Account not found");
            });
  }

  @Transactional
  public DebitResponse debit(Long accountId, DebitRequest request)
      throws AccountNotFoundException, InsufficientBalance {
    Optional<Account> accountOpt = accountRepository.findById(accountId);
    if (accountOpt.isEmpty()) {
      log.error("Account not found: {}", accountId);
      throw new AccountNotFoundException("Account not found");
    }

    Account account = accountOpt.get();

    // Convert logic
    BigDecimal debitAmount = request.getAmount();
    if (request.getCurrency() != null && !request.getCurrency().equals(account.getCurrency())) {
      log.info("Converting debit amount {} from {} to {}", request.getAmount(), request.getCurrency(),
          account.getCurrency());
      debitAmount = currencyService.convert(request.getAmount(), request.getCurrency(), account.getCurrency());
    }

    BigDecimal newBalance = account.getBalance().subtract(debitAmount);

    if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
      log.error(
          "Insufficient balance for account {}: current={}, requested={} (converted={})",
          accountId,
          account.getBalance(),
          request.getAmount(),
          debitAmount);
      throw new InsufficientBalance("Insufficient balance");
    }

    account.setBalance(newBalance);
    accountRepository.save(account);

    log.info(
        "Debited {} {} ({} {}) from account {} (transactionId: {}, idempotencyKey: {})",
        debitAmount,
        account.getCurrency(),
        request.getAmount(),
        request.getCurrency() != null ? request.getCurrency() : account.getCurrency(),
        accountId,
        request.getTransactionId(),
        request.getIdempotencyKey());

    return DebitResponse.builder()
        .transactionId(request.getTransactionId())
        .status("SUCCESS")
        .message("Debit successful")
        .build();
  }

  @Transactional
  public CreditResponse credit(Long accountId, CreditRequest request)
      throws AccountNotFoundException {
    Optional<Account> accountOpt = accountRepository.findById(accountId);
    if (accountOpt.isEmpty()) {
      log.error("Account not found: {}", accountId);
      throw new AccountNotFoundException("Account not found");
    }

    Account account = accountOpt.get();

    // Convert logic
    BigDecimal creditAmount = request.getAmount();
    if (request.getCurrency() != null && !request.getCurrency().equals(account.getCurrency())) {
      log.info("Converting credit amount {} from {} to {}", request.getAmount(), request.getCurrency(),
          account.getCurrency());
      creditAmount = currencyService.convert(request.getAmount(), request.getCurrency(), account.getCurrency());
    }

    account.setBalance(account.getBalance().add(creditAmount));
    accountRepository.save(account);

    log.info(
        "Credited {} {} ({} {}) to account {} (transactionId: {}, idempotencyKey: {})",
        creditAmount,
        account.getCurrency(),
        request.getAmount(),
        request.getCurrency() != null ? request.getCurrency() : account.getCurrency(),
        accountId,
        request.getTransactionId(),
        request.getIdempotencyKey());

    return CreditResponse.builder()
        .transactionId(request.getTransactionId())
        .status("SUCCESS")
        .message("Credit successful")
        .build();
  }

  public BalanceResponse getBalance(Long accountId) throws AccountNotFoundException {
    Optional<Account> accountOpt = accountRepository.findById(accountId);
    if (accountOpt.isEmpty()) {
      log.error("Account not found: {}", accountId);
      throw new AccountNotFoundException("Account not found");
    }

    Account account = accountOpt.get();
    return BalanceResponse.builder()
        .availableBalance(account.getBalance())
        .currentBalance(account.getBalance())
        .currency(account.getCurrency())
        .status(account.getStatus())
        .build();
  }

  public boolean deposit(Long id, BigDecimal amount) throws AccountNotFoundException {
    Optional<Account> existingAccount = accountRepository.findById(id);
    if (existingAccount.isEmpty()) {
      log.error("Account not found: {}", id);
      throw new AccountNotFoundException("Account not found");
    }
    existingAccount.get().setBalance(existingAccount.get().getBalance().add(amount));
    accountRepository.save(existingAccount.get());
    // todo: send event to kafka (transaction service)
    log.info(
        "Deposited {} to account: {} for user: {}",
        amount,
        existingAccount.get().getAccountNumber(),
        existingAccount.get().getUserId());
    return true;
  }

  public boolean withdraw(Long id, BigDecimal amount)
      throws AccountNotFoundException, InsufficientBalance {
    Optional<Account> existingAccount = accountRepository.findById(id);
    if (existingAccount.isEmpty()) {
      log.error("Account not found: {}", id);
      throw new AccountNotFoundException("Account not found");
    }
    if (existingAccount.get().getBalance().subtract(amount).compareTo(BigDecimal.ZERO) < 0) {
      log.error("Insufficient balance: {}", existingAccount.get().getBalance());
      throw new InsufficientBalance("Insufficient balance");
    }
    existingAccount.get().setBalance(existingAccount.get().getBalance().subtract(amount));
    // todo: send event to kafka (transaction service)
    accountRepository.save(existingAccount.get());
    log.info(
        "Withdrawn {} from account: {} for user: {}",
        amount,
        existingAccount.get().getAccountNumber(),
        existingAccount.get().getUserId());
    return true;
  }
}
