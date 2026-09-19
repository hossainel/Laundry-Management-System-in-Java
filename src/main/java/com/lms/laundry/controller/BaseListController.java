package com.lms.laundry.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import com.lms.laundry.manager.IconManager;
import org.controlsfx.control.Notifications;
//import org.kordamp.ikonli.javafx.FontIcon;
import java.util.List;
import java.util.Optional;

public abstract class BaseListController<T> {
    protected final ObservableList<T> masterData = FXCollections.observableArrayList();
    @FXML
    protected TextField txtSearch;
    @FXML
    protected TableView<T> tblData;
    @FXML
    protected TableColumn<T, Void> colAction;
    @FXML
    protected Label lblStatus;
    protected FilteredList<T> filteredData;

    protected void setupTableAndFiltering() {
        filteredData = new FilteredList<>(masterData, p -> true);
        if (txtSearch != null) {
            txtSearch.textProperty().addListener((obs, old, newValue) -> {
                filteredData.setPredicate(entity -> matchesSearch(entity, newValue));
                updateStatusLabel();
            });
        }
        SortedList<T> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tblData.comparatorProperty());
        tblData.setItems(sortedData);
        if (colAction != null) {
            colAction.getStyleClass().add("col-center");
            addActionButtons();
        }
        refreshTable();
    }

    public void refreshTable() {
        if (lblStatus != null) lblStatus.getStyleClass().remove("error-label");
        masterData.setAll(loadEntities());
        updateStatusLabel();
    }

    private void updateStatusLabel() {
        if (lblStatus != null) {
            lblStatus.setText("Showing " + filteredData.size() + " " + getEntityContextName());
        }
    }

    protected void showTemporaryError(String message) {
        if (lblStatus != null) {
            lblStatus.setText(message);
            if (!lblStatus.getStyleClass().contains("error-label")) {
                lblStatus.getStyleClass().add("error-label");
            }
        }
    }

    private void addActionButtons() {
        Callback<TableColumn<T, Void>, TableCell<T, Void>> cellFactory = param -> new TableCell<>() {
            private final Button btnView = new Button();
            private final Button btnDelete = new Button();
            private final HBox pane = new HBox(8, btnView, btnDelete);

            {
                pane.setAlignment(Pos.CENTER);
                // Configured FontAwesome identifiers for action table cells
                //btnView.setGraphic(new FontIcon("fa-edit"));
                btnView.setText("Edit");
                //btnDelete.setGraphic(new FontIcon("fa-trash"));
                btnDelete.setText("Delete");
                IconManager.decorateButton(btnView);
                IconManager.decorateButton(btnDelete);
                btnView.getStyleClass().addAll("button", "button-success");
                btnDelete.getStyleClass().addAll("button", "button-danger");
                btnView.setStyle("-fx-padding: 5 10;");
                btnDelete.setStyle("-fx-padding: 5 10;");
                btnView.setOnAction(event -> handleView(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(event -> confirmAndDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        };
        colAction.setCellFactory(cellFactory);
    }

    private void confirmAndDelete(T entity) {
        if (!canDeleteEntity(entity)) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText("Permanently delete this item?");
        alert.setContentText("This modification cannot be recovered from database history.");
        //alert.setGraphic(new FontIcon("fa-exclamation-triangle"));
        ButtonType deleteButton = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(deleteButton, cancelButton);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == deleteButton) {
            executeDeletion(entity);
            Notifications.create().title("Deleted").text("The entry was successfully dropped.")
                    //.graphic(new FontIcon("fa-trash"))
                    .position(Pos.TOP_RIGHT).showWarning();
        }
    }

    protected abstract boolean matchesSearch(T entity, String searchTerm);

    protected abstract List<T> loadEntities();

    protected abstract String getEntityContextName();

    protected abstract void handleView(T entity);

    protected abstract boolean canDeleteEntity(T entity);

    protected abstract void executeDeletion(T entity);
}
