package com.example.passwordmanagerclient.service;

import xyz.segurapass.api.authorization.*;
import com.example.passwordmanagerclient.util.*;
import com.segurapass.exception.SdkException;
import com.segurapass.service.AuthorizationService;
import com.segurapass.service.KeyService;

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

            String accessToken = loginCompleteResp.getAccessToken();

            if (!keys().isValid(accessToken, AppContext.getPublicKey())) {
                return new OperationResult("Could not verify JWT", false);
            }

            Instant jwtExpiry = TokenManager.getJwtExpiry(accessToken);
            if (jwtExpiry == null) {
                return new OperationResult("Could not get expiry time from JWT", false);
            }

            AppContext.setEmail(email);
            AppContext.setJwtToken(accessToken);
            AppContext.setJwtExpiry(jwtExpiry);
            AppContext.setRefreshToken(loginCompleteResp.getRefreshToken());
            AppContext.setRefreshTokenExpiry(loginCompleteResp.getRefreshTokenExpiryTime());
            AppContext.setMasterPassword(masterPassword);
            AppContext.setSaltKey(loginCompleteResp.getSaltKey());
            AppContext.getSegurapassClient().setJwt(accessToken);

            return new OperationResult("Login successful", true);
        } catch (SdkException e) {
            return new OperationResult(e.getMessage(), false);
        } catch (Exception e) {
            return new OperationResult("An exception occurred " + e.getMessage(), false);
        }
    }

    public static OperationResult refreshJwt() {
        try {
            RefreshResp refreshResp = auth().refreshJwt(AppContext.getRefreshToken());

            String accessToken = refreshResp.getAccessToken();

            if (!keys().isValid(accessToken, AppContext.getPublicKey())) {
                return new OperationResult("Could not verify JWT", false);
            }

            Instant jwtExpiry = TokenManager.getJwtExpiry(accessToken);
            if (jwtExpiry == null) {
                return new OperationResult("Could not get expiry time from JWT", false);
            }

            AppContext.setJwtToken(accessToken);
            AppContext.setJwtExpiry(jwtExpiry);
            AppContext.getSegurapassClient().setJwt(accessToken);

            return new OperationResult("Successfully refreshed JWT", true);
        } catch (SdkException e) {
            return new OperationResult(e.getMessage(), false);
        } catch (Exception e) {
            return new OperationResult("Could not refresh JWT", false);
        }
    }

    public static void logout() {
        try {
            auth().logout(AppContext.getRefreshToken());
        } catch (SdkException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.printf("An exception occurred %s", e.getMessage());
        }
    }

    private static AuthorizationService auth() {
        return AppContext.getSegurapassClient().auth();
    }

    private static KeyService keys() {
        return AppContext.getSegurapassClient().keys();
    }
}
