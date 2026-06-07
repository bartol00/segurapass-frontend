package com.example.passwordmanagerclient.controller.credentials;

import com.segurapass.models.credentials.DecryptedCredential;
import com.example.passwordmanagerclient.service.CredentialsService;
import com.example.passwordmanagerclient.util.AppContext;
import com.example.passwordmanagerclient.util.TokenManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

public class DeleteConfirmationController {

    @Setter
    private CredentialsController parentController;

    @FXML private Button yesButton;
    @FXML private Button noButton;

    private String credentialId;

    public void loadCredential(String credentialId) {
        this.credentialId = credentialId;
    }

    @FXML
    private void onYesDelete() {
        yesButton.setDisable(true);
        noButton.setDisable(true);
        try {
            TokenManager.ensureValidJwt();
        } catch (Exception e) {
            parentController.handleChildExceptions();
            return;
        }

        CredentialsService.deleteCredentials(credentialId);

        List<DecryptedCredential> cache = AppContext.getCredentialsCache();
        cache.removeIf(c -> c.getCredentialsId().equals(UUID.fromString(credentialId)));

        this.credentialId = null;

        Stage stage = (Stage) yesButton.getScene().getWindow();
        stage.close();

        if (parentController != null) {
            parentController.refreshTable();
        }
    }

    @FXML
    private void onNo() {
        this.credentialId = null;
        Stage stage = (Stage) noButton.getScene().getWindow();
        stage.close();
    }
}
