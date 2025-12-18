package com.ebanking.assistant.tool;

import com.ebanking.assistant.service.ActionExecutorService;
import dev.langchain4j.agent.tool.Tool;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Banking tools that can be called by the AI assistant. These tools are automatically discovered by
 * LangChain4j and made available to the LLM.
 *
 * <p>Note: userId is passed as a ThreadLocal context set by AiService before tool execution.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BankingTools {

  private final ActionExecutorService actionExecutorService;

  // ThreadLocal to store userId context for tool execution
  private static final ThreadLocal<String> userIdContext = new ThreadLocal<>();

  public static void setUserId(String userId) {
    userIdContext.set(userId);
  }

  public static String getUserId() {
    return userIdContext.get();
  }

  public static void clearUserId() {
    userIdContext.remove();
  }

  @Tool("Query the balance of a bank account. Requires accountId parameter.")
  public String queryBalance(Long accountId) {
    try {
      String userId = getUserId();
      if (userId == null) {
        return "Error: User context not available";
      }
      Map<String, Object> parameters = new HashMap<>();
      parameters.put("accountId", accountId);
      Map<String, Object> result =
          actionExecutorService.executeAction("query_balance", userId, parameters);
      return formatResult(result);
    } catch (Exception e) {
      log.error("Error in queryBalance tool", e);
      return "Error querying balance: " + e.getMessage();
    }
  }

  @Tool(
      "Query transaction history for an account or user. Requires accountId parameter, and optional limit (default 10).")
  public String queryTransactions(Long accountId, Integer limit) {
    try {
      String userId = getUserId();
      if (userId == null) {
        return "Error: User context not available";
      }
      Map<String, Object> parameters = new HashMap<>();
      if (accountId != null) {
        parameters.put("accountId", accountId);
      } else {
        parameters.put("userId", userId);
      }
      if (limit != null && limit > 0) {
        parameters.put("limit", limit);
      }
      Map<String, Object> result =
          actionExecutorService.executeAction("query_transactions", userId, parameters);
      return formatResult(result);
    } catch (Exception e) {
      log.error("Error in queryTransactions tool", e);
      return "Error querying transactions: " + e.getMessage();
    }
  }

  @Tool(
      "Query account information including account type, currency, and status. Requires accountId parameter (optional, defaults to user's accounts).")
  public String queryAccountInfo(Long accountId) {
    try {
      String userId = getUserId();
      if (userId == null) {
        return "Error: User context not available";
      }
      Map<String, Object> parameters = new HashMap<>();
      if (accountId != null) {
        parameters.put("accountId", accountId);
      } else {
        parameters.put("userId", userId);
      }
      Map<String, Object> result =
          actionExecutorService.executeAction("query_account_info", userId, parameters);
      return formatResult(result);
    } catch (Exception e) {
      log.error("Error in queryAccountInfo tool", e);
      return "Error querying account info: " + e.getMessage();
    }
  }

  @Tool("Query user profile information including name, email, and phone for the current user.")
  public String queryUserInfo() {
    try {
      String userId = getUserId();
      if (userId == null) {
        return "Error: User context not available";
      }
      Map<String, Object> parameters = new HashMap<>();
      parameters.put("userId", userId);
      Map<String, Object> result =
          actionExecutorService.executeAction("query_user_info", userId, parameters);
      return formatResult(result);
    } catch (Exception e) {
      log.error("Error in queryUserInfo tool", e);
      return "Error querying user info: " + e.getMessage();
    }
  }

  @Tool("Query cryptocurrency portfolio including holdings and total value for the current user.")
  public String queryCryptoPortfolio() {
    try {
      String userId = getUserId();
      if (userId == null) {
        return "Error: User context not available";
      }
      Map<String, Object> parameters = new HashMap<>();
      parameters.put("userId", userId);
      Map<String, Object> result =
          actionExecutorService.executeAction("query_crypto_portfolio", userId, parameters);
      return formatResult(result);
    } catch (Exception e) {
      log.error("Error in queryCryptoPortfolio tool", e);
      return "Error querying crypto portfolio: " + e.getMessage();
    }
  }

  private String formatResult(Map<String, Object> result) {
    if (result == null || result.isEmpty()) {
      return "No data available";
    }

    StringBuilder sb = new StringBuilder();
    if (result.containsKey("success") && Boolean.TRUE.equals(result.get("success"))) {
      sb.append("Success: ");
    }

    // Format based on result type
    if (result.containsKey("balance")) {
      sb.append("Balance: ").append(result.get("balance"));
    } else if (result.containsKey("transactions")) {
      Object transactions = result.get("transactions");
      sb.append("Transactions: ").append(transactions);
      if (result.containsKey("count")) {
        sb.append(" (Count: ").append(result.get("count")).append(")");
      }
    } else if (result.containsKey("accounts")) {
      Object accounts = result.get("accounts");
      sb.append("Accounts: ").append(accounts);
      if (result.containsKey("count")) {
        sb.append(" (Count: ").append(result.get("count")).append(")");
      }
    } else if (result.containsKey("user")) {
      sb.append("User Info: ").append(result.get("user"));
    } else if (result.containsKey("portfolio")) {
      sb.append("Portfolio: ").append(result.get("portfolio"));
      if (result.containsKey("holdings")) {
        sb.append(", Holdings: ").append(result.get("holdings"));
      }
    } else {
      sb.append(result.toString());
    }

    return sb.toString();
  }
}
