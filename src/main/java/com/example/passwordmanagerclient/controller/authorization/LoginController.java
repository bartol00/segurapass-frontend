package com.example.passwordmanagerclient.controller.authorization;

import com.example.passwordmanagerclient.controller.DialogManager;
import com.example.passwordmanagerclient.controller.StageManager;
import com.example.passwordmanagerclient.controller.deletion.RemoteDeletionController;
import com.example.passwordmanagerclient.service.AuthService;
import com.example.passwordmanagerclient.util.OperationResult;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import static com.example.passwordmanagerclient.controller.FieldHelpers.*;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField masterPasswordField;
    @FXML private Label statusLabel;
    @FXML private Button loginButton;
    @FXML private Button switchRegisterButton;
    @FXML private Button remoteDeleteButton;

    public void initialize() {
        emailField.textProperty().addListener(
                (obs, oldVal, newVal) -> disableLoginButton()
        );
        masterPasswordField.textProperty().addListener(
                (obs, oldVal, newVal) -> disableLoginButton()
        );
    }

    @FXML
    protected void onLoginClick() {
        String email = emailField.getText();

        if (email.isBlank() || masterPasswordField.getText().isBlank()) {
            statusLabel.setText("Please fill in both fields.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        statusLabel.setText("Logging in...");
        statusLabel.setStyle("-fx-text-fill: blue;");
        loginButton.setDisable(true);
        switchRegisterButton.setDisable(true);
        remoteDeleteButton.setDisable(true);

        char[] masterPasswordChars = extractPassword(masterPasswordField);
        javafx.concurrent.Task<OperationResult> task = new javafx.concurrent.Task<>() {
            @Override
            protected OperationResult call() {
                try {
                    return AuthService.login(email, masterPasswordChars);
                } finally {
                    clearPassword(masterPasswordChars);
                }
            }
        };

        task.setOnSucceeded(event -> {
            OperationResult result = task.getValue();
            if (result.isPassed()) {
                switchToCredentialsView();
            } else {
                masterPasswordField.setText("");
                statusLabel.setText(result.getMessage());
                statusLabel.setStyle("-fx-text-fill: red;");
                switchRegisterButton.setDisable(false);
                remoteDeleteButton.setDisable(false);
            }
        });

        task.setOnFailed(event -> {
            statusLabel.setText("Login failed due to a system error");
            statusLabel.setStyle("-fx-text-fill: red;");
            masterPasswordField.setText("");
            switchRegisterButton.setDisable(false);
            remoteDeleteButton.setDisable(false);
            System.err.println(task.getException().getMessage());
        });

        new Thread(task).start();
    }

    private void disableLoginButton() {
        String email = emailField.getText();
        String password = masterPasswordField.getText();

        loginButton.setDisable(email.isBlank() || password.isBlank());
    }

    @FXML
    protected void onSwitchToRegister() {
        try {
            StageManager.switchScene("/com/example/passwordmanagerclient/authorization/register-view.fxml");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @FXML
    protected void onOpenRemoteDelete() {
        try {
            DialogManager.DialogResult<RemoteDeletionController> result =
                    DialogManager.openWindow(
                            "/com/example/passwordmanagerclient/deletion/remote-deletion-view.fxml",
                            "Remotely Delete Account",
                            (Stage) statusLabel.getScene().getWindow(),
                            false,
                            RemoteDeletionController.class
                    );

            result.controller().setLoginController(this);
            result.stage().showAndWait();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void writeStatusLabel(String message) {
        statusLabel.setStyle("-fx-text-fill: green;");
        statusLabel.setText(message);
    }

    private void switchToCredentialsView() {
        try {
            StageManager.switchScene("/com/example/passwordmanagerclient/credentials/credentials-view.fxml");
        } catch (Exception e) {
            System.err.println(e.getMessage());
            statusLabel.setText("Failed to open credentials view");
        }
    }
}

