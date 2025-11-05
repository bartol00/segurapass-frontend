package com.example.passwordmanagerclient.config;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Properties;

public class AppConfig {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = AppConfig.class.getResourceAsStream("/config.properties")) {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getBackendUrl() {
        return properties.getProperty("backend.url");
    }

    public static String getCurrentVersionNumber() {
        return properties.getProperty("version.current");
    }

    public static LocalDate getCurrentVersionDate() {
        return LocalDate.parse(properties.getProperty("version.date"));
    }

    public static int getTimeoutSeconds() {
        return Integer.parseInt(properties.getProperty("timeout.seconds", "10"));
    }
}

