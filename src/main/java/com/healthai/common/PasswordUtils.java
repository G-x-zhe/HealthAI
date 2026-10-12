package com.healthai.common;

import cn.hutool.crypto.digest.DigestUtil;

public class PasswordUtils {

    // MD5 加密（32 位十六进制）
    public static String encrypt(String password) {
        return DigestUtil.md5Hex(password);
    }

    // 校验：把明文加密后与库中密文比较
    public static boolean matches(String raw, String encoded) {
        return encrypt(raw).equals(encoded);
    }
}
