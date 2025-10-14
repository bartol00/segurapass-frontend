package com.example.passwordmanagerclient.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class SceneManager {

    private SceneManager() {}

    public static void switchScene(Stage stage, String fxmlPath) {
        try {
            double width = stage.getWidth();
            double height = stage.getHeight();
            double x = stage.getX();
            double y = stage.getY();

            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Scene newScene = new Scene(loader.load());

            stage.setScene(newScene);

            stage.setWidth(width);
            stage.setHeight(height);
            stage.setX(x);
            stage.setY(y);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

