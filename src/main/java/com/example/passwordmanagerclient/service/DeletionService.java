package com.example.passwordmanagerclient.service;

import com.example.passwordmanagerclient.util.*;
import com.segurapass.exception.SdkException;

public class DeletionService {

    public static void deleteEmail(String email) {
        try {
            deletion().emailDeletion(email);
        } catch (Exception e) {
            System.out.println(e.getMessage());
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
        } catch (SdkException e) {
            return new OperationResult(e.getMessage(), false);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new OperationResult("Account deletion failed", false);
        }
    }

    private static com.segurapass.service.DeletionService deletion() {
        return AppContext.getSegurapassClient().deletion();
    }

}
