package com.example.passwordmanagerclient.util;

import com.example.passwordmanagerclient.api.credentials.CredentialsResp;
import com.example.passwordmanagerclient.api.credentials.PagedResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.http.HttpResponse;

public class DtoHandler {

    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public static <T> T parseToDto(HttpResponse<String> response, Class<T> clazz) {
        try {
            return mapper.readValue(response.body(), clazz);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Failed to parse response to DTO: " + e.getMessage(), e);
        }
    }

    public static <T> T parseToDto(HttpResponse<String> response, TypeReference<T> typeRef) {
        try {
            return mapper.readValue(response.body(), typeRef);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Failed to parse response to DTO: " + e.getMessage(), e);
        }
    }
}

