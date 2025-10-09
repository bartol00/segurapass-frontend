package com.example.passwordmanagerclient.controller.credentials;

import com.example.passwordmanagerclient.api.credentials.CredentialsResp;
import com.example.passwordmanagerclient.service.CredentialsService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.format.DateTimeFormatter;

public class CredentialsController {

    @FXML private TableView<CredentialsResp> credentialsTable;
    @FXML private TableColumn<CredentialsResp, String> websiteColumn;
    @FXML private TableColumn<CredentialsResp, String> usernameColumn;
    @FXML private TableColumn<CredentialsResp, String> passwordColumn;
    @FXML private TableColumn<CredentialsResp, String> lastUpdatedColumn;

    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Label pageLabel;
    @FXML private ComboBox<Integer> pageSizeCombo;

    private int currentPage = 0;
    private int pageSize = 20;

    @FXML
    public void initialize() {
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

        pageSizeCombo.getItems().addAll(10, 20, 50, 100);
        pageSizeCombo.setValue(pageSize);
        pageSizeCombo.setOnAction(e -> {
            pageSize = pageSizeCombo.getValue();
            currentPage = 0;
            loadCredentialsAsync(currentPage, pageSize);
        });

        loadCredentialsAsync(currentPage, pageSize);
    }

    private void loadCredentialsAsync(int page, int size) {
        setLoadingState(true);

        java.util.concurrent.CompletableFuture.supplyAsync(() -> CredentialsService.getCredentials(page, size)).whenComplete((pagedResponse, ex) -> {
            Platform.runLater(() -> {
                setLoadingState(false);
                if (ex != null) {
                    ex.printStackTrace();
                    credentialsTable.setItems(FXCollections.observableArrayList());
                    pageLabel.setText("Error loading");
                    return;
                }
                credentialsTable.setItems(FXCollections.observableArrayList(pagedResponse.getContent()));
                pageLabel.setText("Page " + (currentPage + 1));
                prevButton.setDisable(currentPage == 0);
                nextButton.setDisable(pagedResponse.getSize() < pageSize);
            });
        });
    }

    private void setLoadingState(boolean loading) {
        prevButton.setDisable(loading || currentPage == 0);
        nextButton.setDisable(loading);
        pageSizeCombo.setDisable(loading);
    }

    @FXML
    private void onPrevPage() {
        if (currentPage == 0) return;
        currentPage--;
        loadCredentialsAsync(currentPage, pageSize);
    }

    @FXML
    private void onNextPage() {
        currentPage++;
        loadCredentialsAsync(currentPage, pageSize);
    }

    @FXML
    private void onAddCredential() {
        // TODO: show add dialog
    }
}
