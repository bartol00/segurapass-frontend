package com.example.passwordmanagerclient.api.authorization;

import lombok.Data;

import java.util.UUID;

@Data
public class LoginStartResp {
    private String B;
    private String saltAuth;
}
