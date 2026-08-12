package com.example.passwordmanagerclient.controller.authorization;

import com.example.passwordmanagerclient.controller.StageManager;
import com.example.passwordmanagerclient.service.MfaService;
import com.example.passwordmanagerclient.util.AppContext;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class TotpEntryController {

    @FXML private TextField otpField;
    @FXML private Button enterButton;
    @FXML private Button cancelButton;

    public boolean loggedIn = false;

    @FXML
    private void onEnterClick() {
        enterButton.setDisable(true);
        cancelButton.setDisable(true);

        try {
            MfaService.loginTotp(AppContext.getTotpCode(), otpField.getText());
        } catch (Exception e) {
            enterButton.setDisable(false);
            cancelButton.setDisable(false);
            System.err.println("Error: " + e.getMessage());
            return;
        }

        Stage stage = (Stage) enterButton.getScene().getWindow();
        stage.close();

        try {
            StageManager.switchScene("/com/example/passwordmanagerclient/credentials/credentials-view.fxml");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        loggedIn = true;
    }

    @FXML
    private void onCancelClick() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

}
