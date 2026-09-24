package com.flighttracker.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

public class EncryptionUtil {

    private static SecretKeySpec getKey(String secret) {
        try {
            byte[] key = secret.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance(AppConstants.HASH_ALGORITHM_SHA256);
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16); // 128-bit key
            return new SecretKeySpec(key, AppConstants.AES_ALGORITHM);
        } catch (Exception e) {
            throw new RuntimeException("Error initializing encryption key", e);
        }
    }

    public static String encrypt(String strToEncrypt, String secret) {
        if (strToEncrypt == null || strToEncrypt.trim().isEmpty()) {
            return "";
        }
        try {
            SecretKeySpec secretKey = getKey(secret != null ? secret : AppConstants.DEFAULT_SECRET_KEY);
            Cipher cipher = Cipher.getInstance(AppConstants.AES_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encrypted = cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            System.err.println("Error encrypting string: " + e.getMessage());
            return strToEncrypt;
        }
    }

    public static String decrypt(String strToDecrypt, String secret) {
        if (strToDecrypt == null || strToDecrypt.trim().isEmpty()) {
            return "";
        }
        try {
            SecretKeySpec secretKey = getKey(secret != null ? secret : AppConstants.DEFAULT_SECRET_KEY);
            Cipher cipher = Cipher.getInstance(AppConstants.AES_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(strToDecrypt));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // If decryption fails (e.g. legacy plain text string), return original string as fallback
            return strToDecrypt;
        }
    }
}
