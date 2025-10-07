package com.example.passwordmanagerclient.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class KeyManager {

    private static final int AES_KEY_SIZE = 256;
    private static final int PBKDF2_ITERATIONS = 65536;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    public static class EncryptedPrivateKey {
        public final String cipherTextBase64;
        public final String ivBase64;
        public final String saltBase64;

        public EncryptedPrivateKey(String cipherTextBase64, String ivBase64, String saltBase64) {
            this.cipherTextBase64 = cipherTextBase64;
            this.ivBase64 = ivBase64;
            this.saltBase64 = saltBase64;
        }
    }

    public static class KeyPairWithEncryptedPrivate {
        public final String publicKeyBase64;
        public final EncryptedPrivateKey encryptedPrivateKey;

        public KeyPairWithEncryptedPrivate(String publicKeyBase64, EncryptedPrivateKey encryptedPrivateKey) {
            this.publicKeyBase64 = publicKeyBase64;
            this.encryptedPrivateKey = encryptedPrivateKey;
        }
    }

    public static KeyPairWithEncryptedPrivate generateKeyPair(String masterPassword) throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        String publicKeyB64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());

        EncryptedPrivateKey encryptedPrivateKey = encryptPrivateKey(privateKey, masterPassword);

        return new KeyPairWithEncryptedPrivate(publicKeyB64, encryptedPrivateKey);
    }

    private static EncryptedPrivateKey encryptPrivateKey(PrivateKey privateKey, String masterPassword) throws Exception {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);

        SecretKey aesKey = deriveKeyFromPassword(masterPassword, salt);

        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, spec);
        byte[] cipherText = cipher.doFinal(privateKey.getEncoded());

        return new EncryptedPrivateKey(
                Base64.getEncoder().encodeToString(cipherText),
                Base64.getEncoder().encodeToString(iv),
                Base64.getEncoder().encodeToString(salt)
        );
    }

    private static SecretKey deriveKeyFromPassword(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, AES_KEY_SIZE);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }
}

