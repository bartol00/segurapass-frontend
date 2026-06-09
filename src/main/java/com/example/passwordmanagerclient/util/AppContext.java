package com.example.passwordmanagerclient.util;

import xyz.segurapass.sdk.helpers.LoginSuccessObject;
import xyz.segurapass.sdk.models.DecryptedCredential;
import com.example.passwordmanagerclient.config.AppConfig;
import com.segurapass.api.ApiClient;
import xyz.segurapass.sdk.SegurapassClient;
import lombok.Getter;
import lombok.Setter;

import java.security.PublicKey;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AppContext {

    @Getter
    private static UUID deviceId;

    @Getter
    @Setter
    private static String email;

    private static LoginSuccessObject session;

    @Getter
    @Setter
    private static String jwtToken;

    @Getter
    @Setter
    private static Instant jwtExpiry;

    @Getter
    @Setter
    private static String refreshToken;

    @Getter
    @Setter
    private static Instant refreshTokenExpiry;

    @Getter
    @Setter
    private static List<DecryptedCredential> credentialsCache = new ArrayList<>();

    @Getter
    @Setter
    private static SegurapassClient segurapassClient;

    @Getter
    @Setter
    private static PublicKey publicKey;


    public static void init() {
        deviceId = UUID.fromString(DeviceIdManager.getDeviceId());
        ApiClient apiClient = new ApiClient(AppConfig.getBackendUrl());
        segurapassClient = new SegurapassClient(apiClient);
    }

    public static LoginSuccessObject getSession() {
        if (session == null) throw new IllegalStateException("Session object not set");
        return session;
    }

    public static void setSession(LoginSuccessObject loginSuccessObject) {
        if (session != null) {
            session.destroy();
        }
        session = loginSuccessObject;
        jwtToken = loginSuccessObject.getAccessToken();
        refreshToken = loginSuccessObject.getRefreshToken();
        refreshTokenExpiry = loginSuccessObject.getRefreshTokenExpiryTime();
        segurapassClient.setJwt(loginSuccessObject.getAccessToken());
    }

    public static void clearSensitiveData() {
        email = null;
        if (session != null) {
            session.destroy();
        }
        session = null;
        jwtToken = null;
        jwtExpiry = null;
        refreshToken = null;
        refreshTokenExpiry = null;
        if (credentialsCache != null) {
            credentialsCache.clear();
        }
        credentialsCache = null;
        segurapassClient.setJwt(null);
    }
}
