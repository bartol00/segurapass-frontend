package com.example.passwordmanagerclient.util;

import lombok.Getter;

public class AuthResult {

    @Getter
    private final String message;

    @Getter
    private final boolean passed;

    public AuthResult(String message, boolean passed) {
        this.message = message;
        this.passed = passed;
    }
}
