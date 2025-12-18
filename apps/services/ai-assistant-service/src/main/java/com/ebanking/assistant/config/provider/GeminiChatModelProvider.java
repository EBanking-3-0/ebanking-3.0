package com.ebanking.assistant.config.provider;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Google Gemini provider implementation for ChatLanguageModel. Supports Gemini Pro and other Gemini
 * models.
 */
@Slf4j
@Component
public class GeminiChatModelProvider implements ChatModelProvider {

  @Override
  public ChatLanguageModel createModel(
      String apiKey, String model, Double temperature, Integer maxTokens) {
    log.info("Configuring Gemini model: {}", model);

    return GoogleAiGeminiChatModel.builder()
        .apiKey(apiKey)
        .modelName(model)
        .temperature(temperature)
        .maxOutputTokens(maxTokens)
        .build();
  }

  @Override
  public String getProviderName() {
    return "gemini";
  }

  @Override
  public boolean isAvailable() {
    try {
      Class.forName("dev.langchain4j.model.googleai.GoogleAiGeminiChatModel");
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }
}
