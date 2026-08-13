package com.example.passwordmanagerclient.controller.mfa;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import java.util.Arrays;
import java.util.stream.Collectors;

public class OtpInputController {

    @FXML private TextField otp1;
    @FXML private TextField otp2;
    @FXML private TextField otp3;
    @FXML private TextField otp4;
    @FXML private TextField otp5;
    @FXML private TextField otp6;

    private TextField[] fields;

    @FXML
    private void initialize() {
        fields = new TextField[]{
                otp1, otp2, otp3,
                otp4, otp5, otp6
        };

        setupFields();
    }

    private void setupFields() {
        for (int i = 0; i < fields.length; i++) {
            final int index = i;
            TextField field = fields[i];

            field.setTextFormatter(
                    new TextFormatter<String>(change -> {
                        String newText = change.getControlNewText();
                        if (newText.matches("\\d?")) {
                            return change;
                        }
                        return null;
                    })
            );

            field.textProperty().addListener(
                    (obs, oldValue, newValue) -> {
                        if (!newValue.isEmpty()
                                && index < fields.length - 1) {
                            fields[index + 1].requestFocus();
                        }
                    }
            );

            field.setOnKeyPressed(event -> {
                switch (event.getCode()) {
                    case BACK_SPACE -> {
                        if (!field.getText().isEmpty()) {
                            // Current box contains a digit -> just delete it
                            field.clear();
                        } else if (index > 0) {
                            // Current box is empty -> go back and delete previous
                            fields[index - 1].clear();
                            fields[index - 1].requestFocus();
                        }
                    }

                    case LEFT -> {
                        if (index > 0) {
                            fields[index - 1].requestFocus();
                        }
                    }

                    case RIGHT -> {
                        if (index < fields.length - 1) {
                            fields[index + 1].requestFocus();
                        }
                    }
                }
            });
        }
    }

    public String getOtp() {
        return Arrays.stream(fields)
                .map(TextField::getText)
                .collect(Collectors.joining());
    }

    public boolean isIncomplete() {
        return getOtp().length() != 6;
    }

    public void clear() {
        for (TextField field : fields) {
            field.clear();
        }
        otp1.requestFocus();
    }

    public void focus() {
        otp1.requestFocus();
    }
}
