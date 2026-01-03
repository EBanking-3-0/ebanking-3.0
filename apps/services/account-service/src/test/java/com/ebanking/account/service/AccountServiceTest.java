package com.ebanking.account.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.ebanking.account.dto.AccountDTO;
import com.ebanking.account.enums.AccountType;
import com.ebanking.account.exception.AccountNotFoundException;
import com.ebanking.account.exception.InsufficientBalance;
import com.ebanking.account.kafka.producer.AccountProducer;
import com.ebanking.account.model.Account;
import com.ebanking.account.repository.AccountRepository;
import com.ebanking.shared.kafka.events.AccountCreatedEvent;
import com.ebanking.shared.kafka.producer.TypedEventProducer;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

  @Mock
  private AccountRepository accountRepository;

  @Mock
  private TypedEventProducer eventProducer;

  @Mock
  private AccountProducer accountProducer;

  @InjectMocks
  private AccountService accountService;

  @Test
  void testCreateAccount() {
    Long userId = 1L;
    AccountType accountType = AccountType.SAVINGS;
    String currency = "USD";

    Account savedAccount = Account.builder()
        .id(1L)
        .userId(userId)
        .accountNumber("1234567890")
        .type(accountType)
        .currency(currency)
        .balance(BigDecimal.ZERO)
        .status("ACTIVE")
        .build();

    when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

    Account result = accountService.createAccount(userId, accountType.toString(), currency);

    assertNotNull(result);
    assertEquals(userId, result.getUserId());
    assertEquals(BigDecimal.ZERO, result.getBalance());

    verify(accountRepository).save(any(Account.class));
    verify(accountProducer).sendAccountCreatedEvent(any(AccountCreatedEvent.class));
    verify(eventProducer).publishAccountCreated(any(AccountCreatedEvent.class));
  }

  @Test
  void testDeleteAccount() throws AccountNotFoundException {
    Long accountId = 1L;
    Account account = Account.builder().id(accountId).userId(1L).accountNumber("123").build();

    when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

    boolean result = accountService.deleteAccount(accountId);

    assertTrue(result);
    verify(accountRepository).delete(account);
  }

  @Test
  void testDeleteAccount_NotFound() {
    Long accountId = 1L;
    when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

    assertThrows(AccountNotFoundException.class, () -> accountService.deleteAccount(accountId));
  }

  @Test
  void testDeposit() throws AccountNotFoundException {
    Long accountId = 1L;
    BigDecimal initialBalance = BigDecimal.valueOf(100);
    BigDecimal depositAmount = BigDecimal.valueOf(50);

    Account account = Account.builder()
        .id(accountId)
        .balance(initialBalance)
        .userId(1L)
        .accountNumber("123")
        .build();

    when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

    boolean result = accountService.deposit(accountId, depositAmount);

    assertTrue(result);
    assertEquals(BigDecimal.valueOf(150), account.getBalance());
    verify(accountRepository).save(account);
  }

  @Test
  void testGetAccountByNumber() throws AccountNotFoundException {
    String accountNumber = "1234567890";
    Account account = Account.builder().accountNumber(accountNumber).build();

    when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));

    Account result = accountService.getAccountByNumber(accountNumber);

    assertNotNull(result);
    assertEquals(accountNumber, result.getAccountNumber());
  }

  @Test
  void testGetAccountsByUserId() {
    Long userId = 1L;
    List<Account> accounts = List.of(Account.builder().userId(userId).build(),
        Account.builder().userId(userId).build());

    when(accountRepository.findByUserId(userId)).thenReturn(accounts);

    List<Account> result = accountService.getAccountsByUserId(userId);

    assertEquals(2, result.size());
  }

  @Test
  void testUpdateAccount() throws AccountNotFoundException {
    Long accountId = 1L;
    AccountDTO updateDto = AccountDTO.builder().balance(BigDecimal.valueOf(200)).status("FROZEN").build();

    Account existingAccount = Account.builder()
        .id(accountId)
        .balance(BigDecimal.valueOf(100))
        .status("ACTIVE")
        .userId(1L)
        .accountNumber("123")
        .build();

    when(accountRepository.findById(accountId)).thenReturn(Optional.of(existingAccount));
    when(accountRepository.save(any(Account.class))).thenReturn(existingAccount);

    Account result = accountService.updateAccount(accountId, updateDto);

    assertEquals(BigDecimal.valueOf(200), result.getBalance());
    assertEquals("FROZEN", result.getStatus());
    verify(accountRepository).save(existingAccount);
  }

  @Test
  void testWithdraw() throws AccountNotFoundException, InsufficientBalance {
    Long accountId = 1L;
    BigDecimal initialBalance = BigDecimal.valueOf(100);
    BigDecimal withdrawAmount = BigDecimal.valueOf(50);

    Account account = Account.builder()
        .id(accountId)
        .balance(initialBalance)
        .userId(1L)
        .accountNumber("123")
        .build();

    when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

    boolean result = accountService.withdraw(accountId, withdrawAmount);

    assertTrue(result);
    assertEquals(BigDecimal.valueOf(50), account.getBalance());
    verify(accountRepository).save(account);
  }

  @Test
  void testWithdraw_InsufficientBalance() {
    Long accountId = 1L;
    BigDecimal initialBalance = BigDecimal.valueOf(10);
    BigDecimal withdrawAmount = BigDecimal.valueOf(50);

    Account account = Account.builder().id(accountId).balance(initialBalance).build();

    when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

    assertThrows(
        InsufficientBalance.class, () -> accountService.withdraw(accountId, withdrawAmount));
  }
}
