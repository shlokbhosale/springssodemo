package com.example.springssodemo.security;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * SecretManager: AES-GCM encrypt/decrypt with master key loaded from env/properties.
 * Fails fast if master key missing or invalid.
 */
@Component
public class SecretManager {

    private static final int IV_LENGTH = 12; // bytes
    private static final int TAG_BITS = 128;

    private final String masterKeyBase64;
    private SecretKeySpec keySpec;

    public SecretManager(@Value("${app.masterKey:}") String masterKeyBase64) {
        this.masterKeyBase64 = masterKeyBase64;
    }

    @PostConstruct
    private void init() {
        if (masterKeyBase64 == null || masterKeyBase64.isBlank()) {
            throw new IllegalStateException("app.masterKey is not configured. Set APP_MASTER_KEY environment variable.");
        }
        byte[] key;
        try {
            key = Base64.getDecoder().decode(masterKeyBase64);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("app.masterKey is not a valid Base64 string", ex);
        }
        if (key.length != 32) {
            throw new IllegalStateException("app.masterKey must decode to 32 bytes (256-bit AES key).");
        }
        this.keySpec = new SecretKeySpec(key, "AES");
    }

    public String encrypt(String plain) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            byte[] iv = new byte[IV_LENGTH];
            SecureRandom.getInstanceStrong().nextBytes(iv);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec);
            byte[] cipherText = cipher.doFinal(plain.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            ByteBuffer bb = ByteBuffer.allocate(iv.length + cipherText.length);
            bb.put(iv);
            bb.put(cipherText);
            return Base64.getEncoder().encodeToString(bb.array());
        } catch (Exception e) {
            throw new IllegalStateException("Secret encryption failed", e);
        }
    }

    public String decrypt(String blob) {
        try {
            byte[] data = Base64.getDecoder().decode(blob);
            if (data.length < IV_LENGTH + 1) throw new IllegalArgumentException("invalid_encrypted_blob");
            byte[] iv = java.util.Arrays.copyOfRange(data, 0, IV_LENGTH);
            byte[] ct = java.util.Arrays.copyOfRange(data, IV_LENGTH, data.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(TAG_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, spec);
            byte[] plain = cipher.doFinal(ct);
            return new String(plain, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Secret decryption failed", e);
        }
    }
}
