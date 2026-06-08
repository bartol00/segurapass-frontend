package com.example.passwordmanagerclient.service;

import com.example.passwordmanagerclient.util.AppContext;
import xyz.segurapass.sdk.exception.SegurapassSdkException;

import java.security.PublicKey;

public class KeyService {

    public static PublicKey getPublicKey() {
        try {
            return keys().getPublicKey();
        } catch (SegurapassSdkException e) {
            System.err.println(e.getMessage());
        } catch (Exception e) {
            System.err.println("Exception occurred when getting public key: " + e.getMessage());
        }
        return null;
    }

    private static xyz.segurapass.sdk.service.KeyService keys() {
        return AppContext.getSegurapassClient().keys();
    }

}
