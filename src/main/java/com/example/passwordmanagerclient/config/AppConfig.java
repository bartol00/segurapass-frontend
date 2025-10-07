package com.example.passwordmanagerclient.config;

import java.io.IOException;
import java.io.InputStream;
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

    public static int getTimeoutSeconds() {
        return Integer.parseInt(properties.getProperty("timeout.seconds", "10"));
    }
}

