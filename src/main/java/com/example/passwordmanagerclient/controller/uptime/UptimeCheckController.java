package com.example.passwordmanagerclient.controller.uptime;

import com.example.passwordmanagerclient.controller.StageManager;
import com.example.passwordmanagerclient.service.UptimeService;
import com.example.passwordmanagerclient.util.AppContext;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Stage;

public class UptimeCheckController {

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    public void initialize() {
        checkServer();
    }

    private void checkServer() {

        new Thread(() -> {

            try {

                String url = AppContext.getServerUrl();

                if (url == null || !UptimeService.getUptime(url)) {
                    Platform.runLater(this::showServerSelectionDialog);
                } else {
                    Platform.runLater(this::goToVersionCheck);
                }

            } catch (Exception e) {
                Platform.runLater(this::showError);
            }

        }).start();
    }

    private void showServerSelectionDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/passwordmanagerclient/uptime/server-selection-dialog-view.fxml")
            );

            Parent root = loader.load();

            ServerSelectionDialogController controller = loader.getController();

            controller.setOnSuccess(this::goToVersionCheck);

            Stage dialog = new Stage();

            dialog.initOwner(progressIndicator.getScene().getWindow());
            dialog.setTitle("SeguraPass Server URL Required");
            dialog.setResizable(false);
            dialog.setScene(new Scene(root));

            dialog.show();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void goToVersionCheck() {
        StageManager.switchScene(
                "/com/example/passwordmanagerclient/versions/version-check-view.fxml"
        );
    }

    private void showError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Network Error");
        alert.setHeaderText("Could not check server up status");
        alert.setContentText("Closing application...");
        alert.showAndWait();

        System.exit(0);
    }
}
