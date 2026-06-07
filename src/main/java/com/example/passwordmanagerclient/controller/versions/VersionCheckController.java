package com.example.passwordmanagerclient.controller.versions;

import xyz.segurapass.api.versions.VersionInfo;
import com.example.passwordmanagerclient.config.AppConfig;
import com.example.passwordmanagerclient.controller.StageManager;
import com.example.passwordmanagerclient.service.VersionService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Stage;

public class VersionCheckController {

    @FXML private ProgressIndicator progressIndicator;

    @FXML
    public void initialize() {

        new Thread(() -> {
            try {
                VersionInfo versionInfo = VersionService.getVersionInfo();

                if (versionInfo == null) {
                    throw new RuntimeException("Null version info");
                }

                boolean upToDate =
                        versionInfo.getVersionNumber().equals(AppConfig.getCurrentVersionNumber())
                                && versionInfo.getVersionDate().equals(AppConfig.getCurrentVersionDate());

                Platform.runLater(() -> {

                    if (upToDate) {
                        StageManager.switchScene(
                                "/com/example/passwordmanagerclient/keys/key-loading.fxml"
                        );
                    } else {
                        showUpdateDialog(versionInfo);
                    }
                });

            } catch (Exception e) {
                System.err.println(e.getMessage());

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Network Error");
                    alert.setHeaderText("Could not check for updates");
                    alert.setContentText("Closing application...");
                    alert.showAndWait();
                    System.exit(0);
                });
            }
        }).start();
    }

    private void showUpdateDialog(VersionInfo versionInfo) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/passwordmanagerclient/versions/update-dialog.fxml")
            );

            Parent root = loader.load();

            UpdateDialogController ctrl = loader.getController();
            ctrl.setData(
                    versionInfo.getVersionNumber(),
                    versionInfo.getVersionDescription(),
                    versionInfo.getDownloadUrl(),
                    versionInfo.getVersionDate().toString()
            );

            Stage dialog = new Stage();
            dialog.initOwner(progressIndicator.getScene().getWindow());
            dialog.setTitle("Update Required");
            dialog.setResizable(false);
            dialog.setScene(new Scene(root));
            dialog.show();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
