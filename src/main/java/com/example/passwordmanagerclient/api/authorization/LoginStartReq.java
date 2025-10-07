package com.example.passwordmanagerclient.api.authorization;

import lombok.Data;

import java.util.UUID;

@Data
public class LoginStartReq {
    private String email;
    private UUID deviceId;
}
