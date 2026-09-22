package com.example.passwordmanagerclient.controller.versions;

import com.example.passwordmanagerclient.controller.DialogManager;
import com.example.passwordmanagerclient.controller.uptime.ServerSelectionDialogController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ServerObsoleteDialogController {

    @FXML private Label serverProtocolVersion;
    @FXML private Label clientProtocolVersion;
    @FXML private Button exitBtn;

    public void setData(int serverProtocolVersion, int clientProtocolVersion) {
        this.serverProtocolVersion.setText(String.format("Server protocol version: %d", serverProtocolVersion));
        this.clientProtocolVersion.setText(String.format("Client protocol version: %d", clientProtocolVersion));
    }

    @FXML
    private void initialize() {
        exitBtn.setOnAction(event -> System.exit(0));
    }

    @FXML
    protected void onServerChangeBtnClick() {
        try {
            DialogManager.DialogResult<ServerSelectionDialogController> result =
                    DialogManager.openWindow(
                            "/com/example/passwordmanagerclient/uptime/server-selection-dialog-view.fxml",
                            "Change Server URL",
                            (Stage) serverProtocolVersion.getScene().getWindow(),
                            false,
                            ServerSelectionDialogController.class
                    );

            result.stage().showAndWait();

            if (result.controller().serverChanged) {
                Stage stage = (Stage) serverProtocolVersion.getScene().getWindow();
                stage.close();
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

}
