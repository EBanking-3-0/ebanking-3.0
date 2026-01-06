package com.ebanking.notification.client;

import com.ebanking.notification.dto.AccountDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/** REST client for communicating with Account Service. Fetches account details enrichment. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccountServiceClient {

  private final RestTemplate restTemplate;

  @Value("${account-service.base-url:http://account-service}")
  private String accountServiceBaseUrl;

  /**
   * Fetch account information.
   *
   * @param accountId Account ID
   * @return Account details
   */
  public AccountDTO getAccount(Long accountId) {
    try {
      String url = accountServiceBaseUrl + "/api/accounts/{accountId}";
      AccountDTO response = restTemplate.getForObject(url, AccountDTO.class, accountId);

      if (response != null) {
        log.debug("Retrieved account for accountId: {}", accountId);
        return response;
      }

      log.warn("Account service returned null for accountId: {}", accountId);
      throw new AccountServiceException("Account not found: " + accountId);

    } catch (Exception e) {
      log.error("Failed to fetch account from account-service for accountId: {}", accountId, e);
      throw new AccountServiceException("Failed to fetch account: " + e.getMessage(), e);
    }
  }

  /**
   * Get account owner's user ID.
   *
   * @param accountId Account ID
   * @return User ID of account owner
   */
  public String getAccountOwnerId(Long accountId) {
    try {
      AccountDTO account = getAccount(accountId);
      return account.getUserId();
    } catch (Exception e) {
      log.error("Failed to get account owner for accountId: {}", accountId, e);
      throw new AccountServiceException("Failed to get account owner: " + e.getMessage(), e);
    }
  }

  /** Custom exception for account service errors. */
  public static class AccountServiceException extends RuntimeException {

    public AccountServiceException(String message) {
      super(message);
    }

    public AccountServiceException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
