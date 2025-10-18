package com.example.passwordmanagerclient.controller;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;

public final class SceneManager {

    private SceneManager() {}

    /**
     * Switch scene with a fade transition.
     * Keeps previous window size and position.
     */
    public static void switchScene(Stage stage, String fxmlPath) {
        try {
            // Save current stage dimensions and position
            double width = stage.getWidth();
            double height = stage.getHeight();
            double x = stage.getX();
            double y = stage.getY();

            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Pane root = loader.load();
            Scene newScene = new Scene(root);
            newScene.getStylesheets().add(
                    SceneManager.class.getResource("/com/example/passwordmanagerclient/style/app.css").toExternalForm()
            );

            if (stage.getScene() != null) {
                // If a scene already exists, fade out current root first
                Pane oldRoot = (Pane) stage.getScene().getRoot();
                fadeOut(oldRoot, () -> {
                    // Once fade out is complete, set the new scene
                    stage.setScene(newScene);
                    stage.setWidth(width);
                    stage.setHeight(height);
                    stage.setX(x);
                    stage.setY(y);
                    fadeIn(root);
                });
            } else {
                // First scene, just show with fade in
                stage.setScene(newScene);
                stage.setWidth(width);
                stage.setHeight(height);
                stage.setX(x);
                stage.setY(y);
                fadeIn(root);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Fade out a pane and run a callback after completion
     */
    private static void fadeOut(Pane pane, Runnable onFinished) {
        FadeTransition fade = new FadeTransition(Duration.millis(200), pane);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(e -> onFinished.run());
        fade.play();
    }

    /**
     * Fade in a pane
     */
    private static void fadeIn(Pane pane) {
        FadeTransition fade = new FadeTransition(Duration.millis(200), pane);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }
}
