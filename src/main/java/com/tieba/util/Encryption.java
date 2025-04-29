package com.tieba.util;

import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 加密工具
 */
@Slf4j
public class Encryption {

    /**
     * 对字符串进行 MD5 加密
     *
     * @param str 需要加密的字符串
     * @return 加密后的字符串
     */
    public static String enCodeMd5(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            return new BigInteger(1, md.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            log.error("MD5加密出错 - {}", e.getMessage());
            return "";
        }
    }
} 