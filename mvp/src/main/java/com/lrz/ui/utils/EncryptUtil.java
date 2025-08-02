package com.lrz.ui.utils;


import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class EncryptUtil {
    /**
     * 将字符串md5加密
     * @param data String
     * @param key String
     * @return String
     */
    public static String encryptHmacMD5ToString(final String data, final String key) {
        if (data == null || data.isEmpty() || key == null || key.isEmpty()) return "";
        return encryptHmacMD5ToString(data.getBytes(), key.getBytes());
    }

    public static String encryptMD5ToString(final String data) {
        if (data == null || data.isEmpty()) return "";
        return encryptMD5ToString(data.getBytes());
    }

    public static String encryptMD5ToString(final byte[] data) {
        return bytes2HexString(encryptMD5(data));
    }

    public static byte[] encryptMD5(final byte[] data) {
        return hashTemplate(data);
    }

    static byte[] hashTemplate(final byte[] data) {
        if (data == null || data.length == 0) return null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(data);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     *将字符串sha256加密
     * @param data 字符
     * @param key key
     * @return String
     */
    public static String encryptHmacSHA256ToString(final String data, final String key) {
        if (data == null || data.isEmpty() || key == null || key.isEmpty()) return "";
        return encryptHmacSHA256ToString(data.getBytes(), key.getBytes());
    }
    public static String encryptHmacMD5ToString(final byte[] data, final byte[] key) {
        return bytes2HexString(encryptHmacMD5(data, key));
    }

    public static String encryptHmacSHA256ToString(final byte[] data, final byte[] key) {
        return bytes2HexString(encryptHmacSHA256(data, key));
    }

    public static byte[] encryptHmacSHA256(final byte[] data, final byte[] key) {
        return hmacTemplate(data, key, "HmacSHA256");
    }

    /**
     * 将字节数组转换成string
     * @param bytes 字节数组
     * @return string
     */
    public static String bytes2HexString(final byte[] bytes) {
        return Util.Str.bytes2HexString(bytes);
    }

    public static byte[] encryptHmacMD5(final byte[] data, final byte[] key) {
        return hmacTemplate(data, key, "HmacMD5");
    }

    private static byte[] hmacTemplate(final byte[] data,
                                       final byte[] key,
                                       final String algorithm) {
        if (data == null || data.length == 0 || key == null || key.length == 0) return null;
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key, algorithm);
            Mac mac = Mac.getInstance(algorithm);
            mac.init(secretKey);
            return mac.doFinal(data);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
