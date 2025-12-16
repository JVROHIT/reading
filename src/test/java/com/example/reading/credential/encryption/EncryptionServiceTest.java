package com.example.reading.credential.encryption;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for EncryptionService.
 */
class EncryptionServiceTest {

    // Valid 32-byte key encoded as Base64
    private static final String TEST_KEY_BASE64 = Base64.getEncoder().encodeToString(
            "this-is-a-32-byte-key-for-test!!".getBytes()
    );

    private EncryptionService encryptionService;

    @BeforeEach
    void setUp() {
        EncryptionProperties properties = new EncryptionProperties();
        properties.setKeyBase64(TEST_KEY_BASE64);
        encryptionService = new EncryptionService(properties);
    }

    @Test
    void encryptAndDecryptShouldRoundtrip() {
        String plaintext = "sk-my-secret-api-key-12345";

        String encrypted = encryptionService.encrypt(plaintext);
        String decrypted = encryptionService.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(plaintext);
    }

    @Test
    void encryptShouldProduceDifferentOutputsForSamePlaintext() {
        String plaintext = "same-plaintext";

        String encrypted1 = encryptionService.encrypt(plaintext);
        String encrypted2 = encryptionService.encrypt(plaintext);

        // Different IVs should produce different ciphertexts
        assertThat(encrypted1).isNotEqualTo(encrypted2);

        // But both should decrypt to the same plaintext
        assertThat(encryptionService.decrypt(encrypted1)).isEqualTo(plaintext);
        assertThat(encryptionService.decrypt(encrypted2)).isEqualTo(plaintext);
    }

    @Test
    void encryptShouldHandleEmptyString() {
        String plaintext = "";

        String encrypted = encryptionService.encrypt(plaintext);
        String decrypted = encryptionService.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(plaintext);
    }

    @Test
    void encryptShouldHandleUnicodeCharacters() {
        String plaintext = "API密钥🔐émojis";

        String encrypted = encryptionService.encrypt(plaintext);
        String decrypted = encryptionService.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(plaintext);
    }

    @Test
    void encryptShouldHandleLongStrings() {
        String plaintext = "x".repeat(10000);

        String encrypted = encryptionService.encrypt(plaintext);
        String decrypted = encryptionService.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(plaintext);
    }

    @Test
    void encryptShouldThrowForNullInput() {
        assertThatThrownBy(() -> encryptionService.encrypt(null))
                .isInstanceOf(EncryptionException.class)
                .hasMessageContaining("null");
    }

    @Test
    void decryptShouldThrowForNullInput() {
        assertThatThrownBy(() -> encryptionService.decrypt(null))
                .isInstanceOf(EncryptionException.class)
                .hasMessageContaining("null");
    }

    @Test
    void decryptShouldThrowForEmptyInput() {
        assertThatThrownBy(() -> encryptionService.decrypt(""))
                .isInstanceOf(EncryptionException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void decryptShouldThrowForInvalidBase64() {
        assertThatThrownBy(() -> encryptionService.decrypt("not-valid-base64!!!"))
                .isInstanceOf(EncryptionException.class)
                .hasMessageContaining("Decryption failed");
    }

    @Test
    void decryptShouldThrowForTamperedData() {
        String plaintext = "secret-data";
        String encrypted = encryptionService.encrypt(plaintext);

        // Tamper with the encrypted data
        byte[] bytes = Base64.getDecoder().decode(encrypted);
        bytes[bytes.length - 1] ^= 0xFF; // Flip bits in auth tag
        String tampered = Base64.getEncoder().encodeToString(bytes);

        assertThatThrownBy(() -> encryptionService.decrypt(tampered))
                .isInstanceOf(EncryptionException.class)
                .hasMessageContaining("Decryption failed");
    }

    @Test
    void decryptShouldThrowForTooShortData() {
        // Less than IV length (12 bytes)
        String tooShort = Base64.getEncoder().encodeToString(new byte[10]);

        assertThatThrownBy(() -> encryptionService.decrypt(tooShort))
                .isInstanceOf(EncryptionException.class)
                .hasMessageContaining("too short");
    }

    @Test
    void decryptWithWrongKeyShouldFail() {
        String plaintext = "secret-data";
        String encrypted = encryptionService.encrypt(plaintext);

        // Create service with different key
        EncryptionProperties differentKeyProps = new EncryptionProperties();
        differentKeyProps.setKeyBase64(Base64.getEncoder().encodeToString(
                "another-32-byte-key-for-test!!!!".getBytes()
        ));
        EncryptionService differentKeyService = new EncryptionService(differentKeyProps);

        assertThatThrownBy(() -> differentKeyService.decrypt(encrypted))
                .isInstanceOf(EncryptionException.class)
                .hasMessageContaining("Decryption failed");
    }

    @Test
    void propertiesShouldFailForMissingKey() {
        EncryptionProperties properties = new EncryptionProperties();
        properties.setKeyBase64(null);

        assertThatThrownBy(properties::getDecodedKey)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not configured");
    }

    @Test
    void propertiesShouldFailForInvalidBase64Key() {
        EncryptionProperties properties = new EncryptionProperties();
        properties.setKeyBase64("not-valid-base64!!!");

        assertThatThrownBy(properties::getDecodedKey)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not valid Base64");
    }

    @Test
    void propertiesShouldFailForWrongKeyLength() {
        EncryptionProperties properties = new EncryptionProperties();
        // 16 bytes instead of 32
        properties.setKeyBase64(Base64.getEncoder().encodeToString("short-key-16byte".getBytes()));

        assertThatThrownBy(properties::getDecodedKey)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32 bytes");
    }
}

