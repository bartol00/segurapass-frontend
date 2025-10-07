package com.example.passwordmanagerclient.api.authorization;

import lombok.Data;

import java.util.UUID;

@Data
public class RefreshReq {
    private String email;
    private UUID deviceId;
    private String refreshToken;
}
