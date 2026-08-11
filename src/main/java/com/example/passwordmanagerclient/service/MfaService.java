package com.example.passwordmanagerclient.service;

import com.example.passwordmanagerclient.util.AppContext;
import xyz.segurapass.api.authorization.LoginCompleteResp;
import xyz.segurapass.api.mfa.TotpResp;

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

    public static LoginCompleteResp loginTotp(String code, String otp) {
        return totp().loginTotp(code, otp);
    }

    public static LoginCompleteResp recoveryTotp(String code, String recoveryCode) {
        return totp().recoveryTotp(code, recoveryCode);
    }

    private static xyz.segurapass.sdk.service.TotpService totp() {
        return AppContext.getSegurapassClient().totp();
    }

}
