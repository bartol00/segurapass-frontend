package com.example.passwordmanagerclient.service;

import com.example.passwordmanagerclient.api.authorization.*;
import com.example.passwordmanagerclient.api.error.ApiError;
import com.example.passwordmanagerclient.config.AppConfig;
import com.example.passwordmanagerclient.util.*;

import java.net.http.HttpResponse;
import java.security.PrivateKey;
import java.util.UUID;

public class AuthService {

    private static final String backendUrl = AppConfig.getBackendUrl();

    public static String login(String email, String masterPassword) {
        try {
            LoginStartReq loginStartReq = new LoginStartReq();

            loginStartReq.setEmail(email);
            loginStartReq.setDeviceId(AppContext.getDeviceId());

            HttpResponse<String> responseStart = HttpComms.sendPostRequest(loginStartReq, "/api/authorization/login/start");

            if (responseStart.statusCode() != 200) {
                ApiError apiError = DtoHandler.parseToDto(responseStart, ApiError.class);
                return apiError.getMessage();
            }

            LoginStartResp loginStartResp = DtoHandler.parseToDto(responseStart, LoginStartResp.class);
            String encryptedPrivateKey = loginStartResp.getEncryptedPrivateKey();
            String iv = loginStartResp.getKeyIv();
            String salt = loginStartResp.getKeySalt();
            UUID nonce = loginStartResp.getNonce();

            PrivateKey pk = PrivateKeyLoader.decryptPrivateKey(encryptedPrivateKey, iv, salt, masterPassword);
            String signedNonce = PrivateKeyLoader.signNonceWithPrivateKey(pk, nonce);

            LoginCompleteReq loginCompleteReq = new LoginCompleteReq();
            loginCompleteReq.setEmail(email);
            loginCompleteReq.setDeviceId(AppContext.getDeviceId());
            loginCompleteReq.setSignedNonce(signedNonce);

            HttpResponse<String> responseComplete = HttpComms.sendPostRequest(loginCompleteReq, "/api/authorization/login/end");

            if (responseComplete.statusCode() != 200) {
                ApiError apiError = DtoHandler.parseToDto(responseComplete, ApiError.class);
                return apiError.getMessage();
            }

            LoginCompleteResp loginCompleteResp = DtoHandler.parseToDto(responseComplete, LoginCompleteResp.class);

            System.out.println(loginCompleteResp.getAccessToken());
            System.out.println(loginCompleteResp.getRefreshToken());
            System.out.println(loginCompleteResp.getRefreshTokenExpiryTime());

            AppContext.setJwtToken(loginCompleteResp.getAccessToken());
            AppContext.setRefreshToken(loginCompleteResp.getRefreshToken());
            AppContext.setRefreshTokenExpiry(loginCompleteResp.getRefreshTokenExpiryTime());

            return "Login successful";
        } catch (Exception e) {
            e.printStackTrace();
            return "Login failed";
        }
    }

    public static String register(String email, String masterPassword) {
        try {
            KeyManager.KeyPairWithEncryptedPrivate keyPairWithEncryptedPrivate = KeyManager.generateKeyPair(masterPassword);

            String publicKey = keyPairWithEncryptedPrivate.publicKeyBase64;

            KeyManager.EncryptedPrivateKey encryptedPrivateKey = keyPairWithEncryptedPrivate.encryptedPrivateKey;
            String privateKeyCipher = encryptedPrivateKey.cipherTextBase64;
            String privateKeyIv = encryptedPrivateKey.ivBase64;
            String privateKeySalt = encryptedPrivateKey.saltBase64;

            RegistrationReq req = new RegistrationReq();

            req.setEmail(email);
            req.setPublicKeyPem(publicKey);
            req.setEncryptedPrivateKey(privateKeyCipher);
            req.setKeyIv(privateKeyIv);
            req.setKeySalt(privateKeySalt);
            req.setDeviceId(AppContext.getDeviceId());

            HttpResponse<String> response = HttpComms.sendPostRequest(req, "/api/authorization/register");

            if (response.statusCode() != 200) {
                ApiError apiError = DtoHandler.parseToDto(response, ApiError.class);
                return apiError.getMessage();
            }

            LoginCompleteResp loginCompleteResp = DtoHandler.parseToDto(response, LoginCompleteResp.class);

            System.out.println(loginCompleteResp.getAccessToken());
            System.out.println(loginCompleteResp.getRefreshToken());
            System.out.println(loginCompleteResp.getRefreshTokenExpiryTime());

            AppContext.setJwtToken(loginCompleteResp.getAccessToken());
            AppContext.setRefreshToken(loginCompleteResp.getRefreshToken());
            AppContext.setRefreshTokenExpiry(loginCompleteResp.getRefreshTokenExpiryTime());

            return "Registration successful";
        } catch (Exception e) {
            // e.printStackTrace();
            return "Registration failed";
        }
    }
}
