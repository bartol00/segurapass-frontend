package com.example.passwordmanagerclient.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class DeviceIdManager {

    private static final String FILE_NAME = "device_id.txt";
    private static final String APP_FOLDER = ".passwordmanager";

    public static String getDeviceId() {
        try {
            Path folderPath = getAppDataFolder();
            Path filePath = folderPath.resolve(FILE_NAME);

            if (Files.exists(filePath)) {
                return Files.readString(filePath).trim();
            } else {
                String newId = UUID.randomUUID().toString();
                Files.createDirectories(folderPath);
                Files.writeString(filePath, newId);
                return newId;
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read or create device ID file", e);
        }
    }

    public static Path getAppDataFolder() {
//        String userHome = System.getProperty("user.home");
//        return Paths.get(userHome, APP_FOLDER);
        return Paths.get(System.getenv("APPDATA"), "SeguraPass");
    }
}
