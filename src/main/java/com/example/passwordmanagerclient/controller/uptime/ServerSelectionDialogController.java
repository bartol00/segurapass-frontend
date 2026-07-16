package com.example.passwordmanagerclient.controller.uptime;

import com.example.passwordmanagerclient.controller.credentials.CredentialsController;
import com.example.passwordmanagerclient.service.UptimeService;
import com.example.passwordmanagerclient.util.AppContext;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Setter;

public class ServerSelectionDialogController {

    @FXML private TextField urlField;
    @FXML private Button serverBtn;
    @FXML private Button exitBtn;
    @FXML private Label serverStatus;

    @Setter
    private CredentialsController credentialsController;
    @Setter
    private Runnable onSuccess;
    @Setter
    private boolean exitAppOnClose = false;

    public void initialize() {
        serverBtn.setDisable(true);
        urlField.textProperty().addListener(
                (obs, oldVal, newVal) -> disableServerButton()
        );
    }

    private void disableServerButton() {
        serverBtn.setDisable(urlField.getText().isBlank());
    }

    @FXML
    private void onServerBtnClick() {
        serverBtn.setDisable(true);
        exitBtn.setDisable(true);

        serverStatus.setText("Checking server status...");
        serverStatus.setStyle("-fx-text-fill: blue;");

        String url = urlField.getText();

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            serverStatus.setText("Server URL has to start with 'http://' or 'https://'");
            serverStatus.setStyle("-fx-text-fill: red;");
            urlField.setText("");
            exitBtn.setDisable(false);
            return;
        }

        boolean uptime = UptimeService.getUptime(url);
        if (!uptime) {
            serverStatus.setText("Server uptime could not be verified");
            serverStatus.setStyle("-fx-text-fill: red;");
            urlField.setText("");
            exitBtn.setDisable(false);
            return;
        }

        AppContext.setServerUrl(url);

        Stage stage = (Stage) serverStatus.getScene().getWindow();
        stage.close();

        if (credentialsController != null) {
            credentialsController.handleChildExceptions();
        } else if (onSuccess != null) {
            onSuccess.run();
        }
    }

    @FXML
    private void onExitBtnClick() {
        Stage stage = (Stage) serverStatus.getScene().getWindow();
        stage.close();
        if (exitAppOnClose) {
            System.exit(0);
        }
    }

}
