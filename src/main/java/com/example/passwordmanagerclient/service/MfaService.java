package com.example.passwordmanagerclient.service;

import com.example.passwordmanagerclient.util.AppContext;
import com.example.passwordmanagerclient.util.TokenManager;
import xyz.segurapass.api.mfa.TotpResp;
import xyz.segurapass.sdk.helpers.LoginSuccessObject;
import xyz.segurapass.sdk.service.KeyService;

import java.time.Instant;

public class MfaService {

    public static String addTotp() {
        TotpResp resp = totp().addTotp(AppContext.getSession().getPrivateSigningKey());
        return resp.getTotpUrl();
    }

    public static void removeTotp() {
        totp().removeTotp(AppContext.getSession().getPrivateSigningKey());
    }

    public static String verifyTotp(String otp) {
        return totp().verifyTotp(otp);
    }

    public static void loginTotp(String code, String otp) {
        LoginSuccessObject successObject = totp().loginTotp(code, otp, AppContext.getPasswordChar());

        if (!keys().isValid(successObject.getAccessToken(), AppContext.getPublicKey())) {
            throw new RuntimeException("Could not verify JWT");
        }

        Instant jwtExpiry = TokenManager.getJwtExpiry(successObject.getAccessToken());
        if (jwtExpiry == null) {
            throw new RuntimeException("Could not get expiry time from JWT");
        }

        AppContext.setSession(successObject);
        AppContext.setJwtExpiry(jwtExpiry);
    }

    public static void recoveryTotp(String code, String recoveryCode) {
        LoginSuccessObject successObject = totp().recoveryTotp(code, recoveryCode, AppContext.getPasswordChar());

        if (!keys().isValid(successObject.getAccessToken(), AppContext.getPublicKey())) {
            throw new RuntimeException("Could not verify JWT");
        }

        Instant jwtExpiry = TokenManager.getJwtExpiry(successObject.getAccessToken());
        if (jwtExpiry == null) {
            throw new RuntimeException("Could not get expiry time from JWT");
        }

        AppContext.setSession(successObject);
        AppContext.setJwtExpiry(jwtExpiry);
    }

    private static xyz.segurapass.sdk.service.TotpService totp() {
        return AppContext.getSegurapassClient().totp();
    }

    private static KeyService keys() {
        return AppContext.getSegurapassClient().keys();
    }

}
