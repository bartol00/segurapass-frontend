package com.example.passwordmanagerclient.util;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
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
    private static String refreshToken;

    @Getter
    @Setter
    private static Instant refreshTokenExpiry;

    @Getter
    @Setter
    private static String keySalt;

    private static char[] masterPassword;


    public static void init() {
        deviceId = UUID.fromString(DeviceIdManager.getDeviceId());
        // System.out.println("Device ID initialized: " + deviceId);
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
        jwtToken = null;
        refreshToken = null;
        refreshTokenExpiry = null;
        email = null;
    }
}
