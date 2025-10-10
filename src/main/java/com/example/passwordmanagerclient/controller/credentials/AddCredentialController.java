package com.example.passwordmanagerclient.controller.credentials;

import com.example.passwordmanagerclient.service.CredentialsService;
import com.example.passwordmanagerclient.util.OperationResult;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Setter;

public class AddCredentialController {

    @Setter
    private CredentialsController parentController;

    @FXML private TextField websiteField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    @FXML
    private void onSaveClick() {
        String website = websiteField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (website.isBlank() || username.isBlank() || password.isBlank()) {
            statusLabel.setText("All fields are required");
            return;
        }

        OperationResult result = CredentialsService.addCredential(website, username, password);

        statusLabel.setText(result.getMessage());

        if (result.isPassed()) {
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.close();

            if (parentController != null) {
                parentController.refreshTable();
            }
        }
    }

    @FXML
    private void onCancelClick() {
        Stage stage = (Stage) statusLabel.getScene().getWindow();
        stage.close();
    }
}
