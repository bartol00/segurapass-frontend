package com.example.passwordmanagerclient.service;

import com.example.passwordmanagerclient.util.*;
import com.segurapass.model.credentials.*;

import java.util.*;

public class CredentialsService {

    public static PagedResponse<CredentialsResp> getCredentials(int page, int size) {
        try {
            PagedResponse<CredentialsResp> pagedResponse = credentials().getCredentials(page, size);

            List<CredentialsResp> encryptedCredentials = pagedResponse.getContent();

            List<CredentialsResp> decryptedCredentials = PrivateKeyLoader.decryptList(
                    encryptedCredentials,
                    AppContext.getMasterPassword(),
                    AppContext.getSaltKey()
            );

            pagedResponse.setContent(decryptedCredentials);
            return pagedResponse;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            PagedResponse<CredentialsResp> emptyPage = new PagedResponse<>();
            emptyPage.setContent(Collections.emptyList());
            return emptyPage;
        }
    }

    public static CredentialsResp addCredential(String website, String username, String password) {
        try {
            PrivateKeyLoader.EncryptionResult encryptionResult = PrivateKeyLoader.encryptCredential(
                    website,
                    username,
                    password,
                    AppContext.getMasterPassword(),
                    AppContext.getSaltKey()
            );

            PrivateKeyLoader.EncryptionFieldResult websiteResult = encryptionResult.getWebsiteField();
            PrivateKeyLoader.EncryptionFieldResult usernameResult = encryptionResult.getUsernameField();
            PrivateKeyLoader.EncryptionFieldResult passwordResult = encryptionResult.getPasswordField();

            CredentialsResp encryptedCredentials = credentials().addCredential(
                    websiteResult.getCipherB64(),
                    websiteResult.getIvB64(),
                    usernameResult.getCipherB64(),
                    usernameResult.getIvB64(),
                    passwordResult.getCipherB64(),
                    passwordResult.getIvB64()
            );

            return PrivateKeyLoader.decryptCredential(
                    encryptedCredentials,
                    AppContext.getMasterPassword(),
                    AppContext.getSaltKey()
            );

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static CredentialsResp updateCredentials(String credentialId, String website, String username, String password) {
        try {
            String websiteCipher = null;
            String websiteIv = null;
            String usernameCipher = null;
            String usernameIv = null;
            String passwordCipher = null;
            String passwordIv = null;

            if (website != null && !website.isBlank()) {
                PrivateKeyLoader.EncryptionFieldResult encryptedWebsite = PrivateKeyLoader.encryptFieldUpdate(
                        website,
                        AppContext.getMasterPassword(),
                        AppContext.getSaltKey()
                );
                websiteCipher = encryptedWebsite.getCipherB64();
                websiteIv = encryptedWebsite.getIvB64();
            }
            if (username != null && !username.isBlank()) {
                PrivateKeyLoader.EncryptionFieldResult encryptedUsername = PrivateKeyLoader.encryptFieldUpdate(
                        username,
                        AppContext.getMasterPassword(),
                        AppContext.getSaltKey()
                );
                usernameCipher = encryptedUsername.getCipherB64();
                usernameIv = encryptedUsername.getIvB64();
            }
            if (password != null && !password.isBlank()) {
                PrivateKeyLoader.EncryptionFieldResult encryptedPassword = PrivateKeyLoader.encryptFieldUpdate(
                        password,
                        AppContext.getMasterPassword(),
                        AppContext.getSaltKey()
                );
                passwordCipher = encryptedPassword.getCipherB64();
                passwordIv = encryptedPassword.getIvB64();
            }

            CredentialsResp encryptedCredentials = credentials().updateCredential(
                    websiteCipher,
                    websiteIv,
                    usernameCipher,
                    usernameIv,
                    passwordCipher,
                    passwordIv,
                    credentialId
            );

            return PrivateKeyLoader.decryptCredential(
                    encryptedCredentials,
                    AppContext.getMasterPassword(),
                    AppContext.getSaltKey()
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
