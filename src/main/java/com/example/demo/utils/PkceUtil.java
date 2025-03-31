package com.example.demo.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PkceUtil {

    // 生成滿足規範的隨機  code_verifier (長度43~128的字母數字和安全字符組成的字符)
    public static String generateCodeVerifier(){
        // 使用安全隨機源生成32字節隨機數據
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        String s = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        return s;
    }


    // 基於給定的 code_verifier 計算 S256 方法的  code_challenge

    public static String generateCodeChallenge(String codeVerifier){

        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] hash = sha256.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
            // 將哈希值進行URL安全的Base64編碼即為 code_challenge
            String s = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
            return s;


        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 Algorithm not found",e);
        }


    }


    // 生成隨機 state  參數
    public static String generateState(){
        byte[] randomBytes = new byte[16];
        new SecureRandom().nextBytes(randomBytes);
        String s = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        return s;
    }


}
