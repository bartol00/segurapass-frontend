package com.example.passwordmanagerclient.controller.versions;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class UpdateDialogController {

    @FXML private Label versionLabel;
    @FXML private Label descLabel;
    @FXML private Label releaseLabel;
    @FXML private Button downloadBtn;
    @FXML private Button exitBtn;

    private String downloadUrl;

    public void setData(String version, String desc, String downloadUrl, String releaseDate) {
        this.downloadUrl = downloadUrl;
        versionLabel.setText("Latest version: v" + version);
        descLabel.setText(desc);
        releaseLabel.setText("Latest version release date: " + releaseDate);
    }

    @FXML
    private void initialize() {
        downloadBtn.setOnAction(e -> {
            try {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(downloadUrl));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            System.exit(0);
        });

        exitBtn.setOnAction(e -> System.exit(0));
    }
}
