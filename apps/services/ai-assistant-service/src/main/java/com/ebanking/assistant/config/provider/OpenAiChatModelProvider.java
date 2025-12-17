package com.ebanking.assistant.config.provider;

import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.extern.slf4j.Slf4j;

/**
 * OpenAI provider implementation for ChatLanguageModel.
 * Supports GPT-3.5, GPT-4, and other OpenAI models.
 * DISABLED: Using Gemini only for now, but keeping modular architecture for future extensions.
 */
@Slf4j
// @Component - Disabled: uncomment and add openai dependency to enable
public class OpenAiChatModelProvider implements ChatModelProvider {

    @Override
    public ChatLanguageModel createModel(String apiKey, String model, Double temperature, Integer maxTokens) {
        throw new UnsupportedOperationException("OpenAI provider is disabled. Use Gemini provider instead.");
    }

    @Override
    public String getProviderName() {
        return "openai";
    }

    @Override
    public boolean isAvailable() {
        return false; // Disabled
    }
}
