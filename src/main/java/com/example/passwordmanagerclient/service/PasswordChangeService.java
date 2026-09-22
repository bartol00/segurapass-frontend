package com.example.passwordmanagerclient.service;

import com.example.passwordmanagerclient.util.AppContext;

public class PasswordChangeService {

    public static void changePassword(char[] oldPassword, char[] newPassword) {
        passwordChange().changePassword(
                AppContext.getEmail(),
                oldPassword,
                newPassword,
                AppContext.getSession().getVaultKey(),
                AppContext.getSession().getPrivateSigningKey(),
                AppContext.getDeviceId()
        );
    }

    private static xyz.segurapass.sdk.service.PasswordChangeService passwordChange() {
        return AppContext.getSegurapassClient().passwordChange();
    }

}
