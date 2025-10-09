package com.example.passwordmanagerclient.service;

import com.example.passwordmanagerclient.api.credentials.CredentialsResp;
import com.example.passwordmanagerclient.api.credentials.PagedResponse;
import com.example.passwordmanagerclient.api.error.ApiError;
import com.example.passwordmanagerclient.util.AppContext;
import com.example.passwordmanagerclient.util.DtoHandler;
import com.example.passwordmanagerclient.util.HttpComms;
import com.fasterxml.jackson.core.type.TypeReference;

import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.Map;

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

            return DtoHandler.parseToDto(
                    response,
                    new TypeReference<>() {
                    }
            );
        } catch (Exception e) {
            e.printStackTrace();
            PagedResponse<CredentialsResp> emptyPage = new PagedResponse<>();
            emptyPage.setContent(Collections.emptyList());
            return emptyPage;
        }
    }

}
