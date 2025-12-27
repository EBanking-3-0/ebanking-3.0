package com.ebanking.assistant.config.provider;

import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Google Gemini provider implementation for ChatLanguageModel. Supports Gemini Pro and other Gemini
 * models. NOTE: Gemini dependency (langchain4j-google-ai-gemini) not included in build.gradle. This
 * provider will be unavailable until the dependency is added.
 */
@Slf4j
@Component
public class GeminiChatModelProvider implements ChatModelProvider {

  @Override
  public ChatLanguageModel createModel(
      String apiKey, String model, Double temperature, Integer maxTokens) {
    log.warn(
        "Gemini provider is not available. Please add langchain4j-google-ai-gemini dependency.");
    throw new UnsupportedOperationException(
        "Gemini provider requires langchain4j-google-ai-gemini dependency");
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
      log.debug("Gemini provider not available: langchain4j-google-ai-gemini dependency not found");
      return false;
    }
  }
}
