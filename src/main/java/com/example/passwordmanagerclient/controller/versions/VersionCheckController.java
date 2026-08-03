package com.example.passwordmanagerclient.controller.versions;

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
import xyz.segurapass.sdk.models.ClientLatestVersion;
import xyz.segurapass.sdk.models.VersionModel;

public class VersionCheckController {

    @FXML private ProgressIndicator progressIndicator;

    @FXML
    public void initialize() {

        new Thread(() -> {
            try {
                VersionModel versionModel = VersionService.getVersionInfo();
                if (versionModel == null) {
                    throw new RuntimeException("Null version info");
                }
                int serverProtocolVersion = Integer.parseInt(versionModel.getProtocolVersion());
                int clientProtocolVersion = AppConfig.getProtocolVersion();

                Platform.runLater(() -> {

                    if (clientProtocolVersion < serverProtocolVersion) {

                        StageManager.switchScene(
                                "/com/example/passwordmanagerclient/keys/key-loading.fxml"
                        );

                    } else if (clientProtocolVersion == serverProtocolVersion) {

                        String bucketUrl = AppConfig.getDownloadUrl() + clientProtocolVersion + "/";

                        ClientLatestVersion clientLatestVersion = VersionService.getClientLatestVersion(
                                bucketUrl,
                                "versions.json"
                        );

                        String currentAppVersion = AppConfig.getAppVersion();
                        assert clientLatestVersion != null;
                        String latestAppVersion = clientLatestVersion.getLatestVersion();

                        if (!latestAppVersion.equals(currentAppVersion)) {
                            showOptionalUpdateDialog(bucketUrl, currentAppVersion, latestAppVersion);
                        } else {
                            StageManager.switchScene(
                                    "/com/example/passwordmanagerclient/keys/key-loading.fxml"
                            );
                        }

                    } else {
                        showServerObsoleteDialog(serverProtocolVersion, clientProtocolVersion);
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

    private void showMandatoryUpdateDialog() {

    }

    private void showOptionalUpdateDialog(String bucketUrl, String currentVersion, String latestVersion) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/passwordmanagerclient/versions/optional-update-view.fxml")
            );

            Parent root = loader.load();

            String url = bucketUrl + latestVersion + "/";
            OptionalUpdateDialogController ctrl = loader.getController();
            ctrl.setData(
                    url,
                    currentVersion,
                    latestVersion
            );

            Stage dialog = new Stage();
            dialog.initOwner(progressIndicator.getScene().getWindow());
            dialog.setTitle("Update SeguraPass");
            dialog.setResizable(false);
            dialog.setScene(new Scene(root));
            dialog.show();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void showServerObsoleteDialog(int serverProtocolVersion, int clientProtocolVersion) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/passwordmanagerclient/versions/server-obsolete-view.fxml")
            );

            Parent root = loader.load();

            ServerObsoleteDialogController ctrl = loader.getController();
            ctrl.setData(
                    serverProtocolVersion,
                    clientProtocolVersion
            );

            Stage dialog = new Stage();
            dialog.initOwner(progressIndicator.getScene().getWindow());
            dialog.setTitle("Server Obsolete");
            dialog.setResizable(false);
            dialog.setScene(new Scene(root));
            dialog.show();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
