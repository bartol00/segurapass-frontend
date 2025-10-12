package com.example.passwordmanagerclient.controller.credentials;

import com.example.passwordmanagerclient.api.credentials.CredentialsResp;
import com.example.passwordmanagerclient.service.CredentialsService;
import com.example.passwordmanagerclient.util.AppContext;
import com.example.passwordmanagerclient.util.TokenManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import lombok.Setter;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class DeleteConfirmationController {

    private String credentialId;
    @Setter
    private CredentialsController parentController;

    @FXML private Button yesButton;
    @FXML private Button noButton;

    public void loadCredential(String credentialId) {
        this.credentialId = credentialId;
    }

    @FXML
    private void onYesDelete() {
        try {
            TokenManager.ensureValidJwt();
        } catch (Exception e) {
            parentController.handleChildExceptions();
            return;
        }

        CredentialsService.deleteCredentials(credentialId);

        List<CredentialsResp> cache = AppContext.getCredentialsCache();
        cache.removeIf(c -> c.getCredentialsId().equals(UUID.fromString(credentialId)));
        cache.sort(Comparator.comparing(CredentialsResp::getWebsite, String.CASE_INSENSITIVE_ORDER));

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
