package com.example.passwordmanagerclient;

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

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/passwordmanagerclient/authorization/login-view.fxml"));
        Scene scene = new Scene(loader.load(), 400, 300);
        stage.setTitle("Password Manager");
        stage.setScene(scene);
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
