package com.example.passwordmanagerclient.controller.credentials;

import xyz.segurapass.sdk.models.DecryptedCredential;
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

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CredentialEditController {

    @Setter
    private CredentialsController parentController;

    @FXML private TextField websiteField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Label statusLabel;

    private String credentialId;

    public void loadCredentialData(String id, String website, String username) {
        this.credentialId = id;
        this.websiteField.setText(website);
        this.usernameField.setText(username);
    }

    @FXML
    private void onSave() {
        saveButton.setDisable(true);
        cancelButton.setDisable(true);

        try {
            TokenManager.ensureValidJwt();
        } catch (Exception e) {
            parentController.handleChildExceptions();
            return;
        }

        List<DecryptedCredential> cache = AppContext.getCredentialsCache();

        DecryptedCredential oldCredential = cache.stream()
                .filter(c -> c.getCredentialsId().equals(UUID.fromString(credentialId)))
                .findAny().orElse(null);

        if (oldCredential == null) {
            statusLabel.setText("Failed to find credential to update");
            saveButton.setDisable(false);
            cancelButton.setDisable(false);
        }

        DecryptedCredential decryptedCredential = CredentialsService.updateCredentials(
                credentialId,
                websiteField.getText(),
                usernameField.getText(),
                passwordField.getText()
        );

        if (websiteField.getText() == null || websiteField.getText().isBlank()) {
            Objects.requireNonNull(decryptedCredential).setWebsite(
                    Objects.requireNonNull(oldCredential).getWebsite()
            );
        }
        if (usernameField.getText() == null || usernameField.getText().isBlank()) {
            Objects.requireNonNull(decryptedCredential).setUsername(
                    Objects.requireNonNull(oldCredential).getUsername()
            );
        }
        if (passwordField.getText() == null || passwordField.getText().isBlank()) {
            Objects.requireNonNull(decryptedCredential).setPassword(
                    Objects.requireNonNull(oldCredential).getPassword()
            );
        }

        if (decryptedCredential != null) {
            List<DecryptedCredential> updatedCache = cache.stream()
                    .map(c -> c.getCredentialsId().equals(decryptedCredential.getCredentialsId()) ? decryptedCredential : c)
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
            cancelButton.setDisable(false);
        }
    }

    @FXML
    private void onCancel() {
        Stage stage = (Stage) websiteField.getScene().getWindow();
        stage.close();
    }
}
