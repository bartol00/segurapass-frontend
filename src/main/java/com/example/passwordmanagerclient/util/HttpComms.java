package com.example.passwordmanagerclient.util;

import com.example.passwordmanagerclient.config.AppConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpComms {

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static HttpResponse<String> sendGetRequestWithAuth(String endpoint, String jwtToken) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getBackendUrl() + endpoint))
                .header("Authorization", "Bearer " + jwtToken)
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static HttpResponse<String> sendGetRequestWithAuthAndParams(String endpoint,
                                                                       Map<String, String> queryParams,
                                                                       String jwtToken) throws Exception {
        String base = AppConfig.getBackendUrl() + endpoint;
        String query = "";
        if (queryParams != null && !queryParams.isEmpty()) {
            query = queryParams.entrySet().stream()
                    .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "=" +
                            URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                    .collect(Collectors.joining("&", "?", ""));
        }
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(base + query))
                .header("Authorization", "Bearer " + jwtToken)
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }


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
