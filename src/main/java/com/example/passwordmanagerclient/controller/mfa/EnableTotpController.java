package com.example.passwordmanagerclient.controller.mfa;

import com.example.passwordmanagerclient.controller.DialogManager;
import com.example.passwordmanagerclient.service.MfaService;
import com.example.passwordmanagerclient.util.AppContext;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class EnableTotpController {

    @FXML private ImageView totpQrCode;
    @FXML private Label statusLabel;
    @FXML private Button verifyButton;
    @FXML private Button cancelButton;

    @FXML private OtpInputController otpInputController;

    @FXML
    public void initialize(String totpUrl) {
        verifyButton.disableProperty()
                .bind(otpInputController.completeProperty().not());

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
        statusLabel.setText("Verifying TOTP...");
        statusLabel.setStyle("-fx-text-fill: blue;");
        cancelButton.setDisable(true);

        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                return MfaService.verifyTotp(otpInputController.getOtp());
            }
        };

        task.setOnSucceeded(event -> {
            AppContext.setTotpEnabled(true);

            try {
                DialogManager.DialogResult<RecoveryCodeController> result =
                        DialogManager.openWindow(
                                "/com/example/passwordmanagerclient/mfa/mfa-recovery-code.fxml",
                                "Recovery Code",
                                (Stage) totpQrCode.getScene().getWindow(),
                                false,
                                RecoveryCodeController.class
                        );
                result.controller().initialize(task.getValue());
                result.stage().showAndWait();

                Stage stage = (Stage) totpQrCode.getScene().getWindow();
                stage.close();
            } catch (Exception e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }
        });

        task.setOnFailed(event -> {
            statusLabel.setText(task.getException().getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
            cancelButton.setDisable(false);
            otpInputController.clear();
        });

        new Thread(task).start();
    }

    @FXML
    private void onCancelClick() {
        Stage stage = (Stage) totpQrCode.getScene().getWindow();
        stage.close();
    }

}
