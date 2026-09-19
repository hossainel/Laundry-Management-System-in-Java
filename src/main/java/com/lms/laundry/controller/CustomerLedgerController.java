package com.lms.laundry.controller;

import com.lms.laundry.manager.CustomerManager;
import com.lms.laundry.manager.ReportManager;
import com.lms.laundry.model.Customer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;

public class CustomerLedgerController {
    @FXML private ComboBox<Customer> cmbCustomer;
    @FXML private TableView<ReportManager.LedgerRow> tblLedger;
    @FXML private TableColumn<ReportManager.LedgerRow, LocalDate> colDate;
    @FXML private TableColumn<ReportManager.LedgerRow, String> colType;
    @FXML private TableColumn<ReportManager.LedgerRow, String> colReference;
    @FXML private TableColumn<ReportManager.LedgerRow, Double> colDebit;
    @FXML private TableColumn<ReportManager.LedgerRow, Double> colCredit;
    @FXML private TableColumn<ReportManager.LedgerRow, Double> colBalance;
    @FXML private Label lblCustomerName;
    @FXML private Label lblTotalOrders;
    @FXML private Label lblTotalPayments;
    @FXML private Label lblTotalBalance;
    @FXML private Label lblStatus;

    @FXML
    public void initialize() {
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colReference.setCellValueFactory(new PropertyValueFactory<>("reference"));
        colDebit.setCellValueFactory(new PropertyValueFactory<>("debit"));
        colCredit.setCellValueFactory(new PropertyValueFactory<>("credit"));
        colBalance.setCellValueFactory(new PropertyValueFactory<>("balance"));
        colDebit.getStyleClass().add("col-right");
        colCredit.getStyleClass().add("col-right");
        colBalance.getStyleClass().add("col-right");

        cmbCustomer.setItems(FXCollections.observableArrayList(CustomerManager.getAll()));
        cmbCustomer.valueProperty().addListener((obs, old, customer) -> loadCustomerLedger(customer));
        if (!cmbCustomer.getItems().isEmpty()) {
            cmbCustomer.getSelectionModel().selectFirst();
        }
    }

    public void setCustomer(Customer customer) {
        if (customer == null) return;
        Customer match = cmbCustomer.getItems().stream()
                .filter(item -> item.getId() == customer.getId())
                .findFirst()
                .orElse(customer);
        cmbCustomer.getSelectionModel().select(match);
        loadCustomerLedger(match);
    }

    @FXML
    public void refreshLedger() {
        loadCustomerLedger(cmbCustomer.getValue());
    }

    private void loadCustomerLedger(Customer customer) {
        if (customer == null) {
            tblLedger.setItems(FXCollections.observableArrayList());
            lblStatus.setText("Select a customer");
            return;
        }

        Customer refreshed = CustomerManager.getById(customer.getId());
        if (refreshed == null) refreshed = customer;
        ObservableList<ReportManager.LedgerRow> rows = ReportManager.getCustomerLedger(refreshed.getId());
        tblLedger.setItems(rows);
        lblCustomerName.setText(refreshed.getName());
        lblTotalOrders.setText(String.format("%.2f", refreshed.getTotalOrder()));
        lblTotalPayments.setText(String.format("%.2f", refreshed.getTotalPayment()));
        lblTotalBalance.setText(refreshed.getBalanceDisplay());
        lblStatus.setText("Showing " + rows.size() + " ledger entries");
    }
}
