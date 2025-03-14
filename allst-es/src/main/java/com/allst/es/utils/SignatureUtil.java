package com.allst.es.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;

public class SignatureUtil {
    private static final String ALGORITHM = "SHA-256";
    private static final String SECRET_KEY = "your_secret_key"; // 预定义的密钥

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

    // 验证签名
    public static boolean verifySignature(Map<String, String> params, String signature) throws NoSuchAlgorithmException {
        String generatedSignature = generateSignature(params);
        return generatedSignature.equals(signature);
    }
}
