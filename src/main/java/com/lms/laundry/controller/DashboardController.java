package com.lms.laundry.controller;

import com.lms.laundry.manager.DashboardManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class DashboardController {
    @FXML
    private Label lblTotalOrders, lblTodaySales, lblTotalDue, lblTotalCustomers;
    @FXML
    private Label lblReceivedOrders, lblWashingOrders, lblReadyOrders, lblDeliveredOrders;
    @FXML
    private ListView<String> listActivity;

    @FXML
    public void initialize() {
        refreshDashboard();
    }

    public void refreshDashboard() {
        DashboardManager.DashboardSummary summary = DashboardManager.getSummary();
        lblTotalCustomers.setText(String.valueOf(summary.getTotalCustomers()));
        lblTotalOrders.setText(String.valueOf(summary.getTotalOrders()));
        lblTodaySales.setText(String.format("%.2f", summary.getTodaySales()));
        lblTotalDue.setText(String.format("%.2f", summary.getTotalDue()));
        lblReceivedOrders.setText(String.valueOf(summary.getReceivedOrders()));
        lblWashingOrders.setText(String.valueOf(summary.getWashingOrders()));
        lblReadyOrders.setText(String.valueOf(summary.getReadyOrders()));
        lblDeliveredOrders.setText(String.valueOf(summary.getDeliveredOrders()));
        listActivity.setItems(FXCollections.observableArrayList(summary.getActivity()));
    }

    @FXML public void openPos() { MainController.navigate("pos-view.fxml", "POS"); }
    @FXML public void openOrders() { MainController.navigate("orders-view.fxml", "Orders"); }
    @FXML public void openPayments() { MainController.navigate("payments-view.fxml", "Payments"); }
}
