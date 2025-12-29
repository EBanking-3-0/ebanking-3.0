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
  public Map<String, Object> execute(Long userId, Map<String, Object> parameters)
      throws ActionExecutionException {
    try {
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
        log.info("Querying accounts for user {}", userId);
        List<Map<String, Object>> accounts = accountServiceClient.getAccountsByUserId(userId);
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
