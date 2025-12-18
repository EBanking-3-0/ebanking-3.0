package com.ebanking.assistant.action;

import java.util.Map;

/**
 * Base interface for all banking actions that can be executed by the AI assistant.
 */
public interface ActionExecutor {
    
    /**
     * Get the name of this action (e.g., "query_balance", "query_transactions")
     */
    String getActionName();
    
    /**
     * Get a description of what this action does (used for AI tool definitions)
     */
    String getDescription();
    
    /**
     * Execute the action with the given parameters
     * 
     * @param userId The user ID executing the action
     * @param parameters Action-specific parameters
     * @return Result of the action execution
     * @throws ActionExecutionException if the action fails
     */
    Map<String, Object> execute(Long userId, Map<String, Object> parameters) throws ActionExecutionException;
    
    /**
     * Check if the user has permission to execute this action
     * 
     * @param userId The user ID
     * @param parameters Action parameters
     * @return true if authorized, false otherwise
     */
    default boolean isAuthorized(Long userId, Map<String, Object> parameters) {
        return true; // Default: all actions are authorized (can be overridden)
    }
}
