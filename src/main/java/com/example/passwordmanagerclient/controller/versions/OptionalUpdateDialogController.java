package com.example.passwordmanagerclient.controller.versions;

import com.example.passwordmanagerclient.controller.StageManager;
import com.example.passwordmanagerclient.util.DownloadUtil;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

public class OptionalUpdateDialogController {

    @FXML private Label currentClientVersionLabel;
    @FXML private Label latestClientVersionLabel;
    @FXML private Button updateButton;
    @FXML private Button declineButton;
    @FXML private ProgressBar downloadProgressBar;
    @FXML private Label progressLabel;

    private String latestVersionUrl;

    public void setData(String url, String currentClientVersion, String latestClientVersion) {
        this.latestVersionUrl = url;
        this.currentClientVersionLabel.setText(String.format("Current app version: %s", currentClientVersion));
        this.latestClientVersionLabel.setText(String.format("Latest app version: %s", latestClientVersion));
    }

    @FXML
    private void initialize() {
        updateButton.setOnAction(event -> {
//            updateButton.setDisable(true);
//            declineButton.setDisable(true);

//            downloadProgressBar.setVisible(true);
//            progressLabel.setVisible(true);

//            Task<Void> task = DownloadUtil.downloadAndInstall(latestVersionUrl);

//            downloadProgressBar.progressProperty().bind(task.progressProperty());
//            progressLabel.textProperty().bind(task.messageProperty());

//            Thread thread = new Thread(task);
//            thread.setDaemon(true);
//            thread.start();
            DownloadUtil.openUrl(latestVersionUrl);
            System.exit(0);
        });
        declineButton.setOnAction(event -> {
            Stage stage = (Stage) currentClientVersionLabel.getScene().getWindow();
            stage.close();
            StageManager.switchScene(
                    "/com/example/passwordmanagerclient/keys/key-loading.fxml"
            );
        });
    }

}
