package com.example.passwordmanagerclient.controller.credentials;

import com.example.passwordmanagerclient.controller.password_change.PasswordChangeController;
import com.segurapass.models.credentials.DecryptedCredential;
import com.example.passwordmanagerclient.controller.StageManager;
import com.example.passwordmanagerclient.controller.deletion.AuthorizedDeletionController;
import com.example.passwordmanagerclient.service.AuthService;
import com.example.passwordmanagerclient.service.CredentialsService;
import com.example.passwordmanagerclient.util.AppContext;
import com.example.passwordmanagerclient.util.TokenManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

public class CredentialsController {

    @FXML private TableView<DecryptedCredential> credentialsTable;
    @FXML private TableColumn<DecryptedCredential, String> websiteColumn;
    @FXML private TableColumn<DecryptedCredential, String> usernameColumn;
    @FXML private TableColumn<DecryptedCredential, String> passwordColumn;
    @FXML private TableColumn<DecryptedCredential, String> lastUpdatedColumn;
    @FXML private TableColumn<DecryptedCredential, Void> actionsColumn;

    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Button logoutButton;
    @FXML private Button changePasswordButton;
    @FXML private Button deleteAccountButton;
    @FXML private Label pageLabel;
    @FXML private ComboBox<Integer> pageSizeCombo;

    private int currentPage = 0;
    private int pageSize = 20;

    @FXML
    public void initialize() {
        setFactories();

        pageSizeCombo.getItems().addAll(5, 10, 20, 50, 100);
        pageSizeCombo.setValue(pageSize);
        pageSizeCombo.setOnAction(e -> {
            pageSize = pageSizeCombo.getValue();
            currentPage = 0;
            setFactories();
            refreshTable();
        });

        preloadCredentials();
        refreshTable();
    }

    public void refreshTable() {
        List<DecryptedCredential> cache = AppContext.getCredentialsCache();
        if (cache == null || cache.isEmpty()) {
            credentialsTable.setItems(FXCollections.observableArrayList());
            pageLabel.setText("No credentials found");
            prevButton.setDisable(true);
            nextButton.setDisable(true);
            return;
        }

        cache.sort(Comparator.comparing(DecryptedCredential::getWebsite, String.CASE_INSENSITIVE_ORDER));

        int fromIndex = currentPage * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, cache.size());

        var subList = cache.subList(fromIndex, toIndex);
        credentialsTable.setItems(FXCollections.observableArrayList(subList));

        pageLabel.setText(String.format("Page %d (%d–%d of %d)",
                currentPage + 1,
                fromIndex + 1,
                toIndex,
                cache.size()
        ));

        prevButton.setDisable(currentPage == 0);
        nextButton.setDisable(toIndex >= cache.size());
    }

    private void preloadCredentials() {
        List<DecryptedCredential> allCredentials = CredentialsService.getCredentials(0, 100);
        AppContext.setCredentialsCache(allCredentials);
    }

    private void setFactories() {
        websiteColumn.setCellValueFactory(new PropertyValueFactory<>("website"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        passwordColumn.setCellValueFactory(new PropertyValueFactory<>("password"));

        usernameColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String username, boolean empty) {
                super.updateItem(username, empty);

                if (empty || username == null) {
                    setGraphic(null);
                    return;
                }

                Label usernameLabel = new Label();

                Button copyButton = new Button("📋");
                copyButton.setPrefSize(20, 20);
                copyButton.setStyle("-fx-font-size: 10px; -fx-padding: 0;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                HBox container = new HBox(usernameLabel, spacer, copyButton);
                container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                container.setSpacing(5);

                copyButton.setOnAction(e -> {
                    Clipboard clipboard = Clipboard.getSystemClipboard();
                    ClipboardContent content = new ClipboardContent();
                    content.putString(username);
                    clipboard.setContent(content);
                });

                usernameLabel.setText(username);

                setGraphic(container);
            }
        });

        passwordColumn.setCellFactory(col -> new TableCell<>() {
            private boolean visible = false;

            @Override
            protected void updateItem(String password, boolean empty) {
                super.updateItem(password, empty);

                if (empty || password == null) {
                    setGraphic(null);
                    return;
                }

                DecryptedCredential item = getTableView().getItems().get(getIndex());

                Label passwordLabel = new Label();

                Button copyButton = new Button("📋");
                copyButton.setPrefSize(20, 20);
                copyButton.setStyle("-fx-font-size: 10px; -fx-padding: 0;");

                Button toggleButton = new Button("\uD83D\uDC41");
                toggleButton.setPrefSize(20, 20);
                toggleButton.setStyle("-fx-font-size: 10px; -fx-padding: 0;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                HBox container = new HBox(passwordLabel, spacer, copyButton, toggleButton);
                container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                container.setSpacing(5);

                copyButton.setOnAction(e -> {
                    Clipboard clipboard = Clipboard.getSystemClipboard();
                    ClipboardContent content = new ClipboardContent();
                    content.putString(password);
                    clipboard.setContent(content);
                });

                passwordLabel.setText(item.isPasswordVisible() ? password : "••••••••");

                toggleButton.setOnAction(e -> {
                    item.setPasswordVisible(!item.isPasswordVisible());
                    passwordLabel.setText(item.isPasswordVisible() ? password : "••••••••");
                });

                setGraphic(container);
            }
        });
        passwordColumn.setMinWidth(350);

        lastUpdatedColumn.setCellValueFactory(cell -> {
            var inst = cell.getValue().getLastUpdated();
            String text = inst == null ? "" :
                    inst.atZone(java.time.ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            return new javafx.beans.property.SimpleStringProperty(text);
        });

        actionsColumn.setMinWidth(300);
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = new Button("Update");
            private final Button deleteButton = new Button("Delete");
            private final HBox container = new HBox(5, editButton, deleteButton);

            {
                editButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                editButton.setMinWidth(60);
                deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                deleteButton.setMinWidth(60);
                container.setAlignment(javafx.geometry.Pos.CENTER);

                editButton.setOnAction(event -> {
                    var credential = getTableView().getItems().get(getIndex());
                    onUpdateCredential(
                            credential.getCredentialsId().toString(),
                            credential.getWebsite(),
                            credential.getUsername()
                    );
                });

                deleteButton.setOnAction(event -> {
                    var credential = getTableView().getItems().get(getIndex());
                    onDeleteCredential(credential.getCredentialsId().toString());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        });

        websiteColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String website, boolean empty) {
                super.updateItem(website, empty);
                if (empty || website == null) {
                    setGraphic(null);
                    return;
                }

                Label websiteLabel = new Label(website);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                HBox container = new HBox(websiteLabel, spacer);
                container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                container.setSpacing(5);

                setGraphic(container);
            }
        });

        lastUpdatedColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String dateText, boolean empty) {
                super.updateItem(dateText, empty);
                if (empty || dateText == null) {
                    setGraphic(null);
                    return;
                }

                Label dateLabel = new Label(dateText);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                HBox container = new HBox(dateLabel, spacer);
                container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                container.setSpacing(5);

                setGraphic(container);
            }
        });

    }

    @FXML
    private void onPrevPage() {
        currentPage--;
        setFactories();
        refreshTable();
    }

    @FXML
    private void onNextPage() {
        currentPage++;
        setFactories();
        refreshTable();
    }

    @FXML
    private void onAddCredential() {
        try {
            TokenManager.ensureValidJwt();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/passwordmanagerclient/credentials/add-credentials-view.fxml"));

            Stage dialogStage = new Stage();
            Scene dialogScene = new Scene(loader.load());
            dialogScene.getStylesheets().add(getClass()
                    .getResource("/com/example/passwordmanagerclient/style/app.css")
                    .toExternalForm()
            );
            dialogStage.setScene(dialogScene);

            dialogStage.setTitle("Add New Credentials");
            dialogStage.setResizable(false);
            dialogStage.initModality(javafx.stage.Modality.WINDOW_MODAL);

            Stage parentStage = (Stage) credentialsTable.getScene().getWindow();
            dialogStage.initOwner(parentStage);

            AddCredentialController controller = loader.getController();
            controller.setParentController(this);

            dialogStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            onLogout();
        }
    }

    @FXML
    private void onUpdateCredential(String credentialId, String website, String username) {
        try {
            TokenManager.ensureValidJwt();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/passwordmanagerclient/credentials/credential-edit-view.fxml"));

            Stage dialogStage = new Stage();
            Scene dialogScene = new Scene(loader.load());
            dialogScene.getStylesheets().add(getClass()
                    .getResource("/com/example/passwordmanagerclient/style/app.css")
                    .toExternalForm()
            );
            dialogStage.setScene(dialogScene);

            dialogStage.setTitle("Update Existing Credentials");
            dialogStage.setResizable(false);
            dialogStage.initModality(javafx.stage.Modality.WINDOW_MODAL);

            Stage parentStage = (Stage) credentialsTable.getScene().getWindow();
            dialogStage.initOwner(parentStage);

            CredentialEditController controller = loader.getController();
            controller.loadCredentialData(credentialId, website, username);
            controller.setParentController(this);

            dialogStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            onLogout();
        }
    }

    @FXML
    private void onDeleteCredential(String credentialId) {
        try {
            TokenManager.ensureValidJwt();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/passwordmanagerclient/credentials/delete-confirmation-view.fxml"));

            Stage dialogStage = new Stage();
            Scene dialogScene = new Scene(loader.load());
            dialogScene.getStylesheets().add(getClass()
                    .getResource("/com/example/passwordmanagerclient/style/app.css")
                    .toExternalForm()
            );
            dialogStage.setScene(dialogScene);

            dialogStage.setTitle("Confirm Deletion");
            dialogStage.setResizable(false);
            dialogStage.initModality(javafx.stage.Modality.WINDOW_MODAL);

            Stage parentStage = (Stage) credentialsTable.getScene().getWindow();
            dialogStage.initOwner(parentStage);

            DeleteConfirmationController controller = loader.getController();
            controller.loadCredential(credentialId);
            controller.setParentController(this);

            dialogStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            onLogout();
        }
    }

    @FXML
    private void onDeleteAccount() {
        try {
            TokenManager.ensureValidJwt();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/passwordmanagerclient/deletion/authorized-deletion-view.fxml"));

            Stage dialogStage = new Stage();
            Scene dialogScene = new Scene(loader.load());
            dialogScene.getStylesheets().add(getClass()
                    .getResource("/com/example/passwordmanagerclient/style/app.css")
                    .toExternalForm()
            );
            dialogStage.setScene(dialogScene);

            dialogStage.setTitle("Delete Account");
            dialogStage.setResizable(false);
            dialogStage.initModality(javafx.stage.Modality.WINDOW_MODAL);

            Stage parentStage = (Stage) credentialsTable.getScene().getWindow();
            dialogStage.initOwner(parentStage);

            AuthorizedDeletionController controller = loader.getController();
            controller.setParentController(this);

            dialogStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            onLogout();
        }
    }

    @FXML
    private void onChangePassword() {
        try {
            TokenManager.ensureValidJwt();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/passwordmanagerclient/password_change/password-change-view.fxml"));

            Stage dialogStage = new Stage();
            Scene dialogScene = new Scene(loader.load());
            dialogScene.getStylesheets().add(getClass()
                    .getResource("/com/example/passwordmanagerclient/style/app.css")
                    .toExternalForm()
            );
            dialogStage.setScene(dialogScene);

            dialogStage.setTitle("Change Password");
            dialogStage.setResizable(false);
            dialogStage.initModality(javafx.stage.Modality.WINDOW_MODAL);

            Stage parentStage = (Stage) credentialsTable.getScene().getWindow();
            dialogStage.initOwner(parentStage);

            PasswordChangeController controller = loader.getController();
            controller.setParentController(this);

            dialogStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            onLogout();
        }
    }

    public void handleChildExceptions() {
        onLogout();
    }

    @FXML
    private void onLogout() {
        try {
            logoutButton.setDisable(true);
            AuthService.logout();
            AppContext.clearSensitiveData();
            StageManager.switchScene("/com/example/passwordmanagerclient/authorization/login-view.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
