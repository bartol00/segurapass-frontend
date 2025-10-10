package com.example.passwordmanagerclient.controller.credentials;

import com.example.passwordmanagerclient.service.CredentialsService;
import com.example.passwordmanagerclient.util.OperationResult;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Setter;

public class CredentialEditController {

    @Setter
    private CredentialsController parentController;

    @FXML private TextField websiteField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    private String credentialId;

    public void loadCredentialData(String id, String website, String username) {
        this.credentialId = id;
        this.websiteField.setText(website);
        this.usernameField.setText(username);
    }

    @FXML
    private void onSave() {
        OperationResult result = CredentialsService.updateCredentials(
                credentialId,
                websiteField.getText(),
                usernameField.getText(),
                passwordField.getText()
        );

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
    private void onCancel() {
        Stage stage = (Stage) websiteField.getScene().getWindow();
        stage.close();
    }
}
