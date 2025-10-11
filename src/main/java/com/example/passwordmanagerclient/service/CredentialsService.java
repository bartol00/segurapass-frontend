package com.example.passwordmanagerclient.service;

import com.example.passwordmanagerclient.api.credentials.CredentialsReq;
import com.example.passwordmanagerclient.api.credentials.CredentialsResp;
import com.example.passwordmanagerclient.api.credentials.PagedResponse;
import com.example.passwordmanagerclient.api.error.ApiError;
import com.example.passwordmanagerclient.util.*;
import com.fasterxml.jackson.core.type.TypeReference;

import java.net.http.HttpResponse;
import java.util.*;

public class CredentialsService {

    public static PagedResponse<CredentialsResp> getCredentials(int page, int size) {
        try {
            Map<String,String> params = Map.of(
                    "page", String.valueOf(page),
                    "size", String.valueOf(size)
            );

            HttpResponse<String> response = HttpComms.sendGetRequestWithAuthAndParams("/api/credentials/get", params, AppContext.getJwtToken());

            if (response.statusCode() != 200) {
                ApiError apiError = DtoHandler.parseToDto(response, ApiError.class);
                System.out.println("Error loading credentials: " + apiError.getMessage());
                PagedResponse<CredentialsResp> emptyPage = new PagedResponse<>();
                emptyPage.setContent(Collections.emptyList());
                return emptyPage;
            }

            PagedResponse<CredentialsResp> pagedResponse = DtoHandler.parseToDto(
                    response,
                    new TypeReference<>() {
                    }
            );

            List<CredentialsResp> encryptedCredentials = pagedResponse.getContent();

            List<CredentialsResp> decryptedCredentials = PrivateKeyLoader.decryptList(
                    encryptedCredentials,
                    AppContext.getMasterPassword(),
                    AppContext.getSaltKey()
            );

            pagedResponse.setContent(decryptedCredentials);
            return pagedResponse;
        } catch (Exception e) {
            e.printStackTrace();
            PagedResponse<CredentialsResp> emptyPage = new PagedResponse<>();
            emptyPage.setContent(Collections.emptyList());
            return emptyPage;
        }
    }

    public static CredentialsResp getCredentialById(String credentialId) {
        try {
            HttpResponse<String> response = HttpComms.sendGetRequestWithAuth("/api/credentials/get/" + credentialId, AppContext.getJwtToken());

            if (response.statusCode() != 200) {
                ApiError apiError = DtoHandler.parseToDto(response, ApiError.class);
                System.out.println("API error: " + apiError.getMessage());
                return null;
            }

            CredentialsResp encryptedCredentials = DtoHandler.parseToDto(response, CredentialsResp.class);

            return PrivateKeyLoader.decryptCredential(
                    encryptedCredentials,
                    AppContext.getMasterPassword(),
                    AppContext.getSaltKey()
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static OperationResult addCredential(String website, String username, String password) {
        try {
            PrivateKeyLoader.EncryptionResult encryptionResult = PrivateKeyLoader.encryptCredential(
                    username,
                    password,
                    AppContext.getMasterPassword(),
                    AppContext.getSaltKey()
            );

            PrivateKeyLoader.EncryptionFieldResult usernameResult = encryptionResult.getUsernameField();
            PrivateKeyLoader.EncryptionFieldResult passwordResult = encryptionResult.getPasswordField();

            CredentialsReq credentialsReq = new CredentialsReq();
            credentialsReq.setWebsite(website);
            credentialsReq.setUsername(usernameResult.getCipherB64());
            credentialsReq.setIvEmail(usernameResult.getIvB64());
            credentialsReq.setPassword(passwordResult.getCipherB64());
            credentialsReq.setIvPassword(passwordResult.getIvB64());

            HttpResponse<String> response = HttpComms.sendPostRequestWithAuth(credentialsReq, "/api/credentials/create", AppContext.getJwtToken());

            if (response.statusCode() != 200) {
                ApiError apiError = DtoHandler.parseToDto(response, ApiError.class);
                return new OperationResult(apiError.getMessage(), false);
            }

            return new OperationResult("Successfully added credential", true);
        } catch (Exception e) {
            e.printStackTrace();
            return new OperationResult("Failed to add credential", false);
        }
    }

    public static OperationResult updateCredentials(String credentialId, String website, String username, String password) {
        try {
            CredentialsReq credentialsReq = new CredentialsReq();

            if (website != null && !website.isBlank()) {
                credentialsReq.setWebsite(website);
            }
            if (username != null && !username.isBlank()) {
                PrivateKeyLoader.EncryptionFieldResult encryptedUsername = PrivateKeyLoader.encryptFieldUpdate(
                        username,
                        AppContext.getMasterPassword(),
                        AppContext.getSaltKey()
                );
                credentialsReq.setUsername(encryptedUsername.getCipherB64());
                credentialsReq.setIvEmail(encryptedUsername.getIvB64());
            }
            if (password != null && !password.isBlank()) {
                PrivateKeyLoader.EncryptionFieldResult encryptedPassword = PrivateKeyLoader.encryptFieldUpdate(
                        password,
                        AppContext.getMasterPassword(),
                        AppContext.getSaltKey()
                );
                credentialsReq.setPassword(encryptedPassword.getCipherB64());
                credentialsReq.setIvPassword(encryptedPassword.getIvB64());
            }

            HttpResponse<String> response = HttpComms.sendPutRequestWithAuth(credentialsReq, "/api/credentials/update/" + credentialId , AppContext.getJwtToken());

            if (response.statusCode() != 200) {
                ApiError apiError = DtoHandler.parseToDto(response, ApiError.class);
                return new OperationResult(apiError.getMessage(), false);
            }

            return new OperationResult("Successfully updated credential", true);
        } catch (Exception e) {
            e.printStackTrace();
            return new OperationResult("Failed to update credential", false);
        }
    }

    public static OperationResult deleteCredentials(String credentialId) {
        try {
            HttpResponse<String> response = HttpComms.sendDeleteRequestWithAuth("/api/credentials/delete/" + credentialId, AppContext.getJwtToken());

            if (response.statusCode() != 200) {
                ApiError apiError = DtoHandler.parseToDto(response, ApiError.class);
                return new OperationResult(apiError.getMessage(), false);
            }

            return new OperationResult("Successfully deleted credential", true);
        } catch (Exception e) {
            e.printStackTrace();
            return new OperationResult("Failed to delete credential", false);
        }
    }
}
