package com.example.passwordmanagerclient.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Objects;

public final class DialogManager {

    private DialogManager() {}

    public static <T> DialogResult<T> openWindow(
            String fxmlPath,
            String title,
            Stage owner,
            boolean resizable,
            Class<T> controllerClass
    ) throws Exception {

        FXMLLoader loader = new FXMLLoader(DialogManager.class.getResource(fxmlPath));
        Scene scene = new Scene(loader.load());

        scene.getStylesheets().add(
                Objects.requireNonNull(DialogManager.class.getResource(
                        "/com/example/passwordmanagerclient/style/app.css"
                )).toExternalForm()
        );

        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle(title);
        stage.setResizable(resizable);

        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(owner);

        Object controller = loader.getController();

        if (!controllerClass.isInstance(controller)) {
            throw new IllegalStateException(
                    "Controller is not of expected type: " + controllerClass.getName()
            );
        }

        return new DialogResult<>(stage, controllerClass.cast(controller));
    }

    public record DialogResult<T>(Stage stage, T controller) {}
}