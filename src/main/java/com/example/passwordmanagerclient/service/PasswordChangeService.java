package com.example.passwordmanagerclient.service;

import com.example.passwordmanagerclient.util.AppContext;
import com.example.passwordmanagerclient.util.OperationResult;
import com.segurapass.exception.SdkException;

public class PasswordChangeService {

    public static OperationResult changePassword(char[] oldPassword, char[] newPassword) {
        try {
            passwordChange().changePassword(
                    AppContext.getEmail(),
                    oldPassword,
                    newPassword,
                    AppContext.getSession().getVaultKey(),
                    AppContext.getDeviceId()
            );
            return new OperationResult("Password changed successfully", true);
        } catch (SdkException e) {
            return new OperationResult(e.getMessage(), false);
        } catch (Exception e) {
            return new OperationResult("An exception occurred " + e.getMessage(), false);
        }
    }

    private static com.segurapass.service.PasswordChangeService passwordChange() {
        return AppContext.getSegurapassClient().passwordChange();
    }

}
