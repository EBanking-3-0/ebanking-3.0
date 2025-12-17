package com.ebanking.assistant.action.actions;

import com.ebanking.assistant.action.ActionExecutionException;
import com.ebanking.assistant.action.ActionExecutor;
import com.ebanking.assistant.client.AccountServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueryBalanceAction implements ActionExecutor {
    
    private final AccountServiceClient accountServiceClient;
    
    @Override
    public String getActionName() {
        return "query_balance";
    }
    
    @Override
    public String getDescription() {
        return "Query the balance of a bank account. Requires accountId parameter.";
    }
    
    @Override
    public Map<String, Object> execute(String userId, Map<String, Object> parameters) throws ActionExecutionException {
        try {
            Object accountIdObj = parameters.get("accountId");
            if (accountIdObj == null) {
                throw new ActionExecutionException("accountId parameter is required");
            }
            
            Long accountId;
            if (accountIdObj instanceof Number) {
                accountId = ((Number) accountIdObj).longValue();
            } else {
                accountId = Long.parseLong(accountIdObj.toString());
            }
            
            log.info("Querying balance for account {} for user {}", accountId, userId);
            Map<String, Object> balance = accountServiceClient.getAccountBalance(accountId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("accountId", accountId);
            result.put("balance", balance);
            result.put("success", true);
            
            return result;
        } catch (Exception e) {
            log.error("Error querying balance", e);
            throw new ActionExecutionException("Failed to query balance: " + e.getMessage(), e);
        }
    }
}
