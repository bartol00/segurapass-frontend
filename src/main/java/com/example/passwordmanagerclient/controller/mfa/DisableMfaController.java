package com.example.passwordmanagerclient.controller.mfa;

import com.example.passwordmanagerclient.service.MfaService;
import com.example.passwordmanagerclient.util.AppContext;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class DisableMfaController {

    @FXML private Label mfaMethodLabel;
    @FXML private Button yesButton;
    @FXML private Button noButton;

    public void setMfaMethodLabel(String mfaMethod) {
        String labelText = String.format("Are you sure you would like to disable %s as an MFA method?", mfaMethod);
        mfaMethodLabel.setText(labelText);
    }

    @FXML
    private void onYes() {
        yesButton.setDisable(true);
        noButton.setDisable(true);

        try {
            MfaService.removeTotp();
            AppContext.setTotpEnabled(false);
        } catch (Exception e) {
            yesButton.setDisable(false);
            noButton.setDisable(false);
            System.err.println(e.getMessage());
            return;
        }

        Stage stage = (Stage) mfaMethodLabel.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onNo() {
        Stage stage = (Stage) mfaMethodLabel.getScene().getWindow();
        stage.close();
    }

}
