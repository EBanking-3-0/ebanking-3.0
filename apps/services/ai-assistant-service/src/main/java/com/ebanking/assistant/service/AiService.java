package com.ebanking.assistant.service;

import com.ebanking.assistant.action.ActionRegistry;
import com.ebanking.assistant.config.LangChainConfig;
import com.ebanking.assistant.model.ChatResponse;
import com.ebanking.assistant.producer.AssistantEventProducer;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatLanguageModel;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

  private final ChatLanguageModel chatLanguageModel;
  private final ChatMemoryProvider chatMemoryProvider;
  private final ActionExecutorService actionExecutorService;
  private final ActionRegistry actionRegistry;
  private final AssistantEventProducer eventProducer;
  private final LangChainConfig.BankingAssistantAiService bankingAssistantAiService;

  private static final String SYSTEM_PROMPT =
      """
        You are a helpful banking assistant for E-Banking 3.0. Your role is to assist customers with banking queries and execute banking actions.

        Available actions you can perform:
        - query_balance: Query account balance (requires accountId)
        - query_transactions: Query transaction history (requires accountId or userId, optional limit)
        - query_account_info: Query account information (requires accountId or userId)
        - query_user_info: Query user profile information (requires userId or email)
        - query_crypto_portfolio: Query cryptocurrency portfolio (requires userId)

        When a user asks about their banking information, you should:
        1. Identify which action(s) are needed
        2. Extract the required parameters from the conversation
        3. Execute the action(s) to get the data
        4. Present the results in a clear, user-friendly manner

        Always be polite, professional, and ensure data privacy. Only access information for the authenticated user.
        If you don't have enough information to execute an action, ask the user for clarification.
        """;
    
    public ChatResponse processMessage(String userMessage, Long userId, String memoryId) {
        try {
            // Set userId in ThreadLocal context for tools to access
            com.ebanking.assistant.tool.BankingTools.setUserId(userId);
            
            try {
                // Use the AI service with tool calling capabilities
                // The tools are automatically available and will be called by the LLM when needed
                String aiResponse = bankingAssistantAiService.chat(userMessage);
                
                // Analyze the message to determine intent (for logging/analytics)
                String intent = classifyIntent(userMessage);
                String actionToExecute = determineAction(userMessage, intent);
                
                // Note: With tool calling, actions are executed automatically by the LLM
                // We still track the intent for analytics purposes
                Map<String, Object> actionResult = new HashMap<>();
                if (actionToExecute != null) {
                    actionResult.put("intent", actionToExecute);
                }
                
                return ChatResponse.builder()
                        .response(aiResponse)
                        .intent(intent)
                        .actionExecuted(actionToExecute)
                        .actionResult(actionResult)
                        .build();
            } finally {
                // Clear ThreadLocal to prevent memory leaks
                com.ebanking.assistant.tool.BankingTools.clearUserId();
            }
                    
        } catch (Exception e) {
            log.error("Error processing message", e);
            eventProducer.publishErrorOccurred(userId, "MESSAGE_PROCESSING_ERROR", 
                    e.getMessage(), memoryId);
            return ChatResponse.builder()
                    .response("I apologize, but I encountered an error processing your request. Please try again.")
                    .build();
        }
    }
    
    private String classifyIntent(String message) {
        String lowerMessage = message.toLowerCase();
        
        if (lowerMessage.contains("balance") || lowerMessage.contains("how much")) {
            return "query_balance";
        } else if (lowerMessage.contains("transaction") || lowerMessage.contains("history") || 
                   lowerMessage.contains("payment") || lowerMessage.contains("transfer")) {
            return "query_transactions";
        } else if (lowerMessage.contains("account") && (lowerMessage.contains("info") || lowerMessage.contains("detail"))) {
            return "query_account_info";
        } else if (lowerMessage.contains("profile") || lowerMessage.contains("user info") || 
                   lowerMessage.contains("my information")) {
            return "query_user_info";
        } else if (lowerMessage.contains("crypto") || lowerMessage.contains("bitcoin") || 
                   lowerMessage.contains("portfolio")) {
            return "query_crypto_portfolio";
        }
        
        return "general_query";
    }
    
    private String determineAction(String message, String intent) {
        if (!intent.equals("general_query") && actionRegistry.hasAction(intent)) {
            return intent;
        }
        return null;
    }
    
    private Map<String, Object> extractParameters(String message, String actionName, Long userId) {
        Map<String, Object> parameters = new HashMap<>();
        
        // Simple parameter extraction - in production, use more sophisticated NLP
        String lowerMessage = message.toLowerCase();
        
        // Extract account ID if mentioned
        java.util.regex.Pattern accountIdPattern = java.util.regex.Pattern.compile("account[\\s#]*(\\d+)", 
                java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher accountMatcher = accountIdPattern.matcher(message);
        if (accountMatcher.find()) {
            try {
                parameters.put("accountId", Long.parseLong(accountMatcher.group(1)));
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
        
        // Extract limit if mentioned
        java.util.regex.Pattern limitPattern = java.util.regex.Pattern.compile("(?:last|recent|limit)[\\s]*(\\d+)", 
                java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher limitMatcher = limitPattern.matcher(message);
        if (limitMatcher.find()) {
            try {
                parameters.put("limit", Integer.parseInt(limitMatcher.group(1)));
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
        
        // Default userId if not specified
        if (!parameters.containsKey("userId") && !parameters.containsKey("accountId")) {
            parameters.put("userId", userId);
        }
        
        return parameters;
    }
    
    private String formatActionResultForAI(String actionName, Map<String, Object> result) {
        if (result == null || result.isEmpty()) {
            return "No data available";
        }
        
        // Format the result in a way that's useful for the AI to generate a response
        StringBuilder sb = new StringBuilder();
        sb.append("Action: ").append(actionName).append("\n");
        
        if (result.containsKey("success") && Boolean.TRUE.equals(result.get("success"))) {
            sb.append("Status: Success\n");
        }
        
        // Add relevant data based on action type
        if (result.containsKey("balance")) {
            sb.append("Balance: ").append(result.get("balance"));
        } else if (result.containsKey("transactions")) {
            sb.append("Transactions: ").append(result.get("transactions"));
        } else if (result.containsKey("accounts")) {
            sb.append("Accounts: ").append(result.get("accounts"));
        } else if (result.containsKey("user")) {
            sb.append("User Info: ").append(result.get("user"));
        } else if (result.containsKey("portfolio")) {
            sb.append("Portfolio: ").append(result.get("portfolio"));
        }
        
        return sb.toString();
    }
}
