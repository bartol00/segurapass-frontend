package com.example.passwordmanagerclient.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ServerUrlManager {

    private static final String FILE_NAME = "server_url.txt";

    public static String getServerUrl() {
        try {
            Path folderPath = getAppDataFolder();
            Path filePath = folderPath.resolve(FILE_NAME);

            if (Files.exists(filePath)) {
                return Files.readString(filePath).trim();
            } else {
                return null;
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read or create device ID file", e);
        }
    }

    public static void createServerUrl() {

    }

    private static Path getAppDataFolder() {
        return Paths.get(System.getenv("APPDATA"), "SeguraPass");
    }

}
