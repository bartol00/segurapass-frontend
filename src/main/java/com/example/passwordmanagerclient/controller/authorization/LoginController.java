package com.example.passwordmanagerclient.controller.authorization;

import com.example.passwordmanagerclient.controller.SceneManager;
import com.example.passwordmanagerclient.service.AuthService;
import com.example.passwordmanagerclient.util.OperationResult;
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

        statusLabel.setText("Logging in...");

        javafx.concurrent.Task<OperationResult> task = new javafx.concurrent.Task<>() {
            @Override
            protected OperationResult call() {
                return AuthService.loginSrp(email, password);
            }
        };

        task.setOnSucceeded(event -> {
            OperationResult result = task.getValue();
            statusLabel.setText(result.getMessage());
            if (result.isPassed()) {
                switchToCredentialsView();
            }
        });

        task.setOnFailed(event -> {
            statusLabel.setText("Login failed due to a system error");
            task.getException().printStackTrace();
        });

        new Thread(task).start();
    }


    @FXML
    protected void onSwitchToRegister() {
        try {
            Stage stage = (Stage) emailField.getScene().getWindow();
            SceneManager.switchScene(stage, "/com/example/passwordmanagerclient/authorization/register-view.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void switchToCredentialsView() {
        try {
            Stage stage = (Stage) emailField.getScene().getWindow();
            SceneManager.switchScene(stage, "/com/example/passwordmanagerclient/credentials/credentials-view.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Failed to open credentials view.");
        }
    }
}

