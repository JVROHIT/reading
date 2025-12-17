package com.example.reading.llm;

import com.example.reading.credential.Provider;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.anthropic.AnthropicChatModel.AnthropicChatModelBuilder;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel.OpenAiChatModelBuilder;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Factory for creating LangChain4j ChatLanguageModel instances based on
 * provider.
 */
@Component
public class LlmClientFactory {

    private static final String HUGGING_FACE_ROUTER_BASE_URL = "https://router.huggingface.co/v1";
    private static final String DEFAULT_LLAMA_MODEL = "meta-llama/Llama-3.1-8B-Instruct";
    private static final String DEFAULT_OPENAI_MODEL = "gpt-4o-mini";
    private static final String DEFAULT_ANTHROPIC_MODEL = "claude-3-5-sonnet-latest";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    /**
     * Creates a ChatLanguageModel for the given provider.
     *
     * @param provider the LLM provider
     * @param apiKey   the decrypted API key
     * @param options  optional model configuration
     * @return a configured ChatLanguageModel
     * @throws IllegalArgumentException if provider is null or apiKey is blank
     */
    public ChatLanguageModel createChatModel(Provider provider, String apiKey, LlmModelOptions options) {
        if (provider == null) {
            throw new IllegalArgumentException("Provider cannot be null");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("API key cannot be null or blank");
        }

        if (options == null) {
            options = LlmModelOptions.builder().build();
        }

        return switch (provider) {
            case OPENAI -> createOpenAiModel(apiKey, options);
            case ANTHROPIC -> createAnthropicModel(apiKey, options);
            case LLAMA -> createLlamaModel(apiKey, options);
        };
    }

    private ChatLanguageModel createOpenAiModel(String apiKey, LlmModelOptions options) {
        OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(options.getModel() != null ? options.getModel() : DEFAULT_OPENAI_MODEL)
                .timeout(options.getTimeout() != null ? options.getTimeout() : DEFAULT_TIMEOUT);

        if (options.getTemperature() != null) {
            builder.temperature(options.getTemperature());
        }

        // TODO: maxTokens is not directly supported by OpenAiChatModel builder
        // Would need to use maxOutputTokens or similar if available in LangChain4j
        // version

        return builder.build();
    }

    private ChatLanguageModel createAnthropicModel(String apiKey, LlmModelOptions options) {
        AnthropicChatModelBuilder builder = AnthropicChatModel.builder()
                .apiKey(apiKey)
                .modelName(options.getModel() != null ? options.getModel() : DEFAULT_ANTHROPIC_MODEL)
                .timeout(options.getTimeout() != null ? options.getTimeout() : DEFAULT_TIMEOUT);

        if (options.getTemperature() != null) {
            builder.temperature(options.getTemperature());
        }

        // TODO: maxTokens support depends on LangChain4j Anthropic builder API
        // Check if maxTokens() or maxOutputTokens() method exists

        return builder.build();
    }

    private ChatLanguageModel createLlamaModel(String apiKey, LlmModelOptions options) {
        OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(options.getBaseUrl() != null ? options.getBaseUrl() : HUGGING_FACE_ROUTER_BASE_URL)
                .modelName(options.getModel() != null ? options.getModel() : DEFAULT_LLAMA_MODEL)
                .timeout(options.getTimeout() != null ? options.getTimeout() : DEFAULT_TIMEOUT);

        if (options.getTemperature() != null) {
            builder.temperature(options.getTemperature());
        }

        // TODO: maxTokens is not directly supported by OpenAiChatModel builder
        // Would need to use maxOutputTokens or similar if available in LangChain4j
        // version

        return builder.build();
    }
}
