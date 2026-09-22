package com.example.passwordmanagerclient.service;

import com.example.passwordmanagerclient.util.AppContext;
import xyz.segurapass.sdk.exception.SegurapassSdkException;

public class DeletionService {

    public static void deleteEmail(String email) {
        try {
            deletion().emailDeletion(email);
        } catch (SegurapassSdkException e) {
            System.err.println(e.getMessage());
        } catch (Exception e) {
            System.err.println("Exception occurred while sending deletion email: " + e.getMessage());
        }
    }

    public static void deleteAuthorized(char[] masterPassword) {
        deletion().authorizedDeletion(
                AppContext.getEmail(),
                masterPassword,
                AppContext.getDeviceId()
        );
    }

    private static xyz.segurapass.sdk.service.DeletionService deletion() {
        return AppContext.getSegurapassClient().deletion();
    }

}
