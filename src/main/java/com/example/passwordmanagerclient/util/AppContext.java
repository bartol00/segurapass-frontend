package com.example.passwordmanagerclient.util;

import com.example.passwordmanagerclient.controller.uptime.UptimeCheckController;
import com.example.passwordmanagerclient.util.appdata.DeviceIdManager;
import com.example.passwordmanagerclient.util.appdata.ServerUrlManager;
import xyz.segurapass.sdk.helpers.LoginSuccessObject;
import xyz.segurapass.sdk.models.DecryptedCredential;
import com.segurapass.api.ApiClient;
import xyz.segurapass.sdk.SegurapassClient;
import lombok.Getter;
import lombok.Setter;

import java.security.PublicKey;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class AppContext {

    @Getter
    private static UUID deviceId;

    @Getter
    private static String serverUrl;

    @Getter
    @Setter
    private static String email;

    @Getter
    @Setter
    private static char[] passwordChar;

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

    @Getter
    @Setter
    private static boolean totpEnabled = false;

    @Getter
    @Setter
    private static String totpCode;

    @Getter
    @Setter
    private static boolean emailClientActive = false;


    public static void init() {
        deviceId = UUID.fromString(DeviceIdManager.getDeviceId());
        serverUrl = ServerUrlManager.readServerUrl();
        if (serverUrl != null) {
            serverUrlSetup();
        }
    }

    public static void serverUrlSetup() {
        ApiClient apiClient = new ApiClient(serverUrl);
        segurapassClient = new SegurapassClient(apiClient);
    }

    public static void setServerUrl(String url) {
        ServerUrlManager.writeServerUrl(url);
        serverUrl = url;
        serverUrlSetup();
        UptimeCheckController controller = new UptimeCheckController();
        controller.goToVersionCheck();
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

    public static void clearPasswordChar() {
        if (passwordChar != null) {
            Arrays.fill(passwordChar, (char) 0);
            passwordChar = null;
        }
    }

    public static void clearSensitiveData() {
        clearPasswordChar();
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
