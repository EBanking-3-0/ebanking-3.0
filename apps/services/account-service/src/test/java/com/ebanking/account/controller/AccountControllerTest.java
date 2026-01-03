package com.ebanking.account.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ebanking.account.dto.AccountDTO;
import com.ebanking.account.mappers.account.AccountMapper;
import com.ebanking.account.model.Account;
import com.ebanking.account.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AccountController.class)
public class AccountControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private AccountService accountService;

  @MockBean private AccountMapper accountMapper;

  @Autowired private ObjectMapper objectMapper;

  @Test
  @WithMockUser(roles = "user")
  void testCreateAccount() throws Exception {
    AccountDTO request = AccountDTO.builder().userId(1L).type("SAVINGS").currency("USD").build();

    Account account = Account.builder().id(1L).userId(1L).accountNumber("1234567890").build();

    when(accountService.createAccount(any(), any(), any())).thenReturn(account);
    when(accountMapper.mapToDTO(any(Account.class))).thenReturn(request);

    mockMvc
        .perform(
            post("/api/accounts")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "user")
  void testGetMyAccounts() throws Exception {
    when(accountService.getAccountsByUserId(anyLong())).thenReturn(List.of(new Account()));
    when(accountMapper.mapToDTO(any())).thenReturn(new AccountDTO());

    mockMvc
        .perform(get("/api/accounts/my-accounts").param("userId", "1"))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "user")
  void testDeposit() throws Exception {
    BigDecimal amount = BigDecimal.valueOf(100);

    mockMvc
        .perform(
            post("/api/accounts/1/deposit")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(amount)))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "user")
  void testWithdraw() throws Exception {
    BigDecimal amount = BigDecimal.valueOf(50);

    mockMvc
        .perform(
            post("/api/accounts/1/withdraw")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(amount)))
        .andExpect(status().isOk());
  }
}
