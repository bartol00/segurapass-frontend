package com.example.passwordmanagerclient.util;

import com.example.passwordmanagerclient.api.credentials.CredentialsResp;
import com.example.passwordmanagerclient.config.AppConfig;
import com.segurapass.ApiClient;
import com.segurapass.SegurapassClient;
import lombok.Getter;
import lombok.Setter;

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
    private static String saltKey;

    @Getter
    @Setter
    private static List<CredentialsResp> credentialsCache = new ArrayList<>();

    private static char[] masterPassword;

    @Getter
    @Setter
    private static SegurapassClient segurapassClient;


    public static void init() {
        deviceId = UUID.fromString(DeviceIdManager.getDeviceId());
        ApiClient apiClient = new ApiClient(AppConfig.getBackendUrl());
        segurapassClient = new SegurapassClient(apiClient);
    }

    public static void setMasterPassword(String pwd) {
        clearMasterPassword();
        masterPassword = pwd.toCharArray();
    }

    public static char[] getMasterPassword() {
        if (masterPassword == null) throw new IllegalStateException("Master password not set");
        return masterPassword;
    }

    public static void clearMasterPassword() {
        if (masterPassword != null) {
            java.util.Arrays.fill(masterPassword, '\0');
            masterPassword = null;
        }
    }

    public static void clearSensitiveData() {
        clearMasterPassword();
        email = null;
        jwtToken = null;
        jwtExpiry = null;
        refreshToken = null;
        refreshTokenExpiry = null;
        saltKey = null;
        if (credentialsCache != null) {
            credentialsCache.clear();
        }
        credentialsCache = null;
        segurapassClient = null;
    }
}
