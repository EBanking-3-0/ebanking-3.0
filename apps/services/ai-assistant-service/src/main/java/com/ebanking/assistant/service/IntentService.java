package com.ebanking.assistant.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for intent classification and routing. This is a simplified implementation - in
 * production, use ML models or more sophisticated NLP.
 */
@Slf4j
@Service
public class IntentService {

  public String classifyIntent(String message) {
    String lowerMessage = message.toLowerCase();

    if (lowerMessage.contains("balance") || lowerMessage.contains("how much")) {
      return "query_balance";
    } else if (lowerMessage.contains("transaction")
        || lowerMessage.contains("history")
        || lowerMessage.contains("payment")
        || lowerMessage.contains("transfer")) {
      return "query_transactions";
    } else if (lowerMessage.contains("account")
        && (lowerMessage.contains("info") || lowerMessage.contains("detail"))) {
      return "query_account_info";
    } else if (lowerMessage.contains("profile")
        || lowerMessage.contains("user info")
        || lowerMessage.contains("my information")) {
      return "query_user_info";
    } else if (lowerMessage.contains("crypto")
        || lowerMessage.contains("bitcoin")
        || lowerMessage.contains("portfolio")) {
      return "query_crypto_portfolio";
    }

    return "general_query";
  }
}
