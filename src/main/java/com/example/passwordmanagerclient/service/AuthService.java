package com.example.passwordmanagerclient.service;

import com.example.passwordmanagerclient.util.*;
import com.segurapass.exception.SdkException;
import com.segurapass.service.AuthorizationService;
import com.segurapass.model.authorization.*;

import java.time.Instant;

public class AuthService {

    public static OperationResult registerSrp(String email, String masterPassword) {
        try {
            auth().register(email, masterPassword, AppContext.getDeviceId());
            return new OperationResult("Registration successful. Please verify the email address you entered before attempting to log in", true);
        } catch (SdkException e) {
            return new OperationResult(e.getMessage(), false);
        } catch (Exception e) {
            return new OperationResult("An exception occurred " + e.getMessage(), false);
        }
    }

    public static OperationResult loginSrp(String email, String masterPassword) {
        try {
            LoginCompleteResp loginCompleteResp = auth().login(email, masterPassword, AppContext.getDeviceId());

            Instant jwtExpiry = TokenManager.getJwtExpiry(loginCompleteResp.getAccessToken());
            if (jwtExpiry == null) {
                return new OperationResult("Could not get expiry time from JWT", false);
            }

            AppContext.setEmail(email);
            AppContext.setJwtToken(loginCompleteResp.getAccessToken());
            AppContext.setJwtExpiry(jwtExpiry);
            AppContext.setRefreshToken(loginCompleteResp.getRefreshToken());
            AppContext.setRefreshTokenExpiry(loginCompleteResp.getRefreshTokenExpiryTime());
            AppContext.setMasterPassword(masterPassword);
            AppContext.setSaltKey(loginCompleteResp.getSaltKey());
            AppContext.getSegurapassClient().setJwt(loginCompleteResp.getAccessToken());

            return new OperationResult("Login successful", true);
        } catch (SdkException e) {
            return new OperationResult(e.getMessage(), false);
        } catch (Exception e) {
            return new OperationResult("An exception occurred " + e.getMessage(), false);
        }
    }

    public static OperationResult refreshJwt() {
        try {
            RefreshResp refreshResp = auth().refreshJwt(AppContext.getEmail(), AppContext.getDeviceId(), AppContext.getRefreshToken());

            Instant jwtExpiry = TokenManager.getJwtExpiry(refreshResp.getAccessToken());
            if (jwtExpiry == null) {
                return new OperationResult("Could not get expiry time from JWT", false);
            }

            AppContext.setJwtToken(refreshResp.getAccessToken());
            AppContext.setJwtExpiry(jwtExpiry);
            AppContext.getSegurapassClient().setJwt(refreshResp.getAccessToken());

            return new OperationResult("Successfully refreshed JWT", true);
        } catch (SdkException e) {
            return new OperationResult(e.getMessage(), false);
        } catch (Exception e) {
            return new OperationResult("Could not refresh JWT", false);
        }
    }

    public static void logout() {
        try {
            auth().logout(AppContext.getEmail(), AppContext.getDeviceId(), AppContext.getRefreshToken());
        } catch (SdkException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.printf("An exception occurred %s", e.getMessage());
        }
    }

    private static AuthorizationService auth() {
        return AppContext.getSegurapassClient().auth();
    }
}
