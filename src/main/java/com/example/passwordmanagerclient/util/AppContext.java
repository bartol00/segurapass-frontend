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
    private static String jwtToken;

    @Getter
    @Setter
    private static String refreshToken;

    @Getter
    @Setter
    private static Instant refreshTokenExpiry;

    public static void init() {
        deviceId = UUID.fromString(DeviceIdManager.getDeviceId());
        // System.out.println("Device ID initialized: " + deviceId);
    }
}
