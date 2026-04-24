package com.example.passwordmanagerclient.service;

import com.example.passwordmanagerclient.util.AppContext;
import com.segurapass.exception.SdkException;

import java.security.PublicKey;

public class KeyService {

    public static PublicKey getPublicKey() {
        try {
            return keys().getPublicKey();
        } catch (SdkException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    private static com.segurapass.service.KeyService keys() {
        return AppContext.getSegurapassClient().keys();
    }

}
