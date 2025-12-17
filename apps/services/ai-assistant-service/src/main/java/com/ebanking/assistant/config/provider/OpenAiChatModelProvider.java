package com.ebanking.assistant.config.provider;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * OpenAI provider implementation for ChatLanguageModel.
 * Supports GPT-3.5, GPT-4, and other OpenAI models.
 */
@Slf4j
@Component
public class OpenAiChatModelProvider implements ChatModelProvider {

    @Override
    public ChatLanguageModel createModel(String apiKey, String model, Double temperature, Integer maxTokens) {
        log.info("Configuring OpenAI model: {}", model);

        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(model)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .build();
    }

    @Override
    public String getProviderName() {
        return "openai";
    }

    @Override
    public boolean isAvailable() {
        try {
            Class.forName("dev.langchain4j.model.openai.OpenAiChatModel");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
