package com.ebanking.assistant.config;

import com.ebanking.assistant.tool.BankingTools;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class LangChainConfig {
    
    @Value("${ai.assistant.provider:openai}")
    private String provider;
    
    @Value("${ai.assistant.model:gpt-4-turbo-preview}")
    private String model;
    
    @Value("${ai.assistant.api-key:}")
    private String apiKey;
    
    @Value("${ai.assistant.temperature:0.7}")
    private Double temperature;
    
    @Value("${ai.assistant.max-tokens:1000}")
    private Integer maxTokens;
    
    @Bean
    public ChatLanguageModel chatLanguageModel() {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("OpenAI API key not configured. Using mock/fallback model for testing.");
            // Return a simple mock model for testing without API key
            return new ChatLanguageModel() {
                @Override
                public String generate(String userMessage) {
                    return "I'm a test AI assistant. To use full AI capabilities, please configure OPENAI_API_KEY environment variable.";
                }
                
                @Override
                public dev.langchain4j.model.output.Response<dev.langchain4j.data.message.AiMessage> generate(
                        java.util.List<dev.langchain4j.data.message.ChatMessage> messages) {
                    dev.langchain4j.data.message.AiMessage aiMessage = dev.langchain4j.data.message.AiMessage.from(
                        "I'm a test AI assistant. To use full AI capabilities, please configure OPENAI_API_KEY environment variable."
                    );
                    return new dev.langchain4j.model.output.Response<>(aiMessage);
                }
            };
        }
        
        // Map model string to enum, default to GPT-4 Turbo
        OpenAiChatModelName modelName;
        try {
            modelName = OpenAiChatModelName.valueOf(model.toUpperCase().replace("-", "_"));
        } catch (IllegalArgumentException e) {
            // Default to GPT-4 Turbo if model name not found
            log.warn("Model {} not found, using GPT_4_TURBO_PREVIEW", model);
            modelName = OpenAiChatModelName.GPT_4_TURBO_PREVIEW;
        }
        
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .build();
    }
    
    @Bean
    public ChatMemoryProvider chatMemoryProvider() {
        return memoryId -> MessageWindowChatMemory.withMaxMessages(20);
    }
    
    /**
     * Creates an AI service with banking tools.
     * The tools from BankingTools are automatically discovered and made available to the LLM.
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
     * Interface for the AI assistant service with tool calling capabilities.
     * The LLM can call the tools defined in BankingTools.
     */
    public interface BankingAssistantAiService {
        String chat(String userMessage);
    }
}
