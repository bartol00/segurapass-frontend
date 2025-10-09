package com.example.passwordmanagerclient.controller.authorization;

import com.example.passwordmanagerclient.service.AuthService;
import com.example.passwordmanagerclient.util.AuthResult;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField masterPasswordField;
    @FXML private Label statusLabel;

    @FXML
    protected void onLoginClick() {
        String email = emailField.getText();
        String password = masterPasswordField.getText();

        if (email.isBlank() || password.isBlank()) {
            statusLabel.setText("Please fill in both fields.");
            return;
        }

        AuthResult authResult = AuthService.login(email, password);
        statusLabel.setText(authResult.getMessage());

        if (authResult.isPassed()) {
            switchToCredentialsView();
        }
    }

    @FXML
    protected void onSwitchToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/passwordmanagerclient/authorization/register-view.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 400, 300));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void switchToCredentialsView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/passwordmanagerclient/credentials/credentials-view.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 800, 600));
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Failed to open credentials view.");
        }
    }
}

