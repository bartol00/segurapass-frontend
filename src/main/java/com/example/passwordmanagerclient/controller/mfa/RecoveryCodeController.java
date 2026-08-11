package com.example.passwordmanagerclient.controller.mfa;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RecoveryCodeController {

    @FXML private TextField recoveryCode;

    @FXML
    public void initialize(String recoveryCode){
        this.recoveryCode.textProperty().setValue(recoveryCode);
    }

    @FXML
    private void okClick() {
        Stage stage = (Stage) recoveryCode.getScene().getWindow();
        stage.close();
    }

}
