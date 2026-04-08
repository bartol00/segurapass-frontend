package com.example.passwordmanagerclient.service;

import com.example.passwordmanagerclient.util.AppContext;
import com.segurapass.model.versions.VersionInfo;

public class VersionService {

    public static VersionInfo getVersionInfo() {
        try {
            return version().getVersionInfo();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    private static com.segurapass.service.VersionService version() {
        return AppContext.getSegurapassClient().version();
    }

}
