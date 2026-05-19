package com.example.passwordmanagerclient.controller;

import javafx.scene.control.PasswordField;

import java.util.Arrays;

public final class FieldHelpers {

    public static char[] extractPassword(PasswordField passwordField) {
        char[] password = passwordField.getText().toCharArray();
        passwordField.clear();
        return password;
    }

    public static void clearPassword(char[] passwordChar) {
        if (passwordChar != null) {
            Arrays.fill(passwordChar, '\0');
        }
    }

}
