package dev.xuya.ldcshop.common.util;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-GCM 加密解密工具类 / AES-GCM Encryption/Decryption Utility
 * 用于卡密内容的加解密，密文以 "ENC:" 前缀 + Base64 编码存储
 *
 * @author xuya
 */
@Slf4j
public class CryptoUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private final SecretKeySpec keySpec;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * 构造加密工具 / Construct CryptoUtil with secret key
     *
     * @param secret 加密密钥（至少32字节）/ Encryption secret (at least 32 bytes)
     */
    public CryptoUtil(String secret) {
        byte[] key = new byte[32];
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(secretBytes, 0, key, 0, Math.min(secretBytes.length, 32));
        this.keySpec = new SecretKeySpec(key, "AES");
    }

    /**
     * 加密明文 / Encrypt plaintext
     * 输出格式: "ENC:" + Base64(IV + ciphertext + tag)
     *
     * @param plaintext 明文 / Plaintext
     * @return 密文字符串 / Ciphertext string
     */
    public String encrypt(String plaintext) {
        if (StrUtil.isBlank(plaintext)) return plaintext;
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
            return "ENC:" + Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new IllegalStateException("Encryption failed", e);
        }
    }

    /**
     * 解密密文 / Decrypt ciphertext
     * 仅处理以 "ENC:" 开头的密文
     *
     * @param ciphertext 密文 / Ciphertext
     * @return 解密后的明文 / Decrypted plaintext
     */
    public String decrypt(String ciphertext) {
        if (StrUtil.isBlank(ciphertext)) return ciphertext;
        if (!ciphertext.startsWith("ENC:")) {
            throw new IllegalStateException("Invalid ciphertext: missing ENC: prefix");
        }
        try {
            byte[] combined = Base64.getDecoder().decode(ciphertext.substring(4));
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, iv.length);
            byte[] encrypted = new byte[combined.length - iv.length];
            System.arraycopy(combined, iv.length, encrypted, 0, encrypted.length);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Decryption failed", e);
        }
    }
}
