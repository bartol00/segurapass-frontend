package com.example.passwordmanagerclient.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;

public final class PrivateKeyLoader {

    private static final int AES_KEY_SIZE = 256;
    private static final int PBKDF2_ITERATIONS = 65536;
    private static final int GCM_TAG_LENGTH = 128; // bits

    private PrivateKeyLoader() {}

    public static PrivateKey decryptPrivateKey(String cipherTextB64,
                                               String ivB64,
                                               String saltB64,
                                               String masterPassword) throws Exception {
        byte[] cipherText = Base64.getDecoder().decode(cipherTextB64);
        byte[] iv = Base64.getDecoder().decode(ivB64);
        byte[] salt = Base64.getDecoder().decode(saltB64);

        SecretKey aesKey = deriveKeyFromPassword(masterPassword.toCharArray(), salt);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, aesKey, gcmSpec);
        byte[] privateKeyDer = cipher.doFinal(cipherText);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyDer);
        KeyFactory kf = KeyFactory.getInstance("RSA");

        return kf.generatePrivate(keySpec);
    }

    public static String signNonceWithPrivateKey(PrivateKey privateKey, UUID nonce) throws Exception {
        byte[] message = nonce.toString().getBytes(StandardCharsets.UTF_8);

        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(privateKey);
        sig.update(message);
        byte[] signatureBytes = sig.sign();
        return Base64.getEncoder().encodeToString(signatureBytes);
    }

    private static SecretKey deriveKeyFromPassword(char[] password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, PBKDF2_ITERATIONS, AES_KEY_SIZE);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = skf.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }
}

