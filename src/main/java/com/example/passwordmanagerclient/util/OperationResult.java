package com.example.passwordmanagerclient.util;

import lombok.Getter;

public class OperationResult {

    @Getter
    private final String message;

    @Getter
    private final boolean passed;

    public OperationResult(String message, boolean passed) {
        this.message = message;
        this.passed = passed;
    }
}
