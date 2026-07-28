package com.example.passwordmanagerclient.service;

import com.example.passwordmanagerclient.util.AppContext;
import com.example.passwordmanagerclient.util.OperationResult;
import xyz.segurapass.sdk.exception.SegurapassSdkException;

public class PasswordChangeService {

    public static OperationResult changePassword(char[] oldPassword, char[] newPassword) {
        try {

            passwordChange().changePassword(
                    AppContext.getEmail(),
                    oldPassword,
                    newPassword,
                    AppContext.getSession().getVaultKey(),
                    AppContext.getSession().getPrivateSigningKey(),
                    AppContext.getDeviceId()
            );

            return new OperationResult("Password changed successfully", true);

        } catch (SegurapassSdkException e) {
            return new OperationResult(e.getMessage(), false);
        } catch (Exception e) {
            return new OperationResult(
                    "Exception occurred while attempting to change master password: " + e.getMessage(),
                    false
            );
        }
    }

    private static xyz.segurapass.sdk.service.PasswordChangeService passwordChange() {
        return AppContext.getSegurapassClient().passwordChange();
    }

}
