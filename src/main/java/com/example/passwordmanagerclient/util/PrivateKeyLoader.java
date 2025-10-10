package com.example.passwordmanagerclient.util;

import com.example.passwordmanagerclient.api.credentials.CredentialsResp;
import lombok.Getter;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

public final class PrivateKeyLoader {

    private static final int AES_KEY_SIZE = 256;
    private static final int PBKDF2_ITERATIONS = 65536;
    private static final int GCM_TAG_LENGTH = 128; // bits

    private PrivateKeyLoader() {}

    @Getter
    public static class EncryptionFieldResult {
        private final String cipherB64;
        private final String ivB64;

        public EncryptionFieldResult(String cipherB64, String ivB64) {
            this.cipherB64 = cipherB64;
            this.ivB64 = ivB64;
        }
    }

    @Getter
    public static class EncryptionResult {
        private final EncryptionFieldResult usernameField;
        private final EncryptionFieldResult passwordField;

        public EncryptionResult(EncryptionFieldResult usernameField, EncryptionFieldResult passwordField) {
            this.usernameField = usernameField;
            this.passwordField = passwordField;
        }
    }

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

    public static EncryptionResult encryptCredential(String usernamePlaintext, String passwordPlaintext, char[] masterPassword, String salt) throws Exception {
        SecretKey secretKey = deriveKeyFromPassword(masterPassword, salt.getBytes());

        EncryptionFieldResult usernameResult = encryptField(usernamePlaintext, secretKey);
        EncryptionFieldResult passwordResult = encryptField(passwordPlaintext, secretKey);

        return new EncryptionResult(usernameResult, passwordResult);
    }

    public static EncryptionFieldResult encryptField(String plaintext, SecretKey secretKey) throws Exception {
        byte[] iv = new byte[12];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);

        byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        return new EncryptionFieldResult(
                Base64.getEncoder().encodeToString(cipherText),
                Base64.getEncoder().encodeToString(iv)
        );
    }

    public static EncryptionFieldResult encryptFieldUpdate(String plaintext, char[] masterPassword, String salt) throws Exception {
        SecretKey secretKey = deriveKeyFromPassword(masterPassword, salt.getBytes());

        byte[] iv = new byte[12];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);

        byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        return new EncryptionFieldResult(
                Base64.getEncoder().encodeToString(cipherText),
                Base64.getEncoder().encodeToString(iv)
        );
    }

    public static List<CredentialsResp> decryptList(List<CredentialsResp> encryptedCredentials, char[] masterPassword, String salt) throws Exception {
        SecretKey secretKey = deriveKeyFromPassword(masterPassword, salt.getBytes());

        List<CredentialsResp> decryptedCredentials = new ArrayList<>();

        for (CredentialsResp encryptedCredential : encryptedCredentials) {
            String encryptedUsername = encryptedCredential.getUsername();
            String usernameIv = encryptedCredential.getIvEmail();

            String encryptedPassword = encryptedCredential.getPassword();
            String passwordIv = encryptedCredential.getIvPassword();

            String decryptedUsername = decryptField(encryptedUsername, usernameIv, secretKey);
            String decryptedPassword = decryptField(encryptedPassword, passwordIv, secretKey);

            encryptedCredential.setUsername(decryptedUsername);
            encryptedCredential.setPassword(decryptedPassword);

            decryptedCredentials.add(encryptedCredential);
        }

        return decryptedCredentials;
    }

    public static CredentialsResp decryptCredential(CredentialsResp encryptedCredential, char[] masterPassword, String salt) throws Exception {
        SecretKey secretKey = deriveKeyFromPassword(masterPassword, salt.getBytes());

        String encryptedUsername = encryptedCredential.getUsername();
        String usernameIv = encryptedCredential.getIvEmail();

        String encryptedPassword = encryptedCredential.getPassword();
        String passwordIv = encryptedCredential.getIvPassword();

        String decryptedUsername = decryptField(encryptedUsername, usernameIv, secretKey);
        String decryptedPassword = decryptField(encryptedPassword, passwordIv, secretKey);

        encryptedCredential.setUsername(decryptedUsername);
        encryptedCredential.setPassword(decryptedPassword);

        return encryptedCredential;
    }

    public static String decryptField(String cipherTextB64, String iv, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, Base64.getDecoder().decode(iv));
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);

        byte[] cipherBytes = Base64.getDecoder().decode(cipherTextB64);
        byte[] plaintext = cipher.doFinal(cipherBytes);

        return new String(plaintext, StandardCharsets.UTF_8);
    }

    private static SecretKey deriveKeyFromPassword(char[] password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, PBKDF2_ITERATIONS, AES_KEY_SIZE);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = skf.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }
}

