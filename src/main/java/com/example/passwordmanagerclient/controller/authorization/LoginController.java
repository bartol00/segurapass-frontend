package com.example.passwordmanagerclient.controller.authorization;

import com.example.passwordmanagerclient.controller.SceneManager;
import com.example.passwordmanagerclient.controller.deletion.RemoteDeletionController;
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
    @FXML private Button loginButton;
    @FXML private Button switchRegisterButton;

    public void initialize() {
        emailField.textProperty().addListener((obs, oldVal, newVal) -> disableLoginButton());
        masterPasswordField.textProperty().addListener((obs, oldVal, newVal) -> disableLoginButton());
    }

    @FXML
    protected void onLoginClick() {
        String email = emailField.getText();
        String password = masterPasswordField.getText();

        if (email.isBlank() || password.isBlank()) {
            statusLabel.setText("Please fill in both fields.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        statusLabel.setText("Logging in...");
        statusLabel.setStyle("-fx-text-fill: blue;");
        loginButton.setDisable(true);
        switchRegisterButton.setDisable(true);

        javafx.concurrent.Task<OperationResult> task = new javafx.concurrent.Task<>() {
            @Override
            protected OperationResult call() {
                return AuthService.loginSrp(email, password);
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
            }
        });

        task.setOnFailed(event -> {
            statusLabel.setText("Login failed due to a system error");
            statusLabel.setStyle("-fx-text-fill: red;");
            masterPasswordField.setText("");
            switchRegisterButton.setDisable(false);
            task.getException().printStackTrace();
        });

        new Thread(task).start();
    }

    private void disableLoginButton() {
        String email = emailField.getText();
        String password = masterPasswordField.getText();

        if (email.isBlank() || password.isBlank()) {
            loginButton.setDisable(true);
        } else {
            loginButton.setDisable(false);
        }
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

    @FXML
    protected void onOpenRemoteDelete() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/passwordmanagerclient/deletion/remote-deletion-view.fxml"));

            Stage dialogStage = new Stage();
            Scene dialogScene = new Scene(loader.load());
            dialogScene.getStylesheets().add(getClass()
                    .getResource("/com/example/passwordmanagerclient/style/app.css")
                    .toExternalForm()
            );
            dialogStage.setScene(dialogScene);

            dialogStage.setTitle("Remotely Delete Account");
            dialogStage.setResizable(false);
            dialogStage.initModality(javafx.stage.Modality.WINDOW_MODAL);

            Stage parentStage = (Stage) statusLabel.getScene().getWindow();
            dialogStage.initOwner(parentStage);

            RemoteDeletionController controller = loader.getController();
            controller.setLoginController(this);

            dialogStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeStatusLabel(String message) {
        statusLabel.setStyle("-fx-text-fill: green;");
        statusLabel.setText(message);
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

