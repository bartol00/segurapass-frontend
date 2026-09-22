package com.example.passwordmanagerclient.service;

import com.example.passwordmanagerclient.util.AppContext;
import com.example.passwordmanagerclient.util.TokenManager;
import xyz.segurapass.api.authorization.LoginCompleteResp;
import xyz.segurapass.sdk.exception.SegurapassSdkException;
import xyz.segurapass.sdk.helpers.LoginSuccessObject;
import xyz.segurapass.sdk.service.AuthorizationService;
import xyz.segurapass.sdk.service.KeyService;

import java.time.Instant;

public class AuthService {

    public static void register(String email, char[] masterPassword) throws Exception {
        auth().register(email, masterPassword, AppContext.getDeviceId());
    }

    public static void login(String email, char[] masterPassword) throws Exception {
        AppContext.setEmail(email);

        Object loginObject = auth().login(email, masterPassword, AppContext.getDeviceId());
        if (loginObject instanceof LoginCompleteResp loginCompleteResp) {
            AppContext.setPasswordChar(masterPassword);
            AppContext.setTotpEnabled(true);
            AppContext.setTotpCode(loginCompleteResp.getTotpCode());
            return;
        }

        assert loginObject instanceof LoginSuccessObject;
        LoginSuccessObject successObject = (LoginSuccessObject) loginObject;

        if (!keys().isValid(successObject.getAccessToken(), AppContext.getPublicKey())) {
            throw new Exception("Could not verify JWT");
        }

        Instant jwtExpiry = TokenManager.getJwtExpiry(successObject.getAccessToken());
        if (jwtExpiry == null) {
            throw new Exception("Could not get expiry time from JWT");
        }

        AppContext.setSession(successObject);
        AppContext.setJwtExpiry(jwtExpiry);
    }

    public static void refreshJwt() throws Exception {
        String accessToken = auth().refreshJwt(AppContext.getRefreshToken());

        if (!keys().isValid(accessToken, AppContext.getPublicKey())) {
            throw new Exception("Could not verify JWT");
        }

        Instant jwtExpiry = TokenManager.getJwtExpiry(accessToken);
        if (jwtExpiry == null) {
            throw new Exception("Could not get expiry time from JWT");
        }

        AppContext.setJwtToken(accessToken);
        AppContext.setJwtExpiry(jwtExpiry);
        AppContext.getSegurapassClient().setJwt(accessToken);
    }

    public static void logout() {
        String refreshToken = AppContext.getRefreshToken();

        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }

        try {
            auth().logout(refreshToken);
        } catch (SegurapassSdkException e) {
            System.err.println(e.getMessage());
        } catch (Exception e) {
            System.err.println("Exception occurred during logout: " + e.getMessage());
        }
    }

    private static AuthorizationService auth() {
        return AppContext.getSegurapassClient().auth();
    }

    private static KeyService keys() {
        return AppContext.getSegurapassClient().keys();
    }
}
