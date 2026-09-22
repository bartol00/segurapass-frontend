package com.example.passwordmanagerclient.controller.authorization;

import com.example.passwordmanagerclient.config.AppConfig;
import com.example.passwordmanagerclient.controller.DialogManager;
import com.example.passwordmanagerclient.controller.StageManager;
import com.example.passwordmanagerclient.controller.deletion.RemoteDeletionController;
import com.example.passwordmanagerclient.controller.uptime.ServerSelectionDialogController;
import com.example.passwordmanagerclient.service.AuthService;
import com.example.passwordmanagerclient.util.*;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.regex.Pattern;

import static com.example.passwordmanagerclient.controller.FieldHelpers.*;

public class RegisterController {

    @FXML private TextField emailField;
    @FXML private PasswordField masterPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ProgressBar strengthBar;
    @FXML private Label strengthLabel;
    @FXML private Label lengthReq;
    @FXML private Label lowercaseReq;
    @FXML private Label uppercaseReq;
    @FXML private Label numberReq;
    @FXML private Label specialReq;
    @FXML private Label passwordMatchReq;
    @FXML private Button registerButton;
    @FXML private Button switchLoginButton;
    @FXML private Button remoteDeleteButton;
    @FXML private Button serverButton;
    @FXML private Label statusLabel;
    @FXML private Label serverLabel;
    @FXML private Label versionLabel;

    private static final Pattern LOWERCASE = Pattern.compile(".*[a-z].*");
    private static final Pattern UPPERCASE = Pattern.compile(".*[A-Z].*");
    private static final Pattern NUMBER = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL = Pattern.compile(".*[@$!%*?&#^()_+=\\-].*");
    private static final int MIN_LENGTH = 14;

    @FXML
    public void initialize() {
        masterPasswordField.textProperty().addListener(
                (obs, oldVal, newVal) -> updateStrength(newVal)
        );
        masterPasswordField.textProperty().addListener(
                (obs, oldVal, newVal) -> checkPasswordMatch()
        );
        confirmPasswordField.textProperty().addListener(
                (obs, oldVal, newVal) -> checkPasswordMatch()
        );
        remoteDeleteButton.setDisable(!AppContext.isEmailClientActive());
        updateServerUrl();
        updateVersionLabel();
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

    private void checkPasswordMatch() {
        String password = masterPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        if (password.isBlank() && confirmPassword.isBlank()) {
            updateLabel(passwordMatchReq, false);
            registerButton.setDisable(true);
            return;
        }
        boolean passwordMatches = confirmPassword.equals(password);
        updateLabel(passwordMatchReq, passwordMatches);
        registerButton.setDisable(!passwordMatches);
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

        statusLabel.setText("");

        if (email.isBlank() || password.isBlank()) {
            statusLabel.setText("Please fill in both fields");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        statusLabel.setText("Registering...");
        statusLabel.setStyle("-fx-text-fill: blue;");
        registerButton.setDisable(true);
        switchLoginButton.setDisable(true);
        remoteDeleteButton.setDisable(true);
        serverButton.setDisable(true);

        char[] masterPasswordChars = extractPassword(masterPasswordField);

        Task<Void> task = getVoidTask(email, masterPasswordChars);

        new Thread(task).start();
    }

    private Task<Void> getVoidTask(String email, char[] masterPasswordChars) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    AuthService.register(email, masterPasswordChars);
                    return null;
                } finally {
                    clearPassword(masterPasswordChars);
                }
            }
        };

        task.setOnSucceeded(event -> {
            statusLabel.setStyle("-fx-text-fill: green;");
            statusLabel.setText(registrationSuccessMessage());
            emailField.setText("");
            postRegister();
        });

        task.setOnFailed(event -> {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText(task.getException().getMessage());
            postRegister();
        });
        return task;
    }

    @FXML
    protected void onSwitchToLogin() {
        try {
            StageManager.switchScene("/com/example/passwordmanagerclient/authorization/login-view.fxml");
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

            result.controller().setRegisterController(this);
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

    private String registrationSuccessMessage() {
        String message = "Registration successful";
        if (AppContext.isEmailClientActive()) {
            message += ". Please verify the email address you entered before attempting to log in";
        }
        return message;
    }

    private void postRegister() {
        masterPasswordField.setText("");
        confirmPasswordField.setText("");
        switchLoginButton.setDisable(false);
        remoteDeleteButton.setDisable(!AppContext.isEmailClientActive());
        serverButton.setDisable(false);
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
}
