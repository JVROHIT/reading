package com.example.reading.credential.encryption;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Service for encrypting and decrypting strings using AES-256-GCM.
 * 
 * <p>Encryption format: Base64(IV || Ciphertext || AuthTag)</p>
 * <ul>
 *   <li>IV: 12 bytes (random per encryption)</li>
 *   <li>Auth Tag: 128 bits (16 bytes)</li>
 * </ul>
 */
@Service
public class EncryptionService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12; // 96 bits recommended for GCM
    private static final int TAG_LENGTH_BITS = 128; // Authentication tag length

    private final SecretKeySpec secretKey;
    private final SecureRandom secureRandom;

    public EncryptionService(EncryptionProperties properties) {
        byte[] keyBytes = properties.getDecodedKey();
        this.secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
        this.secureRandom = new SecureRandom();
    }

    /**
     * Encrypts a plaintext string using AES-256-GCM.
     *
     * @param plaintext the string to encrypt
     * @return Base64-encoded ciphertext (IV prepended)
     * @throws EncryptionException if encryption fails
     */
    public String encrypt(String plaintext) {
        if (plaintext == null) {
            throw new EncryptionException("Cannot encrypt null value");
        }

        try {
            // Generate random IV
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);

            // Initialize cipher
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);

            // Encrypt
            byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);
            byte[] ciphertext = cipher.doFinal(plaintextBytes);

            // Combine IV + Ciphertext (GCM appends auth tag to ciphertext)
            ByteBuffer buffer = ByteBuffer.allocate(IV_LENGTH + ciphertext.length);
            buffer.put(iv);
            buffer.put(ciphertext);

            // Encode as Base64
            return Base64.getEncoder().encodeToString(buffer.array());

        } catch (Exception e) {
            throw new EncryptionException("Encryption failed", e);
        }
    }

    /**
     * Decrypts a Base64-encoded ciphertext that was encrypted with {@link #encrypt(String)}.
     *
     * @param encryptedBase64 the Base64-encoded ciphertext (IV prepended)
     * @return the decrypted plaintext string
     * @throws EncryptionException if decryption fails (invalid data, wrong key, tampered)
     */
    public String decrypt(String encryptedBase64) {
        if (encryptedBase64 == null || encryptedBase64.isBlank()) {
            throw new EncryptionException("Cannot decrypt null or empty value");
        }

        try {
            // Decode from Base64
            byte[] combined = Base64.getDecoder().decode(encryptedBase64);

            if (combined.length < IV_LENGTH + 1) {
                throw new EncryptionException("Invalid encrypted data: too short");
            }

            // Extract IV and ciphertext
            ByteBuffer buffer = ByteBuffer.wrap(combined);
            byte[] iv = new byte[IV_LENGTH];
            buffer.get(iv);
            byte[] ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);

            // Initialize cipher
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);

            // Decrypt
            byte[] plaintextBytes = cipher.doFinal(ciphertext);
            return new String(plaintextBytes, StandardCharsets.UTF_8);

        } catch (EncryptionException e) {
            throw e;
        } catch (Exception e) {
            throw new EncryptionException("Decryption failed", e);
        }
    }
}

