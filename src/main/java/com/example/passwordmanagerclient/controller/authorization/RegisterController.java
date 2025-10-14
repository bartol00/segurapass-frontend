package com.example.passwordmanagerclient.controller.authorization;

import com.example.passwordmanagerclient.controller.SceneManager;
import com.example.passwordmanagerclient.service.AuthService;
import com.example.passwordmanagerclient.util.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.regex.Pattern;

public class RegisterController {

    @FXML private TextField emailField;
    @FXML private PasswordField masterPasswordField;
    @FXML private ProgressBar strengthBar;
    @FXML private Label strengthLabel;
    @FXML private Label lengthReq;
    @FXML private Label lowercaseReq;
    @FXML private Label uppercaseReq;
    @FXML private Label numberReq;
    @FXML private Label specialReq;
    @FXML private Button registerButton;
    @FXML private Label statusLabel;

    private static final Pattern LOWERCASE = Pattern.compile(".*[a-z].*");
    private static final Pattern UPPERCASE = Pattern.compile(".*[A-Z].*");
    private static final Pattern NUMBER = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL = Pattern.compile(".*[@$!%*?&#^()_+=\\-].*");
    private static final int MIN_LENGTH = 14;

    @FXML
    public void initialize() {
        masterPasswordField.textProperty().addListener((obs, oldVal, newVal) -> updateStrength(newVal));
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

        registerButton.setDisable(score < 5);
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
    protected void onRegisterClick() {
        String email = emailField.getText();
        String password = masterPasswordField.getText();

        if (email.isBlank() || password.isBlank()) {
            statusLabel.setText("Please fill in both fields");
            return;
        }

        OperationResult operationResult = AuthService.registerSrp(email, password);
        statusLabel.setText(operationResult.getMessage());

        if (operationResult.isPassed()) {
            switchToCredentialsView();
        }
    }

    @FXML
    protected void onSwitchToLogin() {
        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/passwordmanagerclient/authorization/login-view.fxml"));
//            Stage stage = (Stage) emailField.getScene().getWindow();
//            stage.setScene(new Scene(loader.load()));
            Stage stage = (Stage) emailField.getScene().getWindow();
            SceneManager.switchScene(stage, "/com/example/passwordmanagerclient/authorization/login-view.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void switchToCredentialsView() {
        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/passwordmanagerclient/credentials/credentials-view.fxml"));
//            Stage stage = (Stage) emailField.getScene().getWindow();
//            stage.setScene(new Scene(loader.load(), 800, 600));
            Stage stage = (Stage) emailField.getScene().getWindow();
            SceneManager.switchScene(stage, "/com/example/passwordmanagerclient/credentials/credentials-view.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Failed to open credentials view.");
        }
    }
}
