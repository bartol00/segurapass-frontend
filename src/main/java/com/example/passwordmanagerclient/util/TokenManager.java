package com.example.passwordmanagerclient.util;

import com.example.passwordmanagerclient.service.AuthService;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

public class TokenManager {

    public static void ensureValidJwt() throws Exception {
        if (!isJwtExpired()) {
            return;
        }
        if (isRefreshTokenExpired()) {
            throw new Exception("Refresh token has expired");
        }
        OperationResult result = AuthService.refreshJwt();
        if (!result.isPassed()) {
            throw new Exception(result.getMessage());
        }

        System.out.println("Refresh token successfully used");
    }

    public static Instant getJwtExpiry(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length < 2) return null;

            String payloadJson = new String(
                    Base64.getUrlDecoder().decode(parts[1]),
                    StandardCharsets.UTF_8
            );

            JSONObject payload = new JSONObject(payloadJson);
            if (!payload.has("exp")) return null;

            long expSeconds = payload.getLong("exp");
            return Instant.ofEpochSecond(expSeconds);
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean isJwtExpired() {
        Instant expiry = AppContext.getJwtExpiry();
        return expiry == null || Instant.now().isAfter(expiry.minusSeconds(10));
    }

    private static boolean isRefreshTokenExpired() {
        Instant expiry = AppContext.getRefreshTokenExpiry();
        return expiry == null || Instant.now().isAfter(expiry);
    }

}
