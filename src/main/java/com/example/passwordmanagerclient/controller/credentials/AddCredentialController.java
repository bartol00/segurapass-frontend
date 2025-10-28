package com.example.passwordmanagerclient.controller.credentials;

import com.example.passwordmanagerclient.api.credentials.CredentialsResp;
import com.example.passwordmanagerclient.service.CredentialsService;
import com.example.passwordmanagerclient.util.AppContext;
import com.example.passwordmanagerclient.util.OperationResult;
import com.example.passwordmanagerclient.util.TokenManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Setter;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class AddCredentialController {

    @Setter
    private CredentialsController parentController;

    @FXML private TextField websiteField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;
    @FXML private Button saveButton;

    public void initialize() {
        saveButton.setDisable(true);
        websiteField.textProperty().addListener((obs, oldVal, newVal) -> disableSaveButton());
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> disableSaveButton());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> disableSaveButton());
    }

    private void disableSaveButton() {
        String website = websiteField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        saveButton.setDisable(website.isBlank() || username.isBlank() || password.isBlank());
    }

    @FXML
    private void onSaveClick() {
        saveButton.setDisable(true);
        try {
            TokenManager.ensureValidJwt();
        } catch (Exception e) {
            parentController.handleChildExceptions();
            return;
        }

        String website = websiteField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (website.isBlank() || username.isBlank() || password.isBlank()) {
            statusLabel.setText("All fields are required");
            return;
        }

        CredentialsResp credentialsResp = CredentialsService.addCredential(website, username, password);

        if (credentialsResp != null) {
            List<CredentialsResp> cache = AppContext.getCredentialsCache();
            cache.add(credentialsResp);

            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.close();

            if (parentController != null) {
                parentController.refreshTable();
            }
        } else {
            statusLabel.setText("Failed to add credentials");
            saveButton.setDisable(false);
        }
    }

    @FXML
    private void onCancelClick() {
        Stage stage = (Stage) statusLabel.getScene().getWindow();
        stage.close();
    }
}
