package com.example.passwordmanagerclient.util;

import com.example.passwordmanagerclient.api.credentials.CredentialsResp;
import lombok.Getter;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;

public final class PrivateKeyLoader {

    private static final int AES_KEY_SIZE = 256;
    private static final int GCM_TAG_LENGTH = 128;

    private static final int ARGON2_ITERATIONS = 3;
    private static final int ARGON2_MEMORY_KB = 64 * 1024;
    private static final int ARGON2_PARALLELISM = 1;

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
        private final EncryptionFieldResult websiteField;
        private final EncryptionFieldResult usernameField;
        private final EncryptionFieldResult passwordField;

        public EncryptionResult(EncryptionFieldResult websiteField, EncryptionFieldResult usernameField, EncryptionFieldResult passwordField) {
            this.websiteField = websiteField;
            this.usernameField = usernameField;
            this.passwordField = passwordField;
        }
    }

    public static EncryptionResult encryptCredential(String websitePlaintext, String usernamePlaintext, String passwordPlaintext, char[] masterPassword, String salt) throws Exception {
        SecretKey secretKey = deriveKeyFromPassword(masterPassword, salt.getBytes());

        EncryptionFieldResult websiteResult = encryptField(websitePlaintext, secretKey);
        EncryptionFieldResult usernameResult = encryptField(usernamePlaintext, secretKey);
        EncryptionFieldResult passwordResult = encryptField(passwordPlaintext, secretKey);

        return new EncryptionResult(websiteResult, usernameResult, passwordResult);
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
        return encryptField(plaintext, secretKey);
    }

    public static List<CredentialsResp> decryptList(List<CredentialsResp> encryptedCredentials, char[] masterPassword, String salt) throws Exception {
        SecretKey secretKey = deriveKeyFromPassword(masterPassword, salt.getBytes());

        List<CredentialsResp> decryptedCredentials = new ArrayList<>();

        for (CredentialsResp encryptedCredential : encryptedCredentials) {
            String encryptedWebsite = encryptedCredential.getWebsite();
            String websiteIv = encryptedCredential.getIvWebsite();

            String encryptedUsername = encryptedCredential.getUsername();
            String usernameIv = encryptedCredential.getIvUsername();

            String encryptedPassword = encryptedCredential.getPassword();
            String passwordIv = encryptedCredential.getIvPassword();

            String decryptedWebsite = decryptField(encryptedWebsite, websiteIv, secretKey);
            String decryptedUsername = decryptField(encryptedUsername, usernameIv, secretKey);
            String decryptedPassword = decryptField(encryptedPassword, passwordIv, secretKey);

            encryptedCredential.setWebsite(decryptedWebsite);
            encryptedCredential.setUsername(decryptedUsername);
            encryptedCredential.setPassword(decryptedPassword);

            decryptedCredentials.add(encryptedCredential);
        }

        return decryptedCredentials;
    }

    public static CredentialsResp decryptCredential(CredentialsResp encryptedCredential, char[] masterPassword, String salt) throws Exception {
        SecretKey secretKey = deriveKeyFromPassword(masterPassword, salt.getBytes());

        String encryptedWebsite = encryptedCredential.getWebsite();
        String websiteIv = encryptedCredential.getIvWebsite();

        String encryptedUsername = encryptedCredential.getUsername();
        String usernameIv = encryptedCredential.getIvUsername();

        String encryptedPassword = encryptedCredential.getPassword();
        String passwordIv = encryptedCredential.getIvPassword();

        String decryptedWebsite = decryptField(encryptedWebsite, websiteIv, secretKey);
        String decryptedUsername = decryptField(encryptedUsername, usernameIv, secretKey);
        String decryptedPassword = decryptField(encryptedPassword, passwordIv, secretKey);

        encryptedCredential.setWebsite(decryptedWebsite);
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

    private static SecretKey deriveKeyFromPassword(char[] password, byte[] salt) {
        final int keyLenBytes = AES_KEY_SIZE / 8;
        byte[] pwdBytes = null;
        byte[] keyBytes = new byte[keyLenBytes];

        try {
            pwdBytes = StandardCharsets.UTF_8.encode(java.nio.CharBuffer.wrap(password)).array();
            byte[] exactPwd = Arrays.copyOf(pwdBytes, pwdBytes.length);
            Arrays.fill(pwdBytes, (byte) 0);
            pwdBytes = exactPwd;

            Argon2Parameters.Builder builder = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                    .withSalt(salt)
                    .withParallelism(ARGON2_PARALLELISM)
                    .withMemoryAsKB(ARGON2_MEMORY_KB)
                    .withIterations(ARGON2_ITERATIONS);

            Argon2BytesGenerator gen = new Argon2BytesGenerator();
            gen.init(builder.build());
            gen.generateBytes(pwdBytes, keyBytes);

            byte[] keyCopy = Arrays.copyOf(keyBytes, keyLenBytes);
            Arrays.fill(keyBytes, (byte) 0);
            return new SecretKeySpec(keyCopy, "AES");
        } finally {
            if (pwdBytes != null) Arrays.fill(pwdBytes, (byte) 0);
            Arrays.fill(keyBytes, (byte) 0);
        }
    }
}

