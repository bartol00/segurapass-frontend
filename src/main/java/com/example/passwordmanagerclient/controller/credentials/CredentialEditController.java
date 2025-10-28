package com.example.passwordmanagerclient.controller.credentials;

import com.example.passwordmanagerclient.api.credentials.CredentialsResp;
import com.example.passwordmanagerclient.service.CredentialsService;
import com.example.passwordmanagerclient.util.AppContext;
import com.example.passwordmanagerclient.util.TokenManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Setter;

import java.util.Comparator;
import java.util.List;

public class CredentialEditController {

    @Setter
    private CredentialsController parentController;

    @FXML private TextField websiteField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;
    @FXML private Button saveButton;

    private String credentialId;

    public void loadCredentialData(String id, String website, String username) {
        this.credentialId = id;
        this.websiteField.setText(website);
        this.usernameField.setText(username);
    }

    @FXML
    private void onSave() {
        saveButton.setDisable(true);
        try {
            TokenManager.ensureValidJwt();
        } catch (Exception e) {
            parentController.handleChildExceptions();
            return;
        }

        CredentialsResp credentialsResp = CredentialsService.updateCredentials(
                credentialId,
                websiteField.getText(),
                usernameField.getText(),
                passwordField.getText()
        );

        if (credentialsResp != null) {
            List<CredentialsResp> cache = AppContext.getCredentialsCache();
            List<CredentialsResp> updatedCache = cache.stream()
                    .map(c -> c.getCredentialsId().equals(credentialsResp.getCredentialsId()) ? credentialsResp : c)
                    .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));

            AppContext.setCredentialsCache(updatedCache);

            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.close();

            if (parentController != null) {
                parentController.refreshTable();
            }
        } else {
            statusLabel.setText("Failed to update credentials");
            saveButton.setDisable(false);
        }
    }

    @FXML
    private void onCancel() {
        Stage stage = (Stage) websiteField.getScene().getWindow();
        stage.close();
    }
}
