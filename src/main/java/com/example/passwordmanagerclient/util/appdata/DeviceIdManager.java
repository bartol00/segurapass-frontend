package com.example.passwordmanagerclient.util.appdata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class DeviceIdManager {

    private static final String FILE_NAME = "device_id.txt";

    public static String getDeviceId() {
        try {
            Path folderPath = FilepathConstants.APPDATA_PATH;
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

}
