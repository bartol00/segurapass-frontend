package com.example.passwordmanagerclient.controller.key;

import com.example.passwordmanagerclient.controller.StageManager;
import com.example.passwordmanagerclient.service.KeyService;
import com.example.passwordmanagerclient.util.AppContext;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressIndicator;

import java.security.PublicKey;

public class KeyController {

    @FXML private ProgressIndicator progressIndicator;

    @FXML
    public void initialize() {
        progressIndicator.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((obsW, oldW, newW) -> {});
            }
        });

        new Thread(() -> {
            try {
                PublicKey publicKey = KeyService.getPublicKey();
                if (publicKey == null) throw new Exception("Public key fetch failed");

                AppContext.setPublicKey(publicKey);

                Platform.runLater(
                        () -> StageManager.switchScene(
                                "/com/example/passwordmanagerclient/authorization/login-view.fxml"
                        )
                );

            } catch (Exception e) {
                System.err.println(e.getMessage());

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Network Error");
                    alert.setHeaderText("Could not fetch public key");
                    alert.setContentText("Closing application...");
                    alert.showAndWait();
                    System.exit(0);
                });
            }
        }).start();
    }

}
