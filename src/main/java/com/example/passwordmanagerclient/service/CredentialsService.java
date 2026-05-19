package com.example.passwordmanagerclient.service;

import com.segurapass.models.credentials.DecryptedCredential;
import com.segurapass.models.credentials.DecryptedCredentials;
import xyz.segurapass.api.credentials.*;
import com.example.passwordmanagerclient.util.*;

import java.util.*;

public class CredentialsService {

    public static List<DecryptedCredential> getCredentials(int page, int size) {
        try {
            DecryptedCredentials decryptedCredentials = credentials().getCredentials(page, size, AppContext.getSession().getVaultKey());
            return decryptedCredentials.getCredentials();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new LinkedList<>();
        }
    }

    public static DecryptedCredential addCredential(String website, String username, String password) {
        try {
            CredentialsRespSdk credentialsRespSdk = credentials().addCredential(
                    website,
                    username,
                    password,
                    AppContext.getSession().getVaultKey()
            );

            return new DecryptedCredential(
                    credentialsRespSdk.getCredentialsId(),
                    website,
                    username,
                    password,
                    credentialsRespSdk.getCreatedAt(),
                    credentialsRespSdk.getLastUpdated(),
                    false
            );
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static DecryptedCredential updateCredentials(String credentialId, String website, String username, String password) {
        try {
            CredentialsRespSdk credentialsRespSdk = credentials().updateCredential(
                    credentialId,
                    website,
                    username,
                    password,
                    AppContext.getSession().getVaultKey()
            );

            DecryptedCredential decryptedCredential = new DecryptedCredential();

            return new DecryptedCredential(
                    credentialsRespSdk.getCredentialsId(),
                    website,
                    username,
                    password,
                    credentialsRespSdk.getCreatedAt(),
                    credentialsRespSdk.getLastUpdated(),
                    false
            );
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static OperationResult deleteCredentials(String credentialId) {
        try {
            credentials().deleteCredential(credentialId);
            return new OperationResult("Successfully deleted credential", true);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new OperationResult("Failed to delete credential", false);
        }
    }

    private static com.segurapass.service.CredentialsService credentials() {
        return AppContext.getSegurapassClient().credentials();
    }
}
