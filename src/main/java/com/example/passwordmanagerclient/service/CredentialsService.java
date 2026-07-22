package com.example.passwordmanagerclient.service;

import xyz.segurapass.sdk.exception.SegurapassSdkException;
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

        } catch (SegurapassSdkException e) {
            System.err.println(e.getMessage());
        } catch (Exception e) {
            System.err.println("Exception occurred while getting credentials: " + e.getMessage());
        }
        return new LinkedList<>();
    }

    public static DecryptedCredential addCredential(String website, String username, String password) {

        try {

            return credentials().addCredential(
                    website,
                    username,
                    password,
                    AppContext.getSession().getVaultKey(),
                    AppContext.getSession().getPrivateSigningKey()
            );

        } catch (SegurapassSdkException e) {
            System.err.println(e.getMessage());
        } catch (Exception e) {
            System.err.println("Exception occurred while adding credentials: " + e.getMessage());
        }
        return null;
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
                    AppContext.getSession().getVaultKey(),
                    AppContext.getSession().getPrivateSigningKey()
            );

        } catch (SegurapassSdkException e) {
            System.err.println(e.getMessage());
        } catch (Exception e) {
            System.err.println("Exception occurred while updating credentials: " + e.getMessage());
        }
        return null;
    }

    public static void deleteCredentials(String credentialId) {
        try {
            credentials().deleteCredential(credentialId, AppContext.getSession().getPrivateSigningKey());
        } catch (SegurapassSdkException e) {
            System.err.println(e.getMessage());
        } catch (Exception e) {
            System.err.println("Exception occurred while deleting credentials: " + e.getMessage());
        }
    }

    private static xyz.segurapass.sdk.service.CredentialsService credentials() {
        return AppContext.getSegurapassClient().credentials();
    }
}
