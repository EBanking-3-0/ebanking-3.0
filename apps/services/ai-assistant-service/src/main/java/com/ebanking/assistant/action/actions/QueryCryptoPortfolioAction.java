package com.ebanking.assistant.action.actions;

import com.ebanking.assistant.action.ActionExecutionException;
import com.ebanking.assistant.action.ActionExecutor;
import com.ebanking.assistant.client.CryptoServiceClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueryCryptoPortfolioAction implements ActionExecutor {
    
    private final CryptoServiceClient cryptoServiceClient;
    
    @Override
    public String getActionName() {
        return "query_crypto_portfolio";
    }
    
    @Override
    public String getDescription() {
        return "Query cryptocurrency portfolio including holdings and total value. Requires userId parameter (defaults to current user).";
    }
    
    @Override
    public Map<String, Object> execute(Long userId, Map<String, Object> parameters) throws ActionExecutionException {
        try {
            Object userIdObj = parameters.get("userId");
            
            Long targetUserId = userId;
            if (userIdObj != null) {
                if (userIdObj instanceof Number) {
                    targetUserId = ((Number) userIdObj).longValue();
                } else {
                    targetUserId = Long.parseLong(userIdObj.toString());
                }
            }
            
            log.info("Querying crypto portfolio for user {}", targetUserId);
            
            Map<String, Object> portfolio = cryptoServiceClient.getPortfolioByUserId(targetUserId);
            List<Map<String, Object>> holdings = cryptoServiceClient.getHoldingsByUserId(targetUserId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("portfolio", portfolio);
            result.put("holdings", holdings);
            result.put("holdingsCount", holdings != null ? holdings.size() : 0);
            result.put("success", true);
            
            return result;
        } catch (Exception e) {
            log.error("Error querying crypto portfolio", e);
            throw new ActionExecutionException("Failed to query crypto portfolio: " + e.getMessage(), e);
        }
    }
}
