package com.example.passwordmanagerclient.util;

import com.example.passwordmanagerclient.service.VersionService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import xyz.segurapass.sdk.models.ClientVersion;

import java.awt.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HexFormat;

public class DownloadUtil {

    public static void verifyVersionsSignature(String latestVersionUrl) throws Exception {
        byte[] versionsBytes = VersionService.getBytes(latestVersionUrl + "versions.json");
        byte[] signatureBytes = VersionService.getBytes(latestVersionUrl + "versions.sig");
        boolean isValidSignature = verifySignature(versionsBytes, signatureBytes);
        if (!isValidSignature) {
            throw new Exception("Signature versions verification failed");
        }
    }

    public static Task<Void> downloadAndInstall(String latestVersionUrl) {

        return new Task<>() {

            @Override
            protected Void call() throws Exception {

                // download metadata.json
                updateMessage("Downloading metadata...");
                ClientVersion clientVersion = VersionService.getClientVersion(latestVersionUrl, "metadata.json");
                if (clientVersion == null || clientVersion.getSha256() == null) {
                    throw new Exception("Could not get app metadata");
                }
                byte[] clientVersionBytes = VersionService.getBytes(latestVersionUrl + "metadata.json");

                // download metadata.sig
                byte[] signatureBytes = VersionService.getBytes(latestVersionUrl + "metadata.sig");

                // verify signature
                updateMessage("Verifying signature...");
                boolean isValidSignature = verifySignature(clientVersionBytes, signatureBytes);
                if (!isValidSignature) {
                    throw new Exception("Signature metadata verification failed");
                }

                // download installer
                updateMessage("Downloading installer...");
                Path installerPath = Path.of(
                        System.getProperty("java.io.tmpdir"),
                        "SeguraPass-Setup.exe"
                );
                Files.deleteIfExists(installerPath);
                downloadInstaller(
                        latestVersionUrl + "SeguraPass-Setup.exe",
                        installerPath
                );

                // verify sha256
                updateMessage("Verifying installer...");
                String installerSha256 = sha256(installerPath);
                if (!installerSha256.equals(clientVersion.getSha256())) {
                    throw new Exception("SHA256 verification failed");
                }

                // launch installer
                updateMessage("Launching installer...");
                new ProcessBuilder(
                        installerPath.toString()
                ).start();

                return null;
            }

            @Override
            protected void succeeded() {
                Platform.exit();
                System.exit(0);
            }

            @Override
            protected void failed() {
                getException().printStackTrace();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText("Update Failed");
                    alert.setContentText(getException().getMessage());
                    alert.showAndWait();
                });
            }

            private void downloadInstaller(
                    String url,
                    Path destination
            ) throws Exception {

                HttpClient client = HttpClient.newHttpClient();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<InputStream> response =
                        client.send(request, HttpResponse.BodyHandlers.ofInputStream());

                long totalBytes = response.headers()
                        .firstValueAsLong("Content-Length")
                        .orElse(-1);

                try (InputStream in = response.body();
                     OutputStream out = Files.newOutputStream(destination)) {

                    byte[] buffer = new byte[8192];

                    long downloaded = 0;

                    int read;

                    while ((read = in.read(buffer)) != -1) {

                        out.write(buffer, 0, read);

                        downloaded += read;

                        if (totalBytes > 0) {

                            updateProgress(downloaded, totalBytes);

                            updateMessage(
                                    String.format(
                                            "Downloading installer... %.1f%%",
                                            downloaded * 100.0 / totalBytes
                                    )
                            );
                        }
                    }
                }
            }
        };
    }

    public static boolean verifySignature(
            byte[] payload,
            byte[] signatureBytes
    ) throws Exception {

        PublicKey publicKey = loadPublicKey();

        Signature signature = Signature.getInstance("Ed25519");
        signature.initVerify(publicKey);
        signature.update(payload);

        return signature.verify(signatureBytes);
    }

    private static PublicKey loadPublicKey() throws Exception {
        String path = "/update-signing-public.pem";

        try (InputStream is = DownloadUtil.class.getResourceAsStream(path)) {

            if (is == null) {
                throw new IllegalStateException("Public key not found: " + path);
            }

            String pem = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            pem = pem
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] bytes = Base64.getDecoder().decode(pem);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
            return KeyFactory.getInstance("Ed25519").generatePublic(spec);
        }
    }

    public static String sha256(Path file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        try (InputStream in = Files.newInputStream(file)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }

        return HexFormat.of().formatHex(digest.digest());
    }

}
