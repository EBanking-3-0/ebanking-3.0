package com.ebanking.assistant.config.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Factory for selecting the appropriate ChatModelProvider.
 * Manages registration and selection of available AI providers.
 */
@Slf4j
@Component
public class ChatModelProviderFactory {
    
    private final Map<String, ChatModelProvider> providers;
    
    public ChatModelProviderFactory(List<ChatModelProvider> providerList) {
        this.providers = providerList.stream()
                .collect(Collectors.toMap(ChatModelProvider::getProviderName, p -> p));
        
        log.info("Registered {} AI providers: {}", providers.size(), providers.keySet());
    }
    
    /**
     * Get a provider by name.
     * @param providerName The provider name (e.g., "openai", "gemini")
     * @return The ChatModelProvider instance
     * @throws IllegalArgumentException if provider not found
     */
    public ChatModelProvider getProvider(String providerName) {
        ChatModelProvider provider = providers.get(providerName.toLowerCase());
        
        if (provider == null) {
            throw new IllegalArgumentException(
                String.format("Provider '%s' not found. Available providers: %s", 
                    providerName, providers.keySet())
            );
        }
        
        if (!provider.isAvailable()) {
            throw new IllegalStateException(
                String.format("Provider '%s' is not available. Missing dependencies?", providerName)
            );
        }
        
        return provider;
    }
    
    /**
     * Get all available providers.
     */
    public List<String> getAvailableProviders() {
        return providers.values().stream()
                .filter(ChatModelProvider::isAvailable)
                .map(ChatModelProvider::getProviderName)
                .collect(Collectors.toList());
    }
}
