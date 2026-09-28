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
            System.err.println(e.getMessage());
        }
    }

    public static String getAppVersion() {
        return properties.getProperty("app.version");
    }

    public static Integer getProtocolVersion() {
        return Integer.parseInt(properties.getProperty("segurapass.protocol.version"));
    }

    public static String getDownloadUrl() {
        return properties.getProperty("download.url");
    }

    public static String getMainServerUrl() {
        return properties.getProperty("main-server.url");
    }

}
