package com.example.passwordmanagerclient.controller.password_change;

import com.example.passwordmanagerclient.controller.FieldHelpers;
import com.example.passwordmanagerclient.controller.credentials.CredentialsController;
import com.example.passwordmanagerclient.service.PasswordChangeService;
import com.example.passwordmanagerclient.util.OperationResult;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import lombok.Setter;

public class PasswordChangeController {

    @Setter
    private CredentialsController parentController;

    @FXML private PasswordField oldPassword;
    @FXML private PasswordField newPassword;
    @FXML private PasswordField repeatNewPassword;

    @FXML private Label statusLabel;

    @FXML private Button passwordChangeButton;
    @FXML private Button cancelButton;

    @FXML
    private void onChange() {
        passwordChangeButton.setDisable(true);
        cancelButton.setDisable(true);

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
        OperationResult result = PasswordChangeService.changePassword(oldPasswordBytes, newPasswordBytes);
        FieldHelpers.clearPassword(oldPasswordBytes);
        FieldHelpers.clearPassword(newPasswordBytes);

        if (!result.isPassed()) {
            oldPassword.setText("");
            newPassword.setText("");
            repeatNewPassword.setText("");
            statusLabel.setText(result.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
            passwordChangeButton.setDisable(false);
            cancelButton.setDisable(false);
            return;
        }

        Stage stage = (Stage) passwordChangeButton.getScene().getWindow();
        stage.close();
        if (parentController != null) {
            parentController.handleChildExceptions();
        }
    }

    @FXML
    private void onCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

}
