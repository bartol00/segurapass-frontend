package com.example.passwordmanagerclient.service;

import xyz.segurapass.sdk.service.impl.UptimeServiceImpl;

public class UptimeService {

    public static boolean getUptime(String serverUrl) {
        try {
            xyz.segurapass.sdk.service.UptimeService service = new UptimeServiceImpl();
            return service.getUptime(serverUrl);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

}
