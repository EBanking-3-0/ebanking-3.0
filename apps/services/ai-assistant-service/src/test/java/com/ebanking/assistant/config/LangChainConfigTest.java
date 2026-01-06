package com.ebanking.assistant.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ebanking.assistant.config.provider.ChatModelProvider;
import com.ebanking.assistant.config.provider.ChatModelProviderFactory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.junit.jupiter.api.Test;

class LangChainConfigTest {

  @Test
  void usesFallbackWhenApiKeyMissing() {
    AiModelProperties props = new AiModelProperties();
    props.setApiKey("");
    props.setProvider("openai");
    props.setModel("gpt-4o-mini");

    ChatModelProviderFactory factory = mock(ChatModelProviderFactory.class);

    LangChainConfig config = new LangChainConfig(props, factory);

    ChatLanguageModel model = config.chatLanguageModel();

    String response = model.generate("hi");
    assertEquals(
        "I'm a test AI assistant. Configure AI_ASSISTANT provider/model and OPENAI_API_KEY (or other provider key) for full capabilities.",
        response);
    verifyNoInteractions(factory);
  }

  @Test
  void selectsProviderFromFactory() {
    AiModelProperties props = new AiModelProperties();
    props.setApiKey("key");
    props.setProvider("openai");
    props.setModel("gpt-4o");
    props.setTemperature(0.5);
    props.setMaxTokens(256);

    ChatModelProvider provider = mock(ChatModelProvider.class);
    ChatLanguageModel expectedModel = mock(ChatLanguageModel.class);
    when(provider.createModel(eq("key"), eq("gpt-4o"), eq(0.5), eq(256))).thenReturn(expectedModel);

    ChatModelProviderFactory factory = mock(ChatModelProviderFactory.class);
    when(factory.getProvider("openai")).thenReturn(provider);

    LangChainConfig config = new LangChainConfig(props, factory);

    ChatLanguageModel model = config.chatLanguageModel();

    assertSame(expectedModel, model);
    verify(factory).getProvider("openai");
    verify(provider).createModel(eq("key"), eq("gpt-4o"), eq(0.5), eq(256));
    verify(provider, never()).getProviderName();
  }
}
