package com.example.passwordmanagerclient.controller.password_change;

import com.example.passwordmanagerclient.controller.FieldHelpers;
import com.example.passwordmanagerclient.controller.credentials.CredentialsController;
import com.example.passwordmanagerclient.service.PasswordChangeService;
import com.example.passwordmanagerclient.util.TokenManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import lombok.Setter;

import java.util.regex.Pattern;

public class PasswordChangeController {

    @Setter
    private CredentialsController parentController;

    @FXML private PasswordField oldPassword;
    @FXML private PasswordField newPassword;
    @FXML private PasswordField repeatNewPassword;
    @FXML private ProgressBar strengthBar;
    @FXML private Label strengthLabel;
    @FXML private Label lengthReq;
    @FXML private Label lowercaseReq;
    @FXML private Label uppercaseReq;
    @FXML private Label numberReq;
    @FXML private Label specialReq;
    @FXML private Label passwordMatchReq;
    @FXML private Label statusLabel;
    @FXML private Button passwordChangeButton;
    @FXML private Button cancelButton;

    private static final Pattern LOWERCASE = Pattern.compile(".*[a-z].*");
    private static final Pattern UPPERCASE = Pattern.compile(".*[A-Z].*");
    private static final Pattern NUMBER = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL = Pattern.compile(".*[@$!%*?&#^()_+=\\-].*");
    private static final int MIN_LENGTH = 14;

    @FXML
    public void initialize() {
        newPassword.textProperty().addListener(
                (obs, oldVal, newVal) -> updateStrength(newVal)
        );
        newPassword.textProperty().addListener(
                (obs, oldVal, newVal) -> checkPasswordMatch()
        );
        repeatNewPassword.textProperty().addListener(
                (obs, oldVal, newVal) -> checkPasswordMatch()
        );
    }

    private void updateStrength(String password) {
        boolean hasLower = LOWERCASE.matcher(password).matches();
        boolean hasUpper = UPPERCASE.matcher(password).matches();
        boolean hasNum = NUMBER.matcher(password).matches();
        boolean hasSpecial = SPECIAL.matcher(password).matches();
        boolean longEnough = password.length() >= MIN_LENGTH;

        updateLabel(lengthReq, longEnough);
        updateLabel(lowercaseReq, hasLower);
        updateLabel(uppercaseReq, hasUpper);
        updateLabel(numberReq, hasNum);
        updateLabel(specialReq, hasSpecial);

        int score = 0;
        if (longEnough) score++;
        if (hasLower) score++;
        if (hasUpper) score++;
        if (hasNum) score++;
        if (hasSpecial) score++;

        double progress = score / 5.0;
        strengthBar.setProgress(progress);

        Color color;
        String desc;
        if (score <= 2) {
            color = Color.RED;
            desc = "Weak";
        } else if (score == 3) {
            color = Color.ORANGE;
            desc = "Moderate";
        } else if (score == 4) {
            color = Color.YELLOWGREEN;
            desc = "Strong";
        } else {
            color = Color.GREEN;
            desc = "Very Strong";
        }

        strengthBar.setStyle(String.format("-fx-accent: %s;", toRgbString(color)));
        strengthLabel.setText("Password strength: " + desc);

        passwordChangeButton.setDisable(score < 5);
    }

    private void checkPasswordMatch() {
        if (newPassword.getText().isBlank() && repeatNewPassword.getText().isBlank()) {
            updateLabel(passwordMatchReq, false);
            passwordChangeButton.setDisable(true);
            return;
        }
        boolean passwordMatches = newPassword.getText().equals(repeatNewPassword.getText());
        updateLabel(passwordMatchReq, passwordMatches);
        passwordChangeButton.setDisable(!passwordMatches);
    }

    private void updateLabel(Label label, boolean valid) {
        label.setTextFill(valid ? Color.GREEN : Color.GRAY);
    }

    private String toRgbString(Color c) {
        return String.format("rgb(%d, %d, %d)",
                (int)(c.getRed() * 255),
                (int)(c.getGreen() * 255),
                (int)(c.getBlue() * 255));
    }

    @FXML
    private void onChange() {
        passwordChangeButton.setDisable(true);
        cancelButton.setDisable(true);

        try {
            TokenManager.ensureValidJwt();
        } catch (Exception e) {
            parentController.handleChildExceptions();
            return;
        }

        String oldPasswordText = oldPassword.getText();
        String newPasswordText = newPassword.getText();
        String repeatNewPasswordText = repeatNewPassword.getText();

        if (oldPasswordText.isBlank() || newPasswordText.isBlank() || repeatNewPasswordText.isBlank()) {
            statusLabel.setText("Please enter all fields");
            statusLabel.setStyle("-fx-text-fill: red;");
            passwordChangeButton.setDisable(false);
            cancelButton.setDisable(false);
            return;
        }

        if (oldPasswordText.equals(newPasswordText)) {
            statusLabel.setText("Old and new passwords cannot be the same");
            statusLabel.setStyle("-fx-text-fill: red;");
            passwordChangeButton.setDisable(false);
            cancelButton.setDisable(false);
            return;
        }

        if (!newPasswordText.equals(repeatNewPasswordText)) {
            statusLabel.setText("New passwords do not match");
            statusLabel.setStyle("-fx-text-fill: red;");
            passwordChangeButton.setDisable(false);
            cancelButton.setDisable(false);
            return;
        }

        char[] oldPasswordBytes = FieldHelpers.extractPassword(oldPassword);
        char[] newPasswordBytes = FieldHelpers.extractPassword(newPassword);

        try {
            PasswordChangeService.changePassword(oldPasswordBytes, newPasswordBytes);
            Stage stage = (Stage) passwordChangeButton.getScene().getWindow();
            stage.close();
            if (parentController != null) {
                parentController.handleChildExceptions();
            }
        } catch (Exception e) {
            oldPassword.setText("");
            newPassword.setText("");
            repeatNewPassword.setText("");
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText(e.getMessage());
            passwordChangeButton.setDisable(false);
            cancelButton.setDisable(false);
        } finally {
            FieldHelpers.clearPassword(oldPasswordBytes);
            FieldHelpers.clearPassword(newPasswordBytes);
        }
    }

    @FXML
    private void onCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

}
