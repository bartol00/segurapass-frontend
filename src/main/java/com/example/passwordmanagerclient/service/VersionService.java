package com.example.passwordmanagerclient.service;

import com.example.passwordmanagerclient.api.versions.VersionInfo;
import com.example.passwordmanagerclient.util.DtoHandler;
import com.example.passwordmanagerclient.util.HttpComms;

import java.net.http.HttpResponse;

public class VersionService {

    public static VersionInfo getVersionInfo() {
        try {
            HttpResponse<String> response = HttpComms.sendGetRequest("/api/versions/latest");
            return DtoHandler.parseToDto(response, VersionInfo.class);
        } catch (Exception e) {
            return null;
        }
    }

}
