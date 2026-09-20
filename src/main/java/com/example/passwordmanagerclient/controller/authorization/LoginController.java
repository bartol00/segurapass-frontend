package com.example.passwordmanagerclient.controller.authorization;

import com.example.passwordmanagerclient.config.AppConfig;
import com.example.passwordmanagerclient.controller.DialogManager;
import com.example.passwordmanagerclient.controller.StageManager;
import com.example.passwordmanagerclient.controller.deletion.RemoteDeletionController;
import com.example.passwordmanagerclient.controller.uptime.ServerSelectionDialogController;
import com.example.passwordmanagerclient.service.AuthService;
import com.example.passwordmanagerclient.util.AppContext;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.concurrent.Task;

import static com.example.passwordmanagerclient.controller.FieldHelpers.*;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField masterPasswordField;
    @FXML private Label statusLabel;
    @FXML private Button loginButton;
    @FXML private Button switchRegisterButton;
    @FXML private Button remoteDeleteButton;
    @FXML private Button serverButton;
    @FXML private Label serverLabel;
    @FXML private Label versionLabel;

    public void initialize() {
        emailField.textProperty().addListener(
                (obs, oldVal, newVal) -> disableLoginButton()
        );
        masterPasswordField.textProperty().addListener(
                (obs, oldVal, newVal) -> disableLoginButton()
        );
        updateServerUrl();
        updateVersionLabel();
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
        serverButton.setDisable(true);

        char[] masterPasswordChars = extractPassword(masterPasswordField);
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                AuthService.login(email, masterPasswordChars);
                return null;
            }
        };

        task.setOnSucceeded(event -> {
            if (AppContext.isTotpEnabled()) {
                try {
                    DialogManager.DialogResult<MfaChoiceController> dialogResult =
                            DialogManager.openWindow(
                                    "/com/example/passwordmanagerclient/authorization/mfa-choice.fxml",
                                    "MFA Dashboard",
                                    (Stage) emailField.getScene().getWindow(),
                                    false,
                                    MfaChoiceController.class
                            );
                    dialogResult.stage().setOnCloseRequest(closeEvent -> System.exit(0));
                    dialogResult.stage().showAndWait();
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                    System.exit(1);
                }
            } else {
                switchToCredentialsView();
            }
        });

        task.setOnFailed(event -> {
            statusLabel.setText(task.getException().getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
            masterPasswordField.setText("");
            switchRegisterButton.setDisable(false);
            remoteDeleteButton.setDisable(false);
            serverButton.setDisable(false);
        });

        new Thread(task).start();
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

    @FXML
    protected void onServerChange() {
        try {
            DialogManager.DialogResult<ServerSelectionDialogController> result =
                    DialogManager.openWindow(
                            "/com/example/passwordmanagerclient/uptime/server-selection-dialog-view.fxml",
                            "Change Server URL",
                            (Stage) statusLabel.getScene().getWindow(),
                            false,
                            ServerSelectionDialogController.class
                    );

            result.stage().showAndWait();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            updateServerUrl();
        }
    }

    private void disableLoginButton() {
        String email = emailField.getText();
        String password = masterPasswordField.getText();

        loginButton.setDisable(email.isBlank() || password.isBlank());
    }

    private void updateServerUrl() {
        serverLabel.setText(String.format("Current Server URL: %s", AppContext.getServerUrl()));
    }

    private void updateVersionLabel() {
        versionLabel.setText(String.format("Current App Version: %s", AppConfig.getAppVersion()));
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

