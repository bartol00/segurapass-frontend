package com.example.passwordmanagerclient;

import com.example.passwordmanagerclient.controller.SceneManager;
import com.example.passwordmanagerclient.service.AuthService;
import com.example.passwordmanagerclient.util.AppContext;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        AppContext.init();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/passwordmanagerclient/versions/version-check-view.fxml"));
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(
                SceneManager.class.getResource("/com/example/passwordmanagerclient/style/app.css").toExternalForm()
        );
        stage.setTitle("Password Manager");
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
