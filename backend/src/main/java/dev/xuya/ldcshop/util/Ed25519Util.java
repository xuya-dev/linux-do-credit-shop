package dev.xuya.ldcshop.util;

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

    /**
     * 生成 Ed25519 密钥对 / Generate Ed25519 Key Pair
     *
     * @return 密钥对 / Key Pair
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
     *
     * @param privateKeyBase64 Base64编码的私钥 / Base64 encoded private key
     * @param data             待签名数据 / Data to sign
     * @return Base64编码的签名 / Base64 encoded signature
     */
    public static String sign(String privateKeyBase64, String data) {
        try {
            byte[] privateKeyBytes = Base64.decode(privateKeyBase64);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            PrivateKey privateKey = KeyFactory.getInstance(ALGORITHM).generatePrivate(keySpec);

            Signature signature = Signature.getInstance(ALGORITHM);
            signature.initSign(privateKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));
            return Base64.encode(signature.sign());
        } catch (Exception e) {
            log.error("Ed25519签名失败 / Ed25519 signing failed", e);
            throw new BusinessException(ResultCode.PAY_SIGN_ERROR);
        }
    }

    /**
     * 使用公钥验证签名 / Verify signature with public key
     *
     * @param publicKeyBase64  Base64编码的公钥 / Base64 encoded public key
     * @param data             原始数据 / Original data
     * @param signatureBase64  Base64编码的签名 / Base64 encoded signature
     * @return 验签结果 / Verification result
     */
    public static boolean verify(String publicKeyBase64, String data, String signatureBase64) {
        try {
            byte[] publicKeyBytes = Base64.decode(publicKeyBase64);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            PublicKey publicKey = KeyFactory.getInstance(ALGORITHM).generatePublic(keySpec);

            Signature signature = Signature.getInstance(ALGORITHM);
            signature.initVerify(publicKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));
            return signature.verify(Base64.decode(signatureBase64));
        } catch (Exception e) {
            log.error("Ed25519验签失败 / Ed25519 verification failed", e);
            return false;
        }
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
