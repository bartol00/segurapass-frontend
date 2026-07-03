package com.example.passwordmanagerclient.service;

import com.example.passwordmanagerclient.util.AppContext;

public class UptimeService {

    public static boolean getUptime() {
        try {
            return uptime().getUptime();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    private static xyz.segurapass.sdk.service.UptimeService uptime() {
        return AppContext.getSegurapassClient().uptime();
    }

}
