package com.example.passwordmanagerclient.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.http.HttpResponse;
import java.util.Map;

public class DtoHandler {

    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public static <T> T parseToDto(HttpResponse<String> response, Class<T> clazz) {
        try {
            Map<String, Object> outer = mapper.readValue(response.body(), Map.class);
            String innerBody = (String) outer.get("body");
            return mapper.readValue(innerBody, clazz);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Failed to parse response to DTO: " + e.getMessage(), e);
        }
    }

    public static <T> T parseToDto(HttpResponse<String> response, TypeReference<T> typeRef) {
        try {
            Map<String, Object> outer = mapper.readValue(response.body(), Map.class);
            String innerBody = (String) outer.get("body");
            return mapper.readValue(innerBody, typeRef);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Failed to parse response to DTO: " + e.getMessage(), e);
        }
    }

    public static Integer getStatusCode(HttpResponse<String> response) {
        try {
            Map<String, Object> outer = mapper.readValue(response.body(), Map.class);
            return (Integer) outer.get("statusCode");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Failed to parse response to DTO: " + e.getMessage(), e);
        }
    }
}

