package com.lms.laundry.controller;

import com.lms.laundry.manager.OrderManager;
import com.lms.laundry.manager.IconManager;
import com.lms.laundry.model.Order;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.control.ComboBox;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class OrderListController extends BaseListController<Order> {
    @FXML private TableView<Order> tblOrders;
    @FXML private TableColumn<Order, Integer> colId;
    @FXML private TableColumn<Order, String> colCustomer;
    @FXML private TableColumn<Order, LocalDateTime> colOrderDate;
    @FXML private TableColumn<Order, LocalDate> colDeliveryDate;
    @FXML private TableColumn<Order, Order.Status> colStatus;
    @FXML private TableColumn<Order, Double> colTotal;
    @FXML private TableColumn<Order, Double> colPaid;
    @FXML private TableColumn<Order, Double> colDue;
    @FXML private TableColumn<Order, Void> colPayment;

    @FXML
    public void initialize() {
        this.tblData = tblOrders;
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colOrderDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        colDeliveryDate.setCellValueFactory(new PropertyValueFactory<>("deliveryDate"));
        addStatusQuickUpdate();
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colPaid.setCellValueFactory(new PropertyValueFactory<>("paidAmount"));
        colDue.setCellValueFactory(new PropertyValueFactory<>("dueAmount"));
        colId.getStyleClass().add("col-center");
        colStatus.getStyleClass().add("col-center");
        colTotal.getStyleClass().add("col-right");
        colPaid.getStyleClass().add("col-right");
        colDue.getStyleClass().add("col-right");
        addPaymentButtons();
        setupTableAndFiltering();
    }

    private void addPaymentButtons() {
        if (colPayment == null) return;
        colPayment.getStyleClass().add("col-center");
        colPayment.setCellFactory(param -> new TableCell<>() {
            private final javafx.scene.control.Button btnPay = new javafx.scene.control.Button("Pay");
            {
                btnPay.getStyleClass().add("button-success");
                IconManager.decorateButton(btnPay);
                btnPay.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    PaymentController controller = MainController.navigate("payment-view.fxml", "Record Order Payment");
                    if (controller != null) {
                        controller.setEntity(null);
                        controller.setPaymentPreset(order.getCustomer(), order.getDueAmount());
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Order order = getTableView().getItems().get(getIndex());
                    btnPay.setDisable(order == null || order.getDueAmount() <= 0);
                    setGraphic(btnPay);
                }
            }
        });
    }

    private void addStatusQuickUpdate() {
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(param -> new TableCell<>() {
            private final ComboBox<Order.Status> comboBox = new ComboBox<>(
                    FXCollections.observableArrayList(Order.Status.values())
            );

            {
                comboBox.setMaxWidth(Double.MAX_VALUE);
                comboBox.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    Order.Status selectedStatus = comboBox.getValue();
                    if (order != null && selectedStatus != null && selectedStatus != order.getStatus()) {
                        if (OrderManager.updateStatus(order.getId(), selectedStatus)) {
                            order.setStatus(selectedStatus);
                            refreshTable();
                        } else {
                            showTemporaryError("Database error: Could not update order status.");
                        }
                    }
                });
                setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Order.Status status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                } else {
                    comboBox.setValue(status);
                    setGraphic(comboBox);
                }
            }
        });
    }

    @Override
    protected boolean matchesSearch(Order order, String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) return true;
        String filter = searchTerm.toLowerCase().trim();
        return String.valueOf(order.getId()).contains(filter)
                || (order.getCustomerName() != null && order.getCustomerName().toLowerCase().contains(filter))
                || (order.getStatus() != null && order.getStatus().name().toLowerCase().contains(filter));
    }

    @Override protected List<Order> loadEntities() { return OrderManager.getAll(); }
    @Override protected String getEntityContextName() { return "Orders"; }

    @Override
    protected void handleView(Order order) {
        OrderController controller = MainController.navigate("order-view.fxml", "Edit Order");
        if (controller != null) controller.setEntity(OrderManager.getById(order.getId()));
    }

    @Override protected boolean canDeleteEntity(Order order) { return true; }

    @Override
    protected void executeDeletion(Order order) {
        if (OrderManager.delete(order.getId())) refreshTable();
        else showTemporaryError("Database error: Could not delete order.");
    }

    @FXML
    public void addOrder(ActionEvent event) {
        MainController.navigate("pos-view.fxml", "POS");
    }
}
