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
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

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

            if (DtoHandler.getStatusCode(response) != 200) {
                ApiError apiError = DtoHandler.parseToDto(response, ApiError.class);
                return new OperationResult(apiError.getMessage(), false);
            }

            return new OperationResult("Registration successful. Please verify the email address you entered before attempting to log in", true);
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
            startReq.setDeviceId(AppContext.getDeviceId());
            startReq.setA(Base64.getEncoder().encodeToString(A.toByteArray()));

            HttpResponse<String> responseStart = HttpComms.sendPostRequest(startReq, "/api/authorization/login/start");

            if (DtoHandler.getStatusCode(responseStart) != 200) {
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

            if (DtoHandler.getStatusCode(responseComplete) != 200) {
                ApiError apiError = DtoHandler.parseToDto(responseComplete, ApiError.class);
                return new OperationResult(apiError.getMessage(), false);
            }

            LoginCompleteResp loginCompleteResp = DtoHandler.parseToDto(responseComplete, LoginCompleteResp.class);

            BigInteger M2_client = SRP6Util.calculateM2(digest, A, M1, S, B);

            if (!M2_client.equals(new BigInteger(1, Base64.getDecoder().decode(loginCompleteResp.getM2())))) {
                return new OperationResult("M2 mismatch, cannot verify server authenticity", false);
            }

            Instant jwtExpiry = TokenManager.getJwtExpiry(loginCompleteResp.getAccessToken());
            if (jwtExpiry == null) {
                return new OperationResult("Could not get expiry time from JWT", false);
            }

            AppContext.setEmail(email);
            AppContext.setJwtToken(loginCompleteResp.getAccessToken());
            AppContext.setJwtExpiry(jwtExpiry);
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

    public static OperationResult refreshJwt() {
        try {
            RefreshReq refreshReq = new RefreshReq();
            refreshReq.setEmail(AppContext.getEmail());
            refreshReq.setDeviceId(AppContext.getDeviceId());
            refreshReq.setRefreshToken(AppContext.getRefreshToken());

            HttpResponse<String> response = HttpComms.sendPostRequest(refreshReq, "/api/authorization/refresh");

            if (DtoHandler.getStatusCode(response) != 200) {
                return new OperationResult("Could not get valid JWT from server", false);
            }

            RefreshResp refreshResp = DtoHandler.parseToDto(response, RefreshResp.class);

            Instant jwtExpiry = TokenManager.getJwtExpiry(refreshResp.getAccessToken());
            if (jwtExpiry == null) {
                return new OperationResult("Could not get expiry time from JWT", false);
            }

            AppContext.setJwtToken(refreshResp.getAccessToken());
            AppContext.setJwtExpiry(jwtExpiry);

            return new OperationResult("Successfully refreshed JWT", true);
        } catch (Exception e) {
            return new OperationResult("Could not refresh JWT", false);
        }
    }

    public static void logout() {
        try {
            RefreshReq refreshReq = new RefreshReq();

            refreshReq.setEmail(AppContext.getEmail());
            refreshReq.setDeviceId(AppContext.getDeviceId());
            refreshReq.setRefreshToken(AppContext.getRefreshToken());

            HttpComms.sendPostRequest(refreshReq, "/api/authorization/logout");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
