package com.ebanking.assistant.config;

import com.ebanking.assistant.config.provider.ChatModelProviderFactory;
import com.ebanking.assistant.tool.BankingTools;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class LangChainConfig {

  @Value("${ai.assistant.provider:gemini}")
  private String provider;

  @Value("${ai.assistant.model:gemini-2.0-flash}")
  private String model;

  @Value("${ai.assistant.api-key:}")
  private String apiKey;

  @Value("${ai.assistant.temperature:0.7}")
  private Double temperature;

  @Value("${ai.assistant.max-tokens:1000}")
  private Integer maxTokens;

  private final ChatModelProviderFactory providerFactory;

  public LangChainConfig(ChatModelProviderFactory providerFactory) {
    this.providerFactory = providerFactory;
  }

  @Bean
  public ChatLanguageModel chatLanguageModel() {
    if (apiKey == null || apiKey.isEmpty()) {
      log.warn("AI API key not configured. Using mock/fallback model for testing.");
      // Return a simple mock model for testing without API key
      return new ChatLanguageModel() {
        @Override
        public String generate(String userMessage) {
          return "I'm a test AI assistant. To use full AI capabilities, please configure an API key:\n"
              + "- For Gemini: export GOOGLE_API_KEY=your-key\n"
              + "- For OpenAI: export OPENAI_API_KEY=your-key";
        }

        @Override
        public dev.langchain4j.model.output.Response<dev.langchain4j.data.message.AiMessage>
            generate(java.util.List<dev.langchain4j.data.message.ChatMessage> messages) {
          dev.langchain4j.data.message.AiMessage aiMessage =
              dev.langchain4j.data.message.AiMessage.from(
                  "I'm a test AI assistant. To use full AI capabilities, please configure an API key.");
          return new dev.langchain4j.model.output.Response<>(aiMessage);
        }
      };
    }

    try {
      log.info("Initializing AI model with provider: {}, model: {}", provider, model);
      return providerFactory
          .getProvider(provider)
          .createModel(apiKey, model, temperature, maxTokens);
    } catch (Exception e) {
      log.error("Failed to initialize AI model", e);
      throw new RuntimeException("Failed to initialize AI model: " + e.getMessage(), e);
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
}
