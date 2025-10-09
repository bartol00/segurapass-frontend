package com.example.passwordmanagerclient.service;

import com.example.passwordmanagerclient.api.authorization.*;
import com.example.passwordmanagerclient.api.error.ApiError;
import com.example.passwordmanagerclient.util.*;

import java.net.http.HttpResponse;
import java.security.PrivateKey;
import java.time.Instant;
import java.util.UUID;

public class AuthService {

    public static OperationResult login(String email, String masterPassword) {
        try {
            LoginStartReq loginStartReq = new LoginStartReq();

            loginStartReq.setEmail(email);
            loginStartReq.setDeviceId(AppContext.getDeviceId());

            HttpResponse<String> responseStart = HttpComms.sendPostRequest(loginStartReq, "/api/authorization/login/start");

            if (responseStart.statusCode() != 200) {
                ApiError apiError = DtoHandler.parseToDto(responseStart, ApiError.class);
                return new OperationResult(apiError.getMessage(), false);
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
                return new OperationResult(apiError.getMessage(), false);
            }

            LoginCompleteResp loginCompleteResp = DtoHandler.parseToDto(responseComplete, LoginCompleteResp.class);

            AppContext.setEmail(email);
            AppContext.setJwtToken(loginCompleteResp.getAccessToken());
            AppContext.setRefreshToken(loginCompleteResp.getRefreshToken());
            AppContext.setRefreshTokenExpiry(loginCompleteResp.getRefreshTokenExpiryTime());
            AppContext.setMasterPassword(masterPassword);
            AppContext.setKeySalt(salt);

            return new OperationResult("Login successful", true);
        } catch (Exception e) {
            // e.printStackTrace();
            return new OperationResult("Login failed", false);
        }
    }

    public static OperationResult register(String email, String masterPassword) {
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
                return new OperationResult(apiError.getMessage(), false);
            }

            LoginCompleteResp loginCompleteResp = DtoHandler.parseToDto(response, LoginCompleteResp.class);

            AppContext.setEmail(email);
            AppContext.setJwtToken(loginCompleteResp.getAccessToken());
            AppContext.setRefreshToken(loginCompleteResp.getRefreshToken());
            AppContext.setRefreshTokenExpiry(loginCompleteResp.getRefreshTokenExpiryTime());
            AppContext.setMasterPassword(masterPassword);
            AppContext.setKeySalt(privateKeySalt);

            return new OperationResult("Registration successful", true);
        } catch (Exception e) {
            // e.printStackTrace();
            return new OperationResult("Registration failed", false);
        }
    }

    public static boolean refreshJwt() {
        try {
            // TODO: ovo cak stavit u neku check metodu prije samog poziva ove metode
            Instant refreshTokenExpiry = AppContext.getRefreshTokenExpiry();
            if (refreshTokenExpiry.isBefore(Instant.now())) {
                System.out.println("refresh token is expired");
                return false;
            }

            RefreshReq refreshReq = new RefreshReq();
            refreshReq.setEmail(AppContext.getEmail());
            refreshReq.setDeviceId(AppContext.getDeviceId());
            refreshReq.setRefreshToken(AppContext.getRefreshToken());

            HttpResponse<String> response = HttpComms.sendPostRequest(refreshReq, "/api/authorization/refresh");

            if (response.statusCode() != 200) {
                System.out.println("status code is not 200 but " + response.statusCode());
                return false;
            }

            RefreshResp refreshResp = DtoHandler.parseToDto(response, RefreshResp.class);

            AppContext.setJwtToken(refreshResp.getAccessToken());

            System.out.println("Refresh endpoint: new JWT=" + refreshResp.getAccessToken());

            return true;
        } catch (Exception e) {
            // e.printStackTrace();
            return false;
        }
    }
}
