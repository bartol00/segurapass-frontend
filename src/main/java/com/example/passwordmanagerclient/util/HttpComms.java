package com.example.passwordmanagerclient.util;

import com.example.passwordmanagerclient.config.AppConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpComms {

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static HttpResponse<String> sendPostRequest(Object dto, String endpoint) throws IOException, InterruptedException {
        String jsonBody = mapper.writeValueAsString(dto);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getBackendUrl() + endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

}
