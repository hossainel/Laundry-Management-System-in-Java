package com.lms.laundry.controller;

import com.lms.laundry.manager.PaymentManager;
import com.lms.laundry.model.Payment;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.List;

public class PaymentListController extends BaseListController<Payment> {
    @FXML private TableView<Payment> tblPayments;
    @FXML private TableColumn<Payment, Integer> colId;
    @FXML private TableColumn<Payment, String> colCustomer;
    @FXML private TableColumn<Payment, Double> colAmount;
    @FXML private TableColumn<Payment, String> colMethod;
    @FXML private TableColumn<Payment, LocalDate> colDate;

    @FXML
    public void initialize() {
        this.tblData = tblPayments;
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colMethod.setCellValueFactory(new PropertyValueFactory<>("methodName"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        colId.getStyleClass().add("col-center");
        colAmount.getStyleClass().add("col-right");
        setupTableAndFiltering();
    }

    @Override
    protected boolean matchesSearch(Payment payment, String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) return true;
        String filter = searchTerm.toLowerCase().trim();
        return (payment.getCustomerName() != null && payment.getCustomerName().toLowerCase().contains(filter))
                || (payment.getMethodName() != null && payment.getMethodName().toLowerCase().contains(filter));
    }

    @Override protected List<Payment> loadEntities() { return PaymentManager.getAll(); }
    @Override protected String getEntityContextName() { return "Payments"; }

    @Override
    protected void handleView(Payment payment) {
        PaymentController controller = MainController.navigate("payment-view.fxml", "Edit Payment");
        if (controller != null) controller.setEntity(payment);
    }

    @Override protected boolean canDeleteEntity(Payment payment) { return true; }

    @Override
    protected void executeDeletion(Payment payment) {
        if (PaymentManager.delete(payment.getId())) refreshTable();
        else showTemporaryError("Database error: Could not delete payment.");
    }

    @FXML
    public void addPayment(ActionEvent event) {
        PaymentController controller = MainController.navigate("payment-view.fxml", "Record Payment");
        if (controller != null) controller.setEntity(null);
    }
}
