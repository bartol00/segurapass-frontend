package com.example.passwordmanagerclient.controller.authorization;

import com.example.passwordmanagerclient.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegisterController {

    @FXML private TextField emailField;
    @FXML private PasswordField masterPasswordField;
    @FXML private Label statusLabel;

    @FXML
    protected void onRegisterClick() {
        String email = emailField.getText();
        String password = masterPasswordField.getText();

        if (email.isBlank() || password.isBlank()) {
            statusLabel.setText("Please fill in both fields.");
            return;
        }

        String status = AuthService.register(email, password);
        statusLabel.setText(status);
    }

    @FXML
    protected void onSwitchToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/passwordmanagerclient/authorization/login-view.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 400, 300));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
