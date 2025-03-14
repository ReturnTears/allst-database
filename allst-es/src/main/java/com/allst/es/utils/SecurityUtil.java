package com.allst.es.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;

public class SecurityUtil {
    private static final String ALGORITHM = "AES";
    private static final String SECRET_KEY = "your_aes_secret_key"; // AES密钥（实际使用中应妥善保管）
    private static final String SIGN_ALGORITHM = "SHA-256";
    private static final String SIGN_SECRET_KEY = "your_sign_secret_key"; // 签名密钥

    // AES加密
    public static String encrypt(String data, String key) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    // AES解密
    public static String decrypt(String encryptedData, String key) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        byte[] decodedData = Base64.getDecoder().decode(encryptedData);
        byte[] decryptedData = cipher.doFinal(decodedData);
        return new String(decryptedData, StandardCharsets.UTF_8);
    }

    // 生成签名
    public static String generateSignature(Map<String, String> params) throws NoSuchAlgorithmException {
        // 将参数按键的字典序排序
        TreeMap<String, String> sortedParams = new TreeMap<>(params);
        // 拼接参数和密钥
        StringBuilder sb = new StringBuilder();
        sortedParams.forEach((key, value) -> sb.append(key).append("=").append(value).append("&"));
        sb.append("secret_key=").append(SECRET_KEY);
        // 生成签名
        MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
        byte[] hash = digest.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
        // 将字节数组转换为十六进制字符串
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
