package com.example.passwordmanagerclient.controller.deletion;

import com.example.passwordmanagerclient.controller.authorization.LoginController;
import com.example.passwordmanagerclient.controller.authorization.RegisterController;
import com.example.passwordmanagerclient.service.DeletionService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Setter;

public class RemoteDeletionController {

    @Setter
    private RegisterController registerController;
    @Setter
    private LoginController loginController;

    @FXML private TextField emailField;
    @FXML private Button sendButton;
    @FXML private Button cancelButton;

    @FXML
    private void onSend() {
        String email = emailField.getText();
        sendButton.setDisable(true);
        cancelButton.setDisable(true);
        DeletionService.deleteEmail(email);
        Stage stage = (Stage) sendButton.getScene().getWindow();
        stage.close();
        if (registerController != null) {
            registerController.writeStatusLabel("Deletion email has been sent to: " + email);
        } else if (loginController != null) {
            loginController.writeStatusLabel("Deletion email has been sent to: " + email);
        }
    }

    @FXML
    private void onCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

}
