package com.example.passwordmanagerclient.controller.authorization;

import com.example.passwordmanagerclient.controller.StageManager;
import com.example.passwordmanagerclient.service.MfaService;
import com.example.passwordmanagerclient.util.AppContext;
import javafx.concurrent.Task;
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
        enterButton.setDisable(true);
        recoveryCodeField.textProperty().addListener(
                (obs, oldVal, newVal) -> disableEnterButton()
        );
    }

    @FXML
    private void onEnterClick() {
        enterButton.setDisable(true);
        cancelButton.setDisable(true);

        statusLabel.setText("Verifying recovery code...");
        statusLabel.setStyle("-fx-text-fill: blue;");

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                MfaService.recoveryTotp(AppContext.getTotpCode(), recoveryCodeField.getText());
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
                statusLabel.setStyle("-fx-text-fill: red;");
            }
            loggedIn = true;
        });

        task.setOnFailed(event -> {
            enterButton.setDisable(true);
            cancelButton.setDisable(false);
            statusLabel.setText(task.getException().getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
            recoveryCodeField.setText("");
        });

        new Thread(task).start();
    }

    @FXML
    private void onCancelClick() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void disableEnterButton() {
        enterButton.setDisable(recoveryCodeField.getText().isBlank());
    }

}
