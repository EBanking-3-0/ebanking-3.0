package com.ebanking.assistant.config;

import com.ebanking.assistant.config.provider.ChatModelProvider;
import com.ebanking.assistant.config.provider.ChatModelProviderFactory;
import com.ebanking.assistant.tool.BankingTools;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class LangChainConfig {

  private final AiModelProperties aiModelProperties;
  private final ChatModelProviderFactory chatModelProviderFactory;

  @Bean
  public ChatLanguageModel chatLanguageModel() {
    if (aiModelProperties.getApiKey() == null || aiModelProperties.getApiKey().isBlank()) {
      log.warn("AI API key not configured. Using mock/fallback model for testing.");
      return fallbackModel();
    }

    ChatModelProvider provider =
        chatModelProviderFactory.getProvider(aiModelProperties.getProvider());

    log.info(
        "Creating ChatLanguageModel provider={} model={}",
        aiModelProperties.getProvider(),
        aiModelProperties.getModel());

    try {
      return provider.createModel(
          aiModelProperties.getApiKey(),
          aiModelProperties.getModel(),
          aiModelProperties.getTemperature(),
          aiModelProperties.getMaxTokens());
    } catch (RuntimeException ex) {
      log.error("Failed to create chat model for provider {}", provider.getProviderName(), ex);
      throw ex;
    }
  }

  @Bean
  public ChatMemoryProvider chatMemoryProvider() {
    return memoryId -> MessageWindowChatMemory.withMaxMessages(20);
  }

  /**
   * Creates an AI service with banking tools. The tools from BankingTools are automatically
   * discovered and made available to the LLM.
   */
  @Bean
  public BankingAssistantAiService bankingAssistantAiService(
      ChatLanguageModel chatLanguageModel,
      ChatMemoryProvider chatMemoryProvider,
      BankingTools bankingTools) {

    return AiServices.builder(BankingAssistantAiService.class)
        .chatLanguageModel(chatLanguageModel)
        .chatMemoryProvider(chatMemoryProvider)
        .tools(bankingTools)
        .build();
  }

  /**
   * Interface for the AI assistant service with tool calling capabilities. The LLM can call the
   * tools defined in BankingTools.
   */
  public interface BankingAssistantAiService {
    String chat(String userMessage);
  }

  private ChatLanguageModel fallbackModel() {
    return new ChatLanguageModel() {
      @Override
      public String generate(String userMessage) {
        return fallbackMessage();
      }

      @Override
      public dev.langchain4j.model.output.Response<dev.langchain4j.data.message.AiMessage> generate(
          java.util.List<dev.langchain4j.data.message.ChatMessage> messages) {
        dev.langchain4j.data.message.AiMessage aiMessage =
            dev.langchain4j.data.message.AiMessage.from(fallbackMessage());
        return new dev.langchain4j.model.output.Response<>(aiMessage);
      }
    };
  }

  private String fallbackMessage() {
    return "I'm a test AI assistant. Configure AI_ASSISTANT provider/model and OPENAI_API_KEY (or other provider key) for full capabilities.";
  }
}
