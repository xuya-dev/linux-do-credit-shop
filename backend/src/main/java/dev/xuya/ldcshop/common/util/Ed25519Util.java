package dev.xuya.ldcshop.common.util;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import dev.xuya.ldcshop.common.ResultCode;
import dev.xuya.ldcshop.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

/**
 * Ed25519 签名工具类 / Ed25519 Signing Utility
 * 用于 LINUX DO Credit 支付接口的请求签名和回调验签
 *
 * @author xuya
 */
@Slf4j
public class Ed25519Util {

    /** Ed25519 算法名称 / Ed25519 Algorithm Name */
    private static final String ALGORITHM = "Ed25519";

    /** PKCS#8 DER prefix for Ed25519 private key (wraps a 32-byte raw seed into 46 bytes) */
    private static final byte[] ED25519_PKCS8_PREFIX = {
        0x30, 0x2e, 0x02, 0x01, 0x00, 0x30, 0x05, 0x06,
        0x03, 0x2b, 0x65, 0x70, 0x04, 0x22, 0x04, 0x20
    };

    /** X509 DER prefix for Ed25519 public key (wraps a 32-byte raw key into 44 bytes) */
    private static final byte[] ED25519_X509_PREFIX = {
        0x30, 0x2a, 0x30, 0x05, 0x06, 0x03, 0x2b, 0x65,
        0x70, 0x03, 0x21, 0x00
    };

    /**
     * 生成 Ed25519 密钥对 / Generate Ed25519 Key Pair
     */
    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGORITHM);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new BusinessException(ResultCode.PAY_SIGN_ERROR);
        }
    }

    /**
     * 使用私钥对数据进行签名 / Sign data with private key
     */
    public static String sign(String privateKeyBase64, String data) {
        try {
            byte[] rawDecoded = stripPemAndDecode(privateKeyBase64);
            log.debug("Ed25519 key decoded length: {} bytes", rawDecoded.length);
            byte[] privateKeyBytes = decodeAndWrapPrivate(privateKeyBase64);
            log.debug("Ed25519 key final length: {} bytes", privateKeyBytes.length);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            PrivateKey privateKey = KeyFactory.getInstance(ALGORITHM).generatePrivate(keySpec);

            Signature signature = Signature.getInstance(ALGORITHM);
            signature.initSign(privateKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));
            return Base64.encode(signature.sign());
        } catch (Exception e) {
            log.error("Ed25519 signing failed", e);
            throw new BusinessException(ResultCode.PAY_SIGN_ERROR);
        }
    }

    /**
     * 使用公钥验证签名 / Verify signature with public key
     */
    public static boolean verify(String publicKeyBase64, String data, String signatureBase64) {
        try {
            byte[] publicKeyBytes = decodeAndWrapPublic(publicKeyBase64);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            PublicKey publicKey = KeyFactory.getInstance(ALGORITHM).generatePublic(keySpec);

            Signature signature = Signature.getInstance(ALGORITHM);
            signature.initVerify(publicKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));
            return signature.verify(Base64.decode(signatureBase64));
        } catch (Exception e) {
            log.error("Ed25519 verification failed", e);
            return false;
        }
    }

    /**
     * 解码并包装 Ed25519 私钥为 PKCS#8 格式 / Decode and wrap Ed25519 private key to PKCS#8 format
     * 如果密钥为 32 字节原始格式，自动添加 PKCS#8 DER 前缀
     *
     * @param keyBase64 Base64 编码的私钥 / Base64 encoded private key
     * @return PKCS#8 格式的私钥字节 / PKCS#8 formatted private key bytes
     */
    private static byte[] decodeAndWrapPrivate(String keyBase64) {
        byte[] bytes = stripPemAndDecode(keyBase64);
        if (bytes.length == 32) {
            byte[] wrapped = new byte[ED25519_PKCS8_PREFIX.length + 32];
            System.arraycopy(ED25519_PKCS8_PREFIX, 0, wrapped, 0, ED25519_PKCS8_PREFIX.length);
            System.arraycopy(bytes, 0, wrapped, ED25519_PKCS8_PREFIX.length, 32);
            return wrapped;
        }
        return bytes;
    }

    /**
     * 解码并包装 Ed25519 公钥为 X509 格式 / Decode and wrap Ed25519 public key to X509 format
     * 如果密钥为 32 字节原始格式，自动添加 X509 DER 前缀
     *
     * @param keyBase64 Base64 编码的公钥 / Base64 encoded public key
     * @return X509 格式的公钥字节 / X509 formatted public key bytes
     */
    private static byte[] decodeAndWrapPublic(String keyBase64) {
        byte[] bytes = stripPemAndDecode(keyBase64);
        if (bytes.length == 32) {
            byte[] wrapped = new byte[ED25519_X509_PREFIX.length + 32];
            System.arraycopy(ED25519_X509_PREFIX, 0, wrapped, 0, ED25519_X509_PREFIX.length);
            System.arraycopy(bytes, 0, wrapped, ED25519_X509_PREFIX.length, 32);
            return wrapped;
        }
        return bytes;
    }

    /**
     * 去除 PEM 头尾标记并 Base64 解码 / Strip PEM headers and Base64 decode
     *
     * @param keyBase64 PEM 或 Base64 编码的密钥 / PEM or Base64 encoded key
     * @return 解码后的原始字节 / Decoded raw bytes
     */
    private static byte[] stripPemAndDecode(String keyBase64) {
        String cleaned = keyBase64
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replaceAll("\\s", "");
        return Base64.decode(cleaned);
    }

    /**
     * 构建待签名字符串 / Build string to be signed
     * 将所有非空参数（排除sign和sign_type）按ASCII字典序排列，
     * 然后使用 k1=v1&k2=v2 格式拼接，末尾追加 clientSecret
     *
     * @param params       请求参数 / Request parameters
     * @param clientSecret 应用密钥 / Client Secret
     * @return 待签名字符串 / String to be signed
     */
    public static String buildSignString(Map<String, ?> params, String clientSecret) {
        List<String> keys = new ArrayList<>();
        for (Map.Entry<String, ?> entry : params.entrySet()) {
            String key = entry.getKey();
            Object val = entry.getValue();
            if (val != null && StrUtil.isNotBlank(val.toString()) && !"sign".equals(key) && !"sign_type".equals(key)) {
                keys.add(key);
            }
        }
        Collections.sort(keys);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            if (i > 0) {
                sb.append("&");
            }
            sb.append(keys.get(i)).append("=").append(params.get(keys.get(i)));
        }
        sb.append(clientSecret);
        return sb.toString();
    }
}
