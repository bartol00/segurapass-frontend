package com.example.passwordmanagerclient.service;

import com.example.passwordmanagerclient.api.authorization.*;
import com.example.passwordmanagerclient.api.error.ApiError;
import com.example.passwordmanagerclient.util.*;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.agreement.srp.SRP6Util;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.SRP6GroupParameters;
import org.bouncycastle.crypto.agreement.srp.SRP6StandardGroups;

import java.math.BigInteger;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

public class AuthService {

    public static OperationResult registerSrp(String email, String masterPassword) {
        try {
            SRP6GroupParameters group = SRP6StandardGroups.rfc5054_3072;
            Digest digest = new SHA256Digest();
            SecureRandom random = new SecureRandom();

            byte[] saltKey = new byte[16];
            random.nextBytes(saltKey);

            byte[] saltAuth = new byte[16];
            random.nextBytes(saltAuth);

            BigInteger x = SRP6Util.calculateX(digest,
                    group.getN(),
                    saltAuth,
                    email.getBytes(StandardCharsets.UTF_8),
                    masterPassword.getBytes(StandardCharsets.UTF_8));

            BigInteger v = group.getG().modPow(x, group.getN());

            if (v.compareTo(BigInteger.ONE) < 0 || v.compareTo(group.getN()) >= 0) {
                return new OperationResult("Invalid SRP verifier computed. Please try registering again", false);
            }

            if (v.bitLength() < group.getN().bitLength() / 2) {
                return new OperationResult("Verifier is too small, which is a possible RNG issue. Please try registering again", false);
            }

            RegistrationReq req = new RegistrationReq();
            req.setEmail(email);
            req.setSaltAuth(Base64.getEncoder().encodeToString(saltAuth));
            req.setVerifier(Base64.getEncoder().encodeToString(v.toByteArray()));
            req.setSaltKey(Base64.getEncoder().encodeToString(saltKey));
            req.setDeviceId(AppContext.getDeviceId());

            HttpResponse<String> response = HttpComms.sendPostRequest(req, "/api/authorization/register");

            if (response.statusCode() != 200) {
                ApiError apiError = DtoHandler.parseToDto(response, ApiError.class);
                return new OperationResult(apiError.getMessage(), false);
            }

            RegistrationResp registrationResp = DtoHandler.parseToDto(response, RegistrationResp.class);

            AppContext.setEmail(email);
            AppContext.setJwtToken(registrationResp.getAccessToken());
            AppContext.setRefreshToken(registrationResp.getRefreshToken());
            AppContext.setRefreshTokenExpiry(registrationResp.getRefreshTokenExpiryTime());
            AppContext.setMasterPassword(masterPassword);
            AppContext.setSaltKey(Base64.getEncoder().encodeToString(saltKey));

            return new OperationResult("Registration successful", true);
        } catch (Exception e) {
            e.printStackTrace();
            return new OperationResult("Failed to register user", false);
        }
    }

    public static OperationResult loginSrp(String email, String masterPassword) {
        try {
            SRP6GroupParameters group = SRP6StandardGroups.rfc5054_3072;
            Digest digest = new SHA256Digest();
            SecureRandom random = new SecureRandom();

            BigInteger a = new BigInteger(256, random);
            BigInteger A = group.getG().modPow(a, group.getN());

            LoginStartReq startReq = new LoginStartReq();
            startReq.setEmail(email);
            startReq.setA(Base64.getEncoder().encodeToString(A.toByteArray()));

            HttpResponse<String> responseStart = HttpComms.sendPostRequest(startReq, "/api/authorization/login/start");

            if (responseStart.statusCode() != 200) {
                ApiError apiError = DtoHandler.parseToDto(responseStart, ApiError.class);
                return new OperationResult(apiError.getMessage(), false);
            }

            LoginStartResp loginStartResp = DtoHandler.parseToDto(responseStart, LoginStartResp.class);

            byte[] saltAuth = Base64.getDecoder().decode(loginStartResp.getSaltAuth());
            BigInteger B = new BigInteger(1, Base64.getDecoder().decode(loginStartResp.getB()));

            BigInteger x = SRP6Util.calculateX(digest, group.getN(), saltAuth,
                    email.getBytes(StandardCharsets.UTF_8),
                    masterPassword.getBytes(StandardCharsets.UTF_8));

            BigInteger u = SRP6Util.calculateU(digest, group.getN(), A, B);

            BigInteger k = SRP6Util.calculateK(digest, group.getN(), group.getG());
            BigInteger S = B.subtract(k.multiply(group.getG().modPow(x, group.getN())))
                    .modPow(a.add(u.multiply(x)), group.getN());
            byte[] K = new byte[digest.getDigestSize()];
            digest.update(S.toByteArray(), 0, S.toByteArray().length);
            digest.doFinal(K, 0);

            BigInteger M1 = SRP6Util.calculateM1(digest, group.getN(), A, B, S);

            LoginCompleteReq completeReq = new LoginCompleteReq();
            completeReq.setEmail(email);
            completeReq.setDeviceId(AppContext.getDeviceId());
            completeReq.setM1(Base64.getEncoder().encodeToString(M1.toByteArray()));

            HttpResponse<String> responseComplete = HttpComms.sendPostRequest(completeReq, "/api/authorization/login/end");

            if (responseComplete.statusCode() != 200) {
                ApiError apiError = DtoHandler.parseToDto(responseComplete, ApiError.class);
                return new OperationResult(apiError.getMessage(), false);
            }

            LoginCompleteResp loginCompleteResp = DtoHandler.parseToDto(responseComplete, LoginCompleteResp.class);

            BigInteger M2_client = SRP6Util.calculateM2(digest, A, M1, S, B);

            if (!M2_client.equals(new BigInteger(1, Base64.getDecoder().decode(loginCompleteResp.getM2())))) {
                return new OperationResult("M2 mismatch, cannot verify server authenticity", false);
            }

            AppContext.setEmail(email);
            AppContext.setJwtToken(loginCompleteResp.getAccessToken());
            AppContext.setRefreshToken(loginCompleteResp.getRefreshToken());
            AppContext.setRefreshTokenExpiry(loginCompleteResp.getRefreshTokenExpiryTime());
            AppContext.setMasterPassword(masterPassword);
            AppContext.setSaltKey(loginCompleteResp.getSaltKey());

            return new OperationResult("Login successful", true);
        } catch (Exception e) {
            e.printStackTrace();
            return new OperationResult("Login failed", false);
        }
    }

//    public static OperationResult login(String email, String masterPassword) {
//        try {
//            LoginStartReq loginStartReq = new LoginStartReq();
//
//            loginStartReq.setEmail(email);
//            loginStartReq.setDeviceId(AppContext.getDeviceId());
//
//            HttpResponse<String> responseStart = HttpComms.sendPostRequest(loginStartReq, "/api/authorization/login/start");
//
//            if (responseStart.statusCode() != 200) {
//                ApiError apiError = DtoHandler.parseToDto(responseStart, ApiError.class);
//                return new OperationResult(apiError.getMessage(), false);
//            }
//
//            LoginStartResp loginStartResp = DtoHandler.parseToDto(responseStart, LoginStartResp.class);
//            String encryptedPrivateKey = loginStartResp.getEncryptedPrivateKey();
//            String iv = loginStartResp.getKeyIv();
//            String salt = loginStartResp.getKeySalt();
//            UUID nonce = loginStartResp.getNonce();
//
//            PrivateKey pk = PrivateKeyLoader.decryptPrivateKey(encryptedPrivateKey, iv, salt, masterPassword);
//            String signedNonce = PrivateKeyLoader.signNonceWithPrivateKey(pk, nonce);
//
//            LoginCompleteReq loginCompleteReq = new LoginCompleteReq();
//            loginCompleteReq.setEmail(email);
//            loginCompleteReq.setDeviceId(AppContext.getDeviceId());
//            loginCompleteReq.setSignedNonce(signedNonce);
//
//            HttpResponse<String> responseComplete = HttpComms.sendPostRequest(loginCompleteReq, "/api/authorization/login/end");
//
//            if (responseComplete.statusCode() != 200) {
//                ApiError apiError = DtoHandler.parseToDto(responseComplete, ApiError.class);
//                return new OperationResult(apiError.getMessage(), false);
//            }
//
//            LoginCompleteResp loginCompleteResp = DtoHandler.parseToDto(responseComplete, LoginCompleteResp.class);
//
//            AppContext.setEmail(email);
//            AppContext.setJwtToken(loginCompleteResp.getAccessToken());
//            AppContext.setRefreshToken(loginCompleteResp.getRefreshToken());
//            AppContext.setRefreshTokenExpiry(loginCompleteResp.getRefreshTokenExpiryTime());
//            AppContext.setMasterPassword(masterPassword);
//            AppContext.setSaltKey(salt);
//
//            return new OperationResult("Login successful", true);
//        } catch (Exception e) {
//            // e.printStackTrace();
//            return new OperationResult("Login failed", false);
//        }
//    }

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
