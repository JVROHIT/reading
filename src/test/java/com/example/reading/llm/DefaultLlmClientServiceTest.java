package com.example.reading.llm;

import com.example.reading.credential.CredentialService;
import com.example.reading.credential.Provider;
import com.example.reading.credential.exception.CredentialNotFoundException;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for DefaultLlmClientService.
 */
@ExtendWith(MockitoExtension.class)
class DefaultLlmClientServiceTest {

    @Mock
    private CredentialService credentialService;

    @Mock
    private LlmClientFactory llmClientFactory;

    private DefaultLlmClientService llmClientService;

    private static final String USER_ID = "user-123";
    private static final String DECRYPTED_API_KEY = "sk-test-api-key";

    @BeforeEach
    void setUp() {
        llmClientService = new DefaultLlmClientService(credentialService, llmClientFactory);
    }

    @Test
    void getChatModelShouldReturnModelWhenCredentialExists() {
        // Given
        ChatLanguageModel expectedModel = mock(ChatLanguageModel.class);
        LlmModelOptions options = LlmModelOptions.builder().build();

        when(credentialService.getDecryptedApiKey(USER_ID, Provider.OPENAI))
                .thenReturn(DECRYPTED_API_KEY);
        when(llmClientFactory.createChatModel(Provider.OPENAI, DECRYPTED_API_KEY, options))
                .thenReturn(expectedModel);

        // When
        ChatLanguageModel result = llmClientService.getChatModel(USER_ID, Provider.OPENAI, options);

        // Then
        assertThat(result).isSameAs(expectedModel);
        verify(credentialService).getDecryptedApiKey(USER_ID, Provider.OPENAI);
        verify(llmClientFactory).createChatModel(Provider.OPENAI, DECRYPTED_API_KEY, options);
    }

    @Test
    void getChatModelShouldWorkWithNullOptions() {
        // Given
        ChatLanguageModel expectedModel = mock(ChatLanguageModel.class);

        when(credentialService.getDecryptedApiKey(USER_ID, Provider.ANTHROPIC))
                .thenReturn(DECRYPTED_API_KEY);
        when(llmClientFactory.createChatModel(eq(Provider.ANTHROPIC), eq(DECRYPTED_API_KEY), eq(null)))
                .thenReturn(expectedModel);

        // When
        ChatLanguageModel result = llmClientService.getChatModel(USER_ID, Provider.ANTHROPIC, null);

        // Then
        assertThat(result).isSameAs(expectedModel);
    }

    @Test
    void getChatModelShouldThrowCredentialNotFoundExceptionWhenNoCredential() {
        // Given
        when(credentialService.getDecryptedApiKey(USER_ID, Provider.OPENAI))
                .thenThrow(new CredentialNotFoundException(Provider.OPENAI.name()));

        // When/Then
        assertThatThrownBy(() -> llmClientService.getChatModel(USER_ID, Provider.OPENAI, null))
                .isInstanceOf(CredentialNotFoundException.class)
                .hasMessageContaining("OPENAI");

        verify(llmClientFactory, never()).createChatModel(any(), any(), any());
    }

    @Test
    void getChatModelShouldThrowIllegalArgumentExceptionWhenUserIdIsNull() {
        assertThatThrownBy(() -> llmClientService.getChatModel(null, Provider.OPENAI, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User ID cannot be null or blank");

        verify(credentialService, never()).getDecryptedApiKey(any(), any());
        verify(llmClientFactory, never()).createChatModel(any(), any(), any());
    }

    @Test
    void getChatModelShouldThrowIllegalArgumentExceptionWhenUserIdIsBlank() {
        assertThatThrownBy(() -> llmClientService.getChatModel("   ", Provider.OPENAI, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User ID cannot be null or blank");

        verify(credentialService, never()).getDecryptedApiKey(any(), any());
    }

    @Test
    void getChatModelShouldThrowIllegalArgumentExceptionWhenProviderIsNull() {
        assertThatThrownBy(() -> llmClientService.getChatModel(USER_ID, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Provider cannot be null");

        verify(credentialService, never()).getDecryptedApiKey(any(), any());
    }

    @Test
    void getChatModelShouldWorkForAllProviders() {
        // Test OPENAI
        ChatLanguageModel openAiModel = mock(ChatLanguageModel.class);
        when(credentialService.getDecryptedApiKey(USER_ID, Provider.OPENAI)).thenReturn(DECRYPTED_API_KEY);
        when(llmClientFactory.createChatModel(Provider.OPENAI, DECRYPTED_API_KEY, null)).thenReturn(openAiModel);
        assertThat(llmClientService.getChatModel(USER_ID, Provider.OPENAI, null)).isSameAs(openAiModel);

        // Test ANTHROPIC
        ChatLanguageModel anthropicModel = mock(ChatLanguageModel.class);
        when(credentialService.getDecryptedApiKey(USER_ID, Provider.ANTHROPIC)).thenReturn(DECRYPTED_API_KEY);
        when(llmClientFactory.createChatModel(Provider.ANTHROPIC, DECRYPTED_API_KEY, null)).thenReturn(anthropicModel);
        assertThat(llmClientService.getChatModel(USER_ID, Provider.ANTHROPIC, null)).isSameAs(anthropicModel);

        // Test LLAMA
        ChatLanguageModel llamaModel = mock(ChatLanguageModel.class);
        when(credentialService.getDecryptedApiKey(USER_ID, Provider.LLAMA)).thenReturn(DECRYPTED_API_KEY);
        when(llmClientFactory.createChatModel(Provider.LLAMA, DECRYPTED_API_KEY, null)).thenReturn(llamaModel);
        assertThat(llmClientService.getChatModel(USER_ID, Provider.LLAMA, null)).isSameAs(llamaModel);
    }

    @Test
    void getChatModelShouldPropagateFactoryExceptions() {
        // Given - factory throws IllegalArgumentException (e.g., for LLAMA with blank model)
        when(credentialService.getDecryptedApiKey(USER_ID, Provider.LLAMA))
                .thenReturn(DECRYPTED_API_KEY);
        when(llmClientFactory.createChatModel(eq(Provider.LLAMA), eq(DECRYPTED_API_KEY), any()))
                .thenThrow(new IllegalArgumentException("Model name required for LLAMA"));

        // When/Then
        assertThatThrownBy(() -> llmClientService.getChatModel(USER_ID, Provider.LLAMA, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Model name required");
    }
}

