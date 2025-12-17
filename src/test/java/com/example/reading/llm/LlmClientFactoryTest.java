package com.example.reading.llm;

import com.example.reading.credential.Provider;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for LlmClientFactory.
 */
class LlmClientFactoryTest {

    private LlmClientFactory factory;

    @BeforeEach
    void setUp() {
        factory = new LlmClientFactory();
    }

    @Test
    void createOpenAiModelShouldReturnNonNull() {
        ChatLanguageModel model = factory.createChatModel(
                Provider.OPENAI,
                "sk-test-key",
                LlmModelOptions.builder()
                .build()
        );

        assertThat(model).isNotNull();
    }

    @Test
    void createAnthropicModelShouldReturnNonNull() {
        ChatLanguageModel model = factory.createChatModel(
                Provider.ANTHROPIC,
                "sk-ant-test-key",
                LlmModelOptions.builder()
                .build()
        );

        assertThat(model).isNotNull();
    }

    @Test
    void createLlamaModelShouldReturnNonNull() {
        ChatLanguageModel model = factory.createChatModel(
                Provider.LLAMA,
                "http://localhost:11434",
                LlmModelOptions.builder()
                .build()
        );

        assertThat(model).isNotNull();
    }

    @Test
    void createModelWithCustomOptionsShouldWork() {
        LlmModelOptions options = LlmModelOptions.builder()
                .model("gpt-4")
                .temperature(0.7)
                .build();

        ChatLanguageModel model = factory.createChatModel(
                Provider.OPENAI,
                "sk-test-key",
                options
        );

        assertThat(model).isNotNull();
    }

    @Test
    void createModelWithNullProviderShouldThrow() {
        assertThatThrownBy(() -> factory.createChatModel(null, "sk-test", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Provider cannot be null");
    }

    @Test
    void createModelWithBlankApiKeyShouldThrow() {
        assertThatThrownBy(() -> factory.createChatModel(Provider.OPENAI, "", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("API key cannot be null or blank");
    }

    @Test
    void createModelWithNullApiKeyShouldThrow() {
        assertThatThrownBy(() -> factory.createChatModel(Provider.OPENAI, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("API key cannot be null or blank");
    }

    @Test
    void createModelWithWhitespaceApiKeyShouldThrow() {
        assertThatThrownBy(() -> factory.createChatModel(Provider.OPENAI, "   ", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("API key cannot be null or blank");
    }

    @Test
    void createModelWithNullOptionsShouldUseDefaults() {
        // Should not throw, should use default options
        ChatLanguageModel model = factory.createChatModel(
                Provider.OPENAI,
                "sk-test-key",
                null
        );

        assertThat(model).isNotNull();
    }
}

