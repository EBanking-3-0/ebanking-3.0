package com.ebanking.assistant.action.actions;

import com.ebanking.assistant.action.ActionExecutionException;
import com.ebanking.assistant.action.ActionExecutor;
import com.ebanking.assistant.client.AccountServiceClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueryAccountInfoAction implements ActionExecutor {

  private final AccountServiceClient accountServiceClient;

  @Override
  public String getActionName() {
    return "query_account_info";
  }

  @Override
  public String getDescription() {
    return "Query account information including account type, currency, and status. Requires accountId or userId parameter.";
  }

  @Override
  public Map<String, Object> execute(String userId, Map<String, Object> parameters)
      throws ActionExecutionException {
    try {
      // For now, try to convert userId to Long for legacy service clients
      Long userIdLong = null;
      try {
        userIdLong = Long.parseLong(userId);
      } catch (NumberFormatException e) {
        // UUID userId, keep as null for now
        log.warn("Cannot convert UUID userId to Long for legacy clients: {}", userId);
      }

      Object accountIdObj = parameters.get("accountId");
      Object userIdObj = parameters.get("userId");

      Map<String, Object> result = new HashMap<>();

      if (accountIdObj != null) {
        Long accountId;
        if (accountIdObj instanceof Number) {
          accountId = ((Number) accountIdObj).longValue();
        } else {
          accountId = Long.parseLong(accountIdObj.toString());
        }

        log.info("Querying account info for account {} for user {}", accountId, userId);
        Map<String, Object> account = accountServiceClient.getAccountById(accountId);
        result.put("account", account);
      } else if (userIdObj != null) {
        Long targetUserId;
        if (userIdObj instanceof Number) {
          targetUserId = ((Number) userIdObj).longValue();
        } else {
          targetUserId = Long.parseLong(userIdObj.toString());
        }

        log.info("Querying accounts for user {}", targetUserId);
        List<Map<String, Object>> accounts = accountServiceClient.getAccountsByUserId(targetUserId);
        result.put("accounts", accounts);
        result.put("count", accounts != null ? accounts.size() : 0);
      } else {
        // Default to current user
        if (userIdLong == null) {
          throw new ActionExecutionException(
              "Cannot query accounts: userId is not numeric and no accountId/userId parameter provided");
        }
        log.info("Querying accounts for user {}", userIdLong);
        List<Map<String, Object>> accounts = accountServiceClient.getAccountsByUserId(userIdLong);
        result.put("accounts", accounts);
        result.put("count", accounts != null ? accounts.size() : 0);
      }

      result.put("success", true);
      return result;
    } catch (Exception e) {
      log.error("Error querying account info", e);
      throw new ActionExecutionException("Failed to query account info: " + e.getMessage(), e);
    }
  }
}
