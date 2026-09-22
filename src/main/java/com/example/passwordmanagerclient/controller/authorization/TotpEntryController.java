package com.example.passwordmanagerclient.controller.authorization;

import com.example.passwordmanagerclient.controller.StageManager;
import com.example.passwordmanagerclient.controller.mfa.OtpInputController;
import com.example.passwordmanagerclient.service.MfaService;
import com.example.passwordmanagerclient.util.AppContext;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class TotpEntryController {

    @FXML private Button enterButton;
    @FXML private Button cancelButton;
    @FXML private Label statusLabel;

    @FXML private OtpInputController otpInputController;

    public boolean loggedIn = false;

    @FXML
    public void initialize(){
        enterButton.disableProperty()
                .bind(otpInputController.completeProperty().not());
    }

    @FXML
    private void onEnterClick() {
        cancelButton.setDisable(true);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                MfaService.loginTotp(AppContext.getTotpCode(), otpInputController.getOtp());
                return null;
            }
        };

        task.setOnSucceeded(event -> {
            Stage stage = (Stage) enterButton.getScene().getWindow();
            stage.close();
            try {
                StageManager.switchScene("/com/example/passwordmanagerclient/credentials/credentials-view.fxml");
            } catch (Exception e) {
                statusLabel.setText(e.getMessage());
            }
            loggedIn = true;
        });

        task.setOnFailed(event -> {
            cancelButton.setDisable(false);
            statusLabel.setText("TOTP verification failed");
            statusLabel.setStyle("-fx-text-fill: red;");
            otpInputController.clear();
        });

        new Thread(task).start();
    }

    @FXML
    private void onCancelClick() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

}
