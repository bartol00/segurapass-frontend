package com.example.passwordmanagerclient;

import com.example.passwordmanagerclient.controller.StageManager;
import com.example.passwordmanagerclient.service.AuthService;
import com.example.passwordmanagerclient.util.AppContext;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        AppContext.init();

        StageManager.init(stage);

        FXMLLoader loader = new FXMLLoader(getClass()
                .getResource("/com/example/passwordmanagerclient/uptime/uptime-check-view.fxml"));
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(
                Objects.requireNonNull(
                        StageManager.class.getResource("/com/example/passwordmanagerclient/style/app.css")
                        )
                        .toExternalForm()
        );

        stage.setTitle("SeguraPass");
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/logo16.png"))));
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/logo32.png"))));
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/logo64.png"))));
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/logo128.png"))));
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/logo256.png"))));

        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        AuthService.logout();
        AppContext.clearSensitiveData();
        super.stop();
    }

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                AppContext.clearSensitiveData();
            } catch (Throwable t) {
                // n/a
            }
        }));

        launch(args);
    }
}
