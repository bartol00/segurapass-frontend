package com.example.passwordmanagerclient.service;

import com.segurapass.helpers.LoginSuccessObject;
import xyz.segurapass.api.authorization.*;
import com.example.passwordmanagerclient.util.*;
import com.segurapass.exception.SdkException;
import com.segurapass.service.AuthorizationService;
import com.segurapass.service.KeyService;

import java.time.Instant;

public class AuthService {

    public static OperationResult register(String email, char[] masterPassword) {
        try {
            auth().register(email, masterPassword, AppContext.getDeviceId());
            return new OperationResult("Registration successful. Please verify the email address you entered before attempting to log in", true);
        } catch (SdkException e) {
            return new OperationResult(e.getMessage(), false);
        } catch (Exception e) {
            return new OperationResult("An exception occurred " + e.getMessage(), false);
        }
    }

    public static OperationResult login(String email, char[] masterPassword) {
        try {

            LoginSuccessObject successObject = auth().login(email, masterPassword, AppContext.getDeviceId());

            if (!keys().isValid(successObject.getAccessToken(), AppContext.getPublicKey())) {
                return new OperationResult("Could not verify JWT", false);
            }

            Instant jwtExpiry = TokenManager.getJwtExpiry(successObject.getAccessToken());
            if (jwtExpiry == null) {
                return new OperationResult("Could not get expiry time from JWT", false);
            }

            AppContext.setEmail(email);
            AppContext.setSession(successObject);
            AppContext.setJwtExpiry(jwtExpiry);

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
