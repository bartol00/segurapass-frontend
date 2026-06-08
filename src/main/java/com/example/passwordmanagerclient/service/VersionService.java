package com.example.passwordmanagerclient.service;

import xyz.segurapass.api.versions.VersionInfo;
import com.example.passwordmanagerclient.util.AppContext;

public class VersionService {

    public static VersionInfo getVersionInfo() {
        try {
            return version().getVersionInfo();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    private static xyz.segurapass.sdk.service.VersionService version() {
        return AppContext.getSegurapassClient().version();
    }

}
