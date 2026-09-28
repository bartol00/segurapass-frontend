package com.example.passwordmanagerclient.util.appdata;

import com.example.passwordmanagerclient.config.AppConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ServerUrlManager {

    private static final String FILE_NAME = "server_url.txt";

    public static String readServerUrl() {
        try {
            Path folderPath = FilepathConstants.APPDATA_PATH;
            Path filePath = folderPath.resolve(FILE_NAME);

            if (Files.exists(filePath)) {
                return Files.readString(filePath).trim();
            } else {
                return AppConfig.getMainServerUrl();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read server URL file", e);
        }
    }

    public static void writeServerUrl(String url) {
        try {
            Path folderPath = FilepathConstants.APPDATA_PATH;
            Path filePath = folderPath.resolve(FILE_NAME);

            if (!Files.exists(filePath)) {
                Files.createDirectories(folderPath);
            }

            Files.writeString(filePath, url);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write server URL file", e);
        }
    }

}
