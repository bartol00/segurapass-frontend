package com.example.passwordmanagerclient.controller.authorization;

import com.example.passwordmanagerclient.controller.DialogManager;
import com.example.passwordmanagerclient.util.AppContext;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class MfaChoiceController {

    @FXML private Label totpLabel;

    @FXML
    private void onTotpClick() {
        try {
            DialogManager.DialogResult<TotpEntryController> result =
                    DialogManager.openWindow(
                            "/com/example/passwordmanagerclient/authorization/totp-entry.fxml",
                            "MFA Dashboard",
                            (Stage) totpLabel.getScene().getWindow(),
                            false,
                            TotpEntryController.class
                    );
            result.stage().showAndWait();

            if (result.controller().loggedIn) {
                Stage stage = (Stage) totpLabel.getScene().getWindow();
                stage.close();
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    @FXML
    private void onRecoveryCodeClick() {
        try {
            DialogManager.DialogResult<RecoveryCodeEntryController> result =
                    DialogManager.openWindow(
                            "/com/example/passwordmanagerclient/authorization/recovery-code-entry.fxml",
                            "Recovery Code",
                            (Stage) totpLabel.getScene().getWindow(),
                            false,
                            RecoveryCodeEntryController.class
                    );
            result.stage().showAndWait();

            if (result.controller().loggedIn) {
                AppContext.setTotpEnabled(false);
                Stage stage = (Stage) totpLabel.getScene().getWindow();
                stage.close();
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    @FXML
    private void onCancelClick() {
        System.exit(0);
    }

}
