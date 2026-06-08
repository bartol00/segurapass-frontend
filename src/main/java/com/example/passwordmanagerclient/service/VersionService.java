package com.example.passwordmanagerclient.service;

import com.example.passwordmanagerclient.util.AppContext;
import xyz.segurapass.sdk.models.VersionModel;

public class VersionService {

    public static VersionModel getVersionInfo() {
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
