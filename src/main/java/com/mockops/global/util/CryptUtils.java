package com.mockops.global.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * AES-256 암호화/복호화 유틸리티
 * RefreshToken, WebhookSecret 등 민감한 데이터를 DB에 저장할 때 사용
 */
@Slf4j
@Component
public class CryptUtils {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEY_ALGORITHM = "AES";

    @Value("${jwt.secret}")
    private String secretKey;

    /**
     * 문자열 암호화
     */
    public String encrypt(String plainText) {
        try {
            SecretKeySpec keySpec = generateKey();
            IvParameterSpec ivSpec = generateIv();

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("암호화 실패", e);
            throw new RuntimeException("암호화에 실패했습니다.", e);
        }
    }

    /**
     * 문자열 복호화
     */
    public String decrypt(String encryptedText) {
        try {
            SecretKeySpec keySpec = generateKey();
            IvParameterSpec ivSpec = generateIv();

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("복호화 실패", e);
            throw new RuntimeException("복호화에 실패했습니다.", e);
        }
    }

    /**
     * 32바이트 AES 키 생성 (SHA-256 해시 사용)
     */
    private SecretKeySpec generateKey() throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] key = digest.digest(secretKey.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(key, KEY_ALGORITHM);
    }

    /**
     * 16바이트 IV 생성 (고정 IV - 단순화를 위해, 프로덕션에서는 랜덤 IV 권장)
     */
    private IvParameterSpec generateIv() throws Exception {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        byte[] iv = digest.digest(secretKey.getBytes(StandardCharsets.UTF_8));
        return new IvParameterSpec(iv);
    }
}
