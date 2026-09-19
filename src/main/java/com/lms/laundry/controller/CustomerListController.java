package com.lms.laundry.controller;

import com.lms.laundry.manager.CustomerManager;
import com.lms.laundry.manager.IconManager;
import com.lms.laundry.model.Customer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.util.List;

/**
 * Controller responsible for monitoring, filtering, and managing customer registry tables.
 */
public class CustomerListController extends BaseListController<Customer> {

    @FXML
    private TableView<Customer> tblCustomers;
    @FXML
    private TableColumn<Customer, Integer> colId;
    @FXML
    private TableColumn<Customer, String> colName;
    @FXML
    private TableColumn<Customer, String> colPhone;
    @FXML
    private TableColumn<Customer, String> colEmail;
    @FXML
    private TableColumn<Customer, Double> colOrder;
    @FXML
    private TableColumn<Customer, Double> colPayment;
    @FXML
    private TableColumn<Customer, String> colBalance;
    @FXML
    private TableColumn<Customer, Void> colPayAction;
    @FXML
    private TableColumn<Customer, Void> colLedgerAction;

    @FXML
    public void initialize() {
        this.tblData = tblCustomers;
        // Data Column
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        // Currency Alignment
        colOrder.setCellValueFactory(new PropertyValueFactory<>("totalOrder"));
        colOrder.getStyleClass().add("col-right");
        colPayment.setCellValueFactory(new PropertyValueFactory<>("totalPayment"));
        colPayment.getStyleClass().add("col-right");
        colBalance.setCellValueFactory(new PropertyValueFactory<>("balanceDisplay"));
        colBalance.getStyleClass().add("col-right");
        colId.getStyleClass().add("col-center");
        addPaymentButtons();
        addLedgerButtons();
        setupTableAndFiltering();
    }

    private void addPaymentButtons() {
        if (colPayAction == null) return;
        colPayAction.getStyleClass().add("col-center");
        colPayAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnPay = new Button("Pay");
            private final HBox container = new HBox(btnPay);
            {
                container.setAlignment(Pos.CENTER);
                btnPay.getStyleClass().add("button-success");
                IconManager.decorateButton(btnPay);
                btnPay.setOnAction(event -> {
                    Customer customer = getTableView().getItems().get(getIndex());
                    PaymentController controller = MainController.navigate("payment-view.fxml", "Record Customer Payment");
                    if (controller != null) {
                        controller.setEntity(null);
                        controller.setPaymentPreset(customer, customer.isDue() ? customer.getBalance() : 0);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void addLedgerButtons() {
        if (colLedgerAction == null) return;
        colLedgerAction.getStyleClass().add("col-center");
        colLedgerAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnLedger = new Button("Ledger");
            private final HBox container = new HBox(btnLedger);
            {
                container.setAlignment(Pos.CENTER);
                btnLedger.getStyleClass().add("btn-refresh");
                IconManager.decorateButton(btnLedger);
                btnLedger.setOnAction(event -> {
                    Customer customer = getTableView().getItems().get(getIndex());
                    CustomerLedgerController controller = MainController.navigate("customer-ledger-view.fxml", "Customer Ledger");
                    if (controller != null) {
                        controller.setCustomer(customer);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    @Override
    protected boolean matchesSearch(Customer customer, String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            return true;
        }
        String filter = searchTerm.toLowerCase().trim();
        return customer.getName().toLowerCase().contains(filter) ||
                (customer.getPhone() != null && customer.getPhone().contains(filter)) ||
                (customer.getEmail() != null && customer.getEmail().toLowerCase().contains(filter));
    }

    @Override
    protected List<Customer> loadEntities() {
        return CustomerManager.getAll();
    }

    @Override
    protected String getEntityContextName() {
        return "Customers";
    }

    @Override
    protected void handleView(Customer customer) {
        CustomerController controller = MainController.navigate("customer-view.fxml", "Edit Customer");
        if (controller != null) {
            controller.setEntity(customer);
        }
    }

    @Override
    protected boolean canDeleteEntity(Customer customer) {
        if (customer.getBalance() > 0) {
            showTemporaryError("Cannot delete customer with an active balance ($"
                    + String.format("%.2f", customer.getBalance()) + ")");
            return false;
        }
        return true;
    }

    @Override
    protected void executeDeletion(Customer customer) {
        if (CustomerManager.delete(customer.getId())) {
            refreshTable();
        } else {
            showTemporaryError("Database error: Could not drop customer account record.");
        }
    }

    @FXML
    public void addCustomer(ActionEvent actionEvent) {
        CustomerController controller = MainController.navigate("customer-view.fxml", "Register New Customer");
        if (controller != null) {
            controller.setEntity(null);
        }
    }
}
