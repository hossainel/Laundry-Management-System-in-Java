package com.lms.laundry.controller;

import com.lms.laundry.manager.PaymentMethodManager;
import com.lms.laundry.model.PaymentMethod;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.util.List;

public class PaymentMethodListController extends BaseListController<PaymentMethod> {
    @FXML
    private TableView<PaymentMethod> tblPaymentMethods;
    @FXML
    private TableColumn<PaymentMethod, Integer> colId;
    @FXML
    private TableColumn<PaymentMethod, String> colName;
    @FXML
    private TableColumn<PaymentMethod, Boolean> colStatus;

    @FXML
    public void initialize() {
        this.tblData = tblPaymentMethods;
        // Map column
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colId.getStyleClass().add("col-center");
        colStatus.getStyleClass().add("col-center");
        addStatusToggleButtons();
        setupTableAndFiltering();
    }

    @Override
    protected boolean matchesSearch(PaymentMethod paymentMethod, String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) return true;
        String filter = searchTerm.toLowerCase().trim();
        return paymentMethod.getName().toLowerCase().contains(filter);
    }

    @Override
    protected List<PaymentMethod> loadEntities() {
        return PaymentMethodManager.getAll();
    }

    @Override
    protected String getEntityContextName() {
        return "Product Categories";
    }

    @Override
    protected void handleView(PaymentMethod paymentMethod) {
        PaymentMethodController controller = MainController.navigate("payment-method-view.fxml", "Edit PaymentMethod");
        if (controller != null) {
            controller.setEntity(paymentMethod);
        }
    }

    @Override
    protected boolean canDeleteEntity(PaymentMethod paymentMethod) {
        return true;
    }

    @Override
    protected void executeDeletion(PaymentMethod paymentMethod) {
        if (PaymentMethodManager.delete(paymentMethod.getId())) {
            refreshTable();
        } else {
            showTemporaryError("Database error: Could not complete paymentMethod deletion.");
        }
    }

    /**
     * Renders explicit operational Active/Inactive state buttons inside the table tracking list.
     */
    private void addStatusToggleButtons() {
        colStatus.setCellValueFactory(new PropertyValueFactory<>("active"));
        colStatus.setCellFactory(param -> new TableCell<>() {
            private final Button btnToggle = new Button();
            private final HBox container = new HBox(btnToggle);

            {
                container.setAlignment(Pos.CENTER);
                btnToggle.getStyleClass().add("status-toggle-btn");
                btnToggle.setOnAction(event -> {
                    PaymentMethod paymentMethod = getTableView().getItems().get(getIndex());
                    boolean success = paymentMethod.isActive() ? PaymentMethodManager.disable(paymentMethod.getId()) : PaymentMethodManager.enable(paymentMethod.getId());
                    if (success) {
                        refreshTable();
                    } else {
                        showTemporaryError("Database error: Could not change method status.");
                    }
                });
            }

            @Override
            protected void updateItem(Boolean isActive, boolean empty) {
                super.updateItem(isActive, empty);
                if (empty || isActive == null) {
                    setGraphic(null);
                } else {
                    btnToggle.getStyleClass().removeAll("btn-active", "btn-inactive");
                    if (isActive) {
                        btnToggle.setText("Active");
                        //btnToggle.setGraphic(new FontIcon("medal-check"));
                        btnToggle.getStyleClass().add("btn-active");
                    } else {
                        btnToggle.setText("Inactive");
                        //btnToggle.setGraphic(new FontIcon("modal-block"));
                        btnToggle.getStyleClass().add("btn-inactive");
                    }
                    setGraphic(container);
                }
            }
        });
    }

    @FXML
    public void addPaymentMethod(ActionEvent actionEvent) {
        PaymentMethodController controller = MainController.navigate("payment-method-view.fxml", "Add New Payment Method");
        if (controller != null) {
            controller.setEntity(null);
        }
    }
}
