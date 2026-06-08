package com.example.passwordmanagerclient.controller.deletion;

import com.example.passwordmanagerclient.controller.FieldHelpers;
import com.example.passwordmanagerclient.controller.credentials.CredentialsController;
import com.example.passwordmanagerclient.service.DeletionService;
import com.example.passwordmanagerclient.util.OperationResult;
import com.example.passwordmanagerclient.util.TokenManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import lombok.Setter;

public class AuthorizedDeletionController {

    @Setter
    private CredentialsController parentController;

    @FXML private PasswordField masterPassword;
    @FXML private Label statusLabel;
    @FXML private Button deleteButton;
    @FXML private Button cancelButton;

    @FXML
    private void onDelete() {
        deleteButton.setDisable(true);
        cancelButton.setDisable(true);

        try {
            TokenManager.ensureValidJwt();
        } catch (Exception e) {
            parentController.handleChildExceptions();
            return;
        }

        if (masterPassword.getText().isBlank()) {
            statusLabel.setText("Please enter the master password");
            statusLabel.setStyle("-fx-text-fill: red;");
            deleteButton.setDisable(false);
            cancelButton.setDisable(false);
            return;
        }

        char[] masterPasswordChars = FieldHelpers.extractPassword(masterPassword);
        OperationResult result = DeletionService.deleteAuthorized(masterPasswordChars);
        FieldHelpers.clearPassword(masterPasswordChars);

        if (!result.passed()) {
            masterPassword.setText("");
            statusLabel.setText(result.message());
            statusLabel.setStyle("-fx-text-fill: red;");
            deleteButton.setDisable(false);
            cancelButton.setDisable(false);
            return;
        }

        Stage stage = (Stage) deleteButton.getScene().getWindow();
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
