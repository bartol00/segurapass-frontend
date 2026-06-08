package com.example.passwordmanagerclient.service;

import xyz.segurapass.sdk.models.DecryptedCredential;
import xyz.segurapass.sdk.models.DecryptedCredentials;
import com.example.passwordmanagerclient.util.*;

import java.util.LinkedList;
import java.util.List;

public class CredentialsService {

    public static List<DecryptedCredential> getCredentials(int page, int size) {

        try {

            DecryptedCredentials decryptedCredentials = credentials().getCredentials(
                    page,
                    size,
                    AppContext.getSession().getVaultKey()
            );
            return decryptedCredentials.getCredentials();

        } catch (Exception e) {
            System.err.println(e.getMessage());
            return new LinkedList<>();
        }
    }

    public static DecryptedCredential addCredential(String website, String username, String password) {

        try {

            return credentials().addCredential(
                    website,
                    username,
                    password,
                    AppContext.getSession().getVaultKey()
            );

        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    public static DecryptedCredential updateCredentials(
            String credentialId,
            String website,
            String username,
            String password
    ) {
        try {

            return credentials().updateCredential(
                    credentialId,
                    website,
                    username,
                    password,
                    AppContext.getSession().getVaultKey()
            );

        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    public static void deleteCredentials(String credentialId) {
        try {
            credentials().deleteCredential(credentialId);
        } catch (Exception e) {
            System.err.println("Failed to delete credentials: " + e.getMessage());
        }
    }

    private static xyz.segurapass.sdk.service.CredentialsService credentials() {
        return AppContext.getSegurapassClient().credentials();
    }
}
