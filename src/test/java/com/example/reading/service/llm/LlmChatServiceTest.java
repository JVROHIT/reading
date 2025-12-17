package com.example.reading.service.llm;

import com.example.reading.auth.CurrentUserService;
import com.example.reading.credential.CredentialService;
import com.example.reading.credential.Provider;
import com.example.reading.credential.dto.CredentialResponse;
import com.example.reading.credential.exception.CredentialNotFoundException;
import com.example.reading.llm.LlmClientFactory;
import com.example.reading.llm.LlmModelOptions;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for LlmChatService.
 */
@ExtendWith(MockitoExtension.class)
class LlmChatServiceTest {

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private CredentialService credentialService;

    @Mock
    private LlmClientFactory llmClientFactory;

    private LlmChatService llmChatService;

    private static final String USER_ID = "user-123";
    private static final String DECRYPTED_API_KEY = "sk-test-api-key";

    @BeforeEach
    void setUp() {
        llmChatService = new LlmChatService(currentUserService, credentialService, llmClientFactory);
    }

    @Test
    void chatShouldReturnResponseWhenSingleCredentialExists() {
        // Given
        CredentialResponse credential = CredentialResponse.builder()
                .id("cred-1")
                .provider(Provider.OPENAI)
                .build();

        ChatLanguageModel mockModel = mock(ChatLanguageModel.class);
        when(mockModel.generate(any(String.class))).thenReturn("LLM response");

        when(currentUserService.getCurrentUserId()).thenReturn(USER_ID);
        when(credentialService.getCredentials(USER_ID)).thenReturn(List.of(credential));
        when(credentialService.getDecryptedApiKey(USER_ID, Provider.OPENAI)).thenReturn(DECRYPTED_API_KEY);
        when(llmClientFactory.createChatModel(eq(Provider.OPENAI), eq(DECRYPTED_API_KEY), any(LlmModelOptions.class)))
                .thenReturn(mockModel);

        // When
        String result = llmChatService.chat("System prompt", "User prompt", null);

        // Then
        assertThat(result).isEqualTo("LLM response");
        verify(llmClientFactory).createChatModel(eq(Provider.OPENAI), eq(DECRYPTED_API_KEY), any(LlmModelOptions.class));
    }

    @Test
    void chatShouldUseSpecifiedProviderWhenMultipleCredentialsExist() {
        // Given
        CredentialResponse openaiCred = CredentialResponse.builder()
                .id("cred-1")
                .provider(Provider.OPENAI)
                .build();
        CredentialResponse anthropicCred = CredentialResponse.builder()
                .id("cred-2")
                .provider(Provider.ANTHROPIC)
                .build();

        ChatLanguageModel mockModel = mock(ChatLanguageModel.class);
        when(mockModel.generate(any(String.class))).thenReturn("Anthropic response");

        when(currentUserService.getCurrentUserId()).thenReturn(USER_ID);
        when(credentialService.getCredentials(USER_ID)).thenReturn(List.of(openaiCred, anthropicCred));
        when(credentialService.getDecryptedApiKey(USER_ID, Provider.ANTHROPIC)).thenReturn(DECRYPTED_API_KEY);
        when(llmClientFactory.createChatModel(eq(Provider.ANTHROPIC), eq(DECRYPTED_API_KEY), any(LlmModelOptions.class)))
                .thenReturn(mockModel);

        // When
        String result = llmChatService.chat("System prompt", "User prompt", "ANTHROPIC");

        // Then
        assertThat(result).isEqualTo("Anthropic response");
        verify(llmClientFactory).createChatModel(eq(Provider.ANTHROPIC), eq(DECRYPTED_API_KEY), any(LlmModelOptions.class));
    }

    @Test
    void chatShouldThrowNoCredentialConfiguredExceptionWhenNoCredentials() {
        // Given
        when(currentUserService.getCurrentUserId()).thenReturn(USER_ID);
        when(credentialService.getCredentials(USER_ID)).thenReturn(Collections.emptyList());

        // When/Then
        assertThatThrownBy(() -> llmChatService.chat("System", "User", null))
                .isInstanceOf(NoCredentialConfiguredException.class)
                .hasMessageContaining("No LLM credentials configured");

        verify(llmClientFactory, never()).createChatModel(any(), any(), any());
    }

    @Test
    void chatShouldThrowAmbiguousCredentialExceptionWhenMultipleCredentialsAndNoProviderSpecified() {
        // Given
        CredentialResponse cred1 = CredentialResponse.builder().id("cred-1").provider(Provider.OPENAI).build();
        CredentialResponse cred2 = CredentialResponse.builder().id("cred-2").provider(Provider.ANTHROPIC).build();

        when(currentUserService.getCurrentUserId()).thenReturn(USER_ID);
        when(credentialService.getCredentials(USER_ID)).thenReturn(List.of(cred1, cred2));

        // When/Then
        assertThatThrownBy(() -> llmChatService.chat("System", "User", null))
                .isInstanceOf(AmbiguousCredentialException.class)
                .hasMessageContaining("Multiple credentials found");

        verify(llmClientFactory, never()).createChatModel(any(), any(), any());
    }

    @Test
    void chatShouldThrowExceptionWhenSpecifiedProviderNotConfigured() {
        // Given
        CredentialResponse cred = CredentialResponse.builder().id("cred-1").provider(Provider.OPENAI).build();

        when(currentUserService.getCurrentUserId()).thenReturn(USER_ID);
        when(credentialService.getCredentials(USER_ID)).thenReturn(List.of(cred));

        // When/Then
        assertThatThrownBy(() -> llmChatService.chat("System", "User", "ANTHROPIC"))
                .isInstanceOf(NoCredentialConfiguredException.class)
                .hasMessageContaining("No credential configured for provider: ANTHROPIC");
    }

    @Test
    void chatShouldThrowExceptionWhenInvalidProviderName() {
        // Given
        CredentialResponse cred = CredentialResponse.builder().id("cred-1").provider(Provider.OPENAI).build();

        when(currentUserService.getCurrentUserId()).thenReturn(USER_ID);
        when(credentialService.getCredentials(USER_ID)).thenReturn(List.of(cred));

        // When/Then
        assertThatThrownBy(() -> llmChatService.chat("System", "User", "INVALID_PROVIDER"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid provider");
    }

    @Test
    void chatShouldWorkWithLlamaProvider() {
        // Given
        CredentialResponse credential = CredentialResponse.builder()
                .id("cred-1")
                .provider(Provider.LLAMA)
                .build();

        ChatLanguageModel mockModel = mock(ChatLanguageModel.class);
        when(mockModel.generate(any(String.class))).thenReturn("Llama response");

        when(currentUserService.getCurrentUserId()).thenReturn(USER_ID);
        when(credentialService.getCredentials(USER_ID)).thenReturn(List.of(credential));
        when(credentialService.getDecryptedApiKey(USER_ID, Provider.LLAMA)).thenReturn(DECRYPTED_API_KEY);
        when(llmClientFactory.createChatModel(eq(Provider.LLAMA), eq(DECRYPTED_API_KEY), any(LlmModelOptions.class)))
                .thenReturn(mockModel);

        // When
        String result = llmChatService.chat("System prompt", "User prompt", null);

        // Then
        assertThat(result).isEqualTo("Llama response");
    }

    @Test
    void chatShouldPropagateCredentialNotFoundException() {
        // Given
        CredentialResponse credential = CredentialResponse.builder()
                .id("cred-1")
                .provider(Provider.OPENAI)
                .build();

        when(currentUserService.getCurrentUserId()).thenReturn(USER_ID);
        when(credentialService.getCredentials(USER_ID)).thenReturn(List.of(credential));
        when(credentialService.getDecryptedApiKey(USER_ID, Provider.OPENAI))
                .thenThrow(new CredentialNotFoundException("OPENAI"));

        // When/Then
        assertThatThrownBy(() -> llmChatService.chat("System", "User", null))
                .isInstanceOf(CredentialNotFoundException.class);
    }

    @Test
    void chatShouldHandleCaseInsensitiveProviderName() {
        // Given
        CredentialResponse credential = CredentialResponse.builder()
                .id("cred-1")
                .provider(Provider.OPENAI)
                .build();

        ChatLanguageModel mockModel = mock(ChatLanguageModel.class);
        when(mockModel.generate(any(String.class))).thenReturn("Response");

        when(currentUserService.getCurrentUserId()).thenReturn(USER_ID);
        when(credentialService.getCredentials(USER_ID)).thenReturn(List.of(credential));
        when(credentialService.getDecryptedApiKey(USER_ID, Provider.OPENAI)).thenReturn(DECRYPTED_API_KEY);
        when(llmClientFactory.createChatModel(eq(Provider.OPENAI), eq(DECRYPTED_API_KEY), any(LlmModelOptions.class)))
                .thenReturn(mockModel);

        // When - lowercase provider name
        String result = llmChatService.chat("System", "User", "openai");

        // Then
        assertThat(result).isEqualTo("Response");
    }
}

