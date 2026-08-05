package com.example.passwordmanagerclient.controller.versions;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

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

}
