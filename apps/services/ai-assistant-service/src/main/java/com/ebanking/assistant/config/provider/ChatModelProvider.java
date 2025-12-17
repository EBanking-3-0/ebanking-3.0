package com.ebanking.assistant.config.provider;

import dev.langchain4j.model.chat.ChatLanguageModel;

/**
 * Interface for different AI chat model providers.
 * Allows modular support for multiple AI providers (OpenAI, Gemini, etc.)
 */
public interface ChatModelProvider {
    
    /**
     * Creates and returns a ChatLanguageModel instance.
     * @param apiKey The API key for the provider
     * @param model The model name/identifier
     * @param temperature Temperature parameter for model
     * @param maxTokens Maximum tokens for response
     * @return Configured ChatLanguageModel instance
     */
    ChatLanguageModel createModel(String apiKey, String model, Double temperature, Integer maxTokens);
    
    /**
     * Get the provider name (e.g., "openai", "gemini")
     */
    String getProviderName();
    
    /**
     * Check if this provider is available (dependencies present)
     */
    boolean isAvailable();
}
