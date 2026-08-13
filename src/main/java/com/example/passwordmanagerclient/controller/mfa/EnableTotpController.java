package com.example.passwordmanagerclient.controller.mfa;

import com.example.passwordmanagerclient.controller.DialogManager;
import com.example.passwordmanagerclient.service.MfaService;
import com.example.passwordmanagerclient.util.AppContext;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class EnableTotpController {

    @FXML private ImageView totpQrCode;
    @FXML private Button verifyButton;
    @FXML private Button cancelButton;

    @FXML private OtpInputController otpInputController;

    @FXML
    public void initialize(String totpUrl) {
        int width = 300;
        int height = 300;

        BitMatrix matrix;
        try {
            matrix = new QRCodeWriter().encode(
                    totpUrl,
                    BarcodeFormat.QR_CODE,
                    width,
                    height
            );
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            Stage stage = (Stage) totpQrCode.getScene().getWindow();
            stage.close();
            return;
        }
        assert matrix != null;

        WritableImage image = new WritableImage(width, height);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.getPixelWriter().setColor(
                        x,
                        y,
                        matrix.get(x, y) ? Color.BLACK : Color.WHITE
                );
            }
        }

        totpQrCode.setImage(image);
    }

    @FXML
    private void onVerifyClick() {
        verifyButton.setDisable(true);
        cancelButton.setDisable(true);

        try {
            if (otpInputController.isIncomplete()) {
                verifyButton.setDisable(false);
                cancelButton.setDisable(false);
                return;
            }
            String mfaRecoveryCode = MfaService.verifyTotp(otpInputController.getOtp());
            otpInputController.clear();

            AppContext.setTotpEnabled(true);

            DialogManager.DialogResult<RecoveryCodeController> result =
                    DialogManager.openWindow(
                            "/com/example/passwordmanagerclient/mfa/mfa-recovery-code.fxml",
                            "Enable TOTP",
                            (Stage) totpQrCode.getScene().getWindow(),
                            false,
                            RecoveryCodeController.class
                    );
            result.controller().initialize(mfaRecoveryCode);
            result.stage().showAndWait();
        } catch (Exception e) {
            verifyButton.setDisable(false);
            cancelButton.setDisable(false);
            System.err.println("Error: " + e.getMessage());
            return;
        }

        Stage stage = (Stage) totpQrCode.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onCancelClick() {
        Stage stage = (Stage) totpQrCode.getScene().getWindow();
        stage.close();
    }

}
