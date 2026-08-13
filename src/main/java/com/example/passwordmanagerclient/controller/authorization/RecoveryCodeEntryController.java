package com.example.passwordmanagerclient.controller.authorization;

import com.example.passwordmanagerclient.controller.StageManager;
import com.example.passwordmanagerclient.service.MfaService;
import com.example.passwordmanagerclient.util.AppContext;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RecoveryCodeEntryController {

    @FXML private TextField recoveryCodeField;
    @FXML private Button enterButton;
    @FXML private Button cancelButton;
    @FXML private Label statusLabel;

    public boolean loggedIn = false;

    @FXML
    public void initialize(){
        statusLabel.setText("");
        statusLabel.setStyle("-fx-text-fill: red;");
    }

    @FXML
    private void onEnterClick() {
        enterButton.setDisable(true);
        cancelButton.setDisable(true);

        try {
            MfaService.recoveryTotp(AppContext.getTotpCode(), recoveryCodeField.getText());
        } catch (Exception e) {
            enterButton.setDisable(false);
            cancelButton.setDisable(false);
            statusLabel.setText(e.getMessage());
            return;
        }

        Stage stage = (Stage) enterButton.getScene().getWindow();
        stage.close();

        try {
            StageManager.switchScene("/com/example/passwordmanagerclient/credentials/credentials-view.fxml");
        } catch (Exception e) {
            statusLabel.setText(e.getMessage());
        }

        loggedIn = true;
    }

    @FXML
    private void onCancelClick() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

}
