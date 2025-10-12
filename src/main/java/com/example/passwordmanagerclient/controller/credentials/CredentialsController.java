package com.example.passwordmanagerclient.controller.credentials;

import com.example.passwordmanagerclient.api.credentials.CredentialsResp;
import com.example.passwordmanagerclient.api.credentials.PagedResponse;
import com.example.passwordmanagerclient.service.AuthService;
import com.example.passwordmanagerclient.service.CredentialsService;
import com.example.passwordmanagerclient.util.AppContext;
import com.example.passwordmanagerclient.util.TokenManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CredentialsController {

    @FXML private TableView<CredentialsResp> credentialsTable;
    @FXML private TableColumn<CredentialsResp, String> websiteColumn;
    @FXML private TableColumn<CredentialsResp, String> usernameColumn;
    @FXML private TableColumn<CredentialsResp, String> passwordColumn;
    @FXML private TableColumn<CredentialsResp, String> lastUpdatedColumn;
    @FXML private TableColumn<CredentialsResp, Void> actionsColumn;

    @FXML private Button prevButton;
    @FXML private Button nextButton;
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
        List<CredentialsResp> cache = AppContext.getCredentialsCache();
        if (cache == null || cache.isEmpty()) {
            credentialsTable.setItems(FXCollections.observableArrayList());
            pageLabel.setText("No credentials found");
            prevButton.setDisable(true);
            nextButton.setDisable(true);
            return;
        }

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
        List<CredentialsResp> allCredentials = new ArrayList<>();
        int page = 0;
        int size = 3;

        while (true) {
            PagedResponse<CredentialsResp> response = CredentialsService.getCredentials(page, size);
            allCredentials.addAll(response.getContent());
            if (response.getContent().size() < size) break;
            page++;
        }

        AppContext.setCredentialsCache(allCredentials);
    }

    private void setFactories() {
        websiteColumn.setCellValueFactory(new PropertyValueFactory<>("website"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        passwordColumn.setCellValueFactory(new PropertyValueFactory<>("password"));

        lastUpdatedColumn.setCellValueFactory(cell -> {
            var inst = cell.getValue().getLastUpdated();
            String text = inst == null ? "" :
                    inst.atZone(java.time.ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            return new javafx.beans.property.SimpleStringProperty(text);
        });

        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = new Button("Update");
            private final Button deleteButton = new Button("Delete");
            private final HBox container = new HBox(5, editButton, deleteButton);

            {
                editButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
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
            dialogStage.setScene(new Scene(loader.load()));
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
            dialogStage.setScene(new Scene(loader.load()));
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
            dialogStage.setScene(new Scene(loader.load()));
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

    public void handleChildExceptions() {
        onLogout();
    }

    @FXML
    private void onLogout() {
        try {
            AuthService.logout();
            AppContext.clearSensitiveData();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/passwordmanagerclient/authorization/login-view.fxml"));
            Stage stage = (Stage) credentialsTable.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setMaximized(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
