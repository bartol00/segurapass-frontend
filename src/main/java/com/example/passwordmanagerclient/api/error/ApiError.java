package com.example.passwordmanagerclient.api.error;

import lombok.Data;

import java.time.Instant;

@Data
public class ApiError {
    private String httpStatus;
    private String message;
    private Instant timestamp;
}
