package com.ebanking.assistant.action.actions;

import com.ebanking.assistant.action.ActionExecutionException;
import com.ebanking.assistant.action.ActionExecutor;
import com.ebanking.assistant.client.PaymentServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueryTransactionsAction implements ActionExecutor {
    
    private final PaymentServiceClient paymentServiceClient;
    
    @Override
    public String getActionName() {
        return "query_transactions";
    }
    
    @Override
    public String getDescription() {
        return "Query transaction history for an account or user. Requires accountId or userId parameter, and optional limit (default 10).";
    }
    
    @Override
    public Map<String, Object> execute(String userId, Map<String, Object> parameters) throws ActionExecutionException {
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
            Object limitObj = parameters.get("limit");
            
            Integer limit = 10;
            if (limitObj != null) {
                if (limitObj instanceof Number) {
                    limit = ((Number) limitObj).intValue();
                } else {
                    limit = Integer.parseInt(limitObj.toString());
                }
            }
            
            List<Map<String, Object>> transactions;
            
            if (accountIdObj != null) {
                Long accountId;
                if (accountIdObj instanceof Number) {
                    accountId = ((Number) accountIdObj).longValue();
                } else {
                    accountId = Long.parseLong(accountIdObj.toString());
                }
                
                log.info("Querying transactions for account {} with limit {} for user {}", accountId, limit, userId);
                transactions = paymentServiceClient.getTransactionsByAccountId(accountId, limit);
            } else if (userIdObj != null) {
                Long targetUserId;
                if (userIdObj instanceof Number) {
                    targetUserId = ((Number) userIdObj).longValue();
                } else {
                    targetUserId = Long.parseLong(userIdObj.toString());
                }
                
                log.info("Querying transactions for user {} with limit {}", targetUserId, limit);
                transactions = paymentServiceClient.getTransactionsByUserId(targetUserId, limit);
            } else {
                // Default to current user
                if (userIdLong == null) {
                    throw new ActionExecutionException("Cannot query transactions: userId is not numeric and no accountId/userId parameter provided");
                }
                log.info("Querying transactions for user {} with limit {}", userIdLong, limit);
                transactions = paymentServiceClient.getTransactionsByUserId(userIdLong, limit);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("transactions", transactions);
            result.put("count", transactions != null ? transactions.size() : 0);
            result.put("limit", limit);
            result.put("success", true);
            
            return result;
        } catch (Exception e) {
            log.error("Error querying transactions", e);
            throw new ActionExecutionException("Failed to query transactions: " + e.getMessage(), e);
        }
    }
}
