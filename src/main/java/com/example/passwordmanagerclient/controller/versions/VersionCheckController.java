package com.example.passwordmanagerclient.controller.versions;

import com.example.passwordmanagerclient.config.AppConfig;
import com.example.passwordmanagerclient.controller.SceneManager;
import com.example.passwordmanagerclient.service.VersionService;
import com.segurapass.model.versions.VersionInfo;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;

public class VersionCheckController {

    @FXML private ProgressIndicator progressIndicator;

    @FXML
    public void initialize() {
        new Thread(() -> {
            try {
                VersionInfo versionInfo = VersionService.getVersionInfo();
                if (versionInfo == null) throw new Exception("Null version info");

                String currentVersionNumber = AppConfig.getCurrentVersionNumber();
                LocalDate currentVersionDate = AppConfig.getCurrentVersionDate();

                boolean upToDate = versionInfo.getVersionNumber().equals(currentVersionNumber)
                        && versionInfo.getVersionDate().equals(currentVersionDate);

                Platform.runLater(() -> {
                    if (upToDate) {
                        SceneManager.switchScene(
                                (Stage) progressIndicator.getScene().getWindow(),
                                "/com/example/passwordmanagerclient/authorization/login-view.fxml"
                        );
                    } else {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/passwordmanagerclient/versions/update-dialog.fxml"));
                        Parent root;
                        try {
                            root = loader.load();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        UpdateDialogController ctrl = loader.getController();
                        ctrl.setData(
                                versionInfo.getVersionNumber(),
                                versionInfo.getVersionDescription(),
                                versionInfo.getDownloadUrl(),
                                versionInfo.getVersionDate().toString()
                        );

                        Stage dialog = new Stage();
                        dialog.initOwner(progressIndicator.getScene().getWindow());
                        dialog.initModality(Modality.APPLICATION_MODAL);
                        dialog.setTitle("Update Required");
                        dialog.setResizable(false);
                        dialog.setScene(new Scene(root));
                        dialog.show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();

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
}
