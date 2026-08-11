package com.example.passwordmanagerclient.controller.mfa;

import com.example.passwordmanagerclient.controller.DialogManager;
import com.example.passwordmanagerclient.service.MfaService;
import com.example.passwordmanagerclient.util.AppContext;
import com.example.passwordmanagerclient.util.TokenManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class MfaDashboardController {

    @FXML private Label totpClickable;
    @FXML private Label totpLabel;

    @FXML
    public void initialize() {
        updateTotpLabel();
    }

    private void updateTotpLabel() {
        if (AppContext.isTotpEnabled()) {
            totpLabel.setText("Enabled");
            totpLabel.setStyle("-fx-text-fill: green;");
        } else {
            totpLabel.setText("Disabled");
            totpLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void onTotpClick() throws Exception {
        totpClickable.setDisable(true);

        TokenManager.ensureValidJwt();

        if (AppContext.isTotpEnabled()) {
            MfaService.removeTotp();
            AppContext.setTotpEnabled(false);
        } else {
            String totpUrl = MfaService.addTotp();
            try {
                DialogManager.DialogResult<EnableTotpController> result =
                        DialogManager.openWindow(
                                "/com/example/passwordmanagerclient/mfa/enable-totp.fxml",
                                "Enable TOTP",
                                (Stage) totpLabel.getScene().getWindow(),
                                false,
                                EnableTotpController.class
                        );
                result.controller().initialize(totpUrl);
                result.stage().showAndWait();
                AppContext.setTotpEnabled(true);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }

        updateTotpLabel();
        totpClickable.setDisable(false);
    }

}
