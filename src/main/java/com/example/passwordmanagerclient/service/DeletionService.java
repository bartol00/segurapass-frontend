package com.example.passwordmanagerclient.service;

import com.example.passwordmanagerclient.util.*;
import xyz.segurapass.sdk.exception.SegurapassSdkException;

public class DeletionService {

    public static void deleteEmail(String email) {
        try {
            deletion().emailDeletion(email);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public static OperationResult deleteAuthorized(char[] masterPassword) {
        try {

            deletion().authorizedDeletion(
                    AppContext.getEmail(),
                    masterPassword,
                    AppContext.getDeviceId()
            );

            return new OperationResult("Account deletion successful", true);

        } catch (SegurapassSdkException e) {
            return new OperationResult(e.getMessage(), false);
        } catch (Exception e) {
            System.err.println("Account deletion failed: " + e.getMessage());
            return new OperationResult("Account deletion failed", false);
        }
    }

    private static xyz.segurapass.sdk.service.DeletionService deletion() {
        return AppContext.getSegurapassClient().deletion();
    }

}
