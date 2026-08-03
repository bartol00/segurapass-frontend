package com.example.passwordmanagerclient.service;

import com.example.passwordmanagerclient.util.AppContext;
import xyz.segurapass.sdk.models.ClientLatestVersion;
import xyz.segurapass.sdk.models.ClientVersion;
import xyz.segurapass.sdk.models.VersionModel;

public class VersionService {

    public static VersionModel getVersionInfo() {
        try {
            return version().getVersionInfo();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    public static ClientLatestVersion getClientLatestVersion(String url, String suffix) {
        try {
            return version().getClientLatestVersion(url, suffix);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    public static ClientVersion getClientVersion(String url, String suffix) {
        try {
            return version().getClientVersion(url, suffix);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    public static byte[] getBytes(String url) {
        try {
            return version().getBytes(url);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    private static xyz.segurapass.sdk.service.VersionService version() {
        return AppContext.getSegurapassClient().version();
    }

}
