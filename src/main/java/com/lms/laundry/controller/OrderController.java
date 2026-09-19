package com.lms.laundry.controller;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.Form;
import com.dlsc.formsfx.model.structure.Group;
import com.dlsc.formsfx.model.validators.DoubleRangeValidator;

import com.lms.laundry.manager.CustomerManager;
import com.lms.laundry.manager.IconManager;
import com.lms.laundry.manager.OrderManager;
import com.lms.laundry.manager.ProductManager;
import com.lms.laundry.manager.ServiceManager;

import com.lms.laundry.model.Customer;
import com.lms.laundry.model.Order;
import com.lms.laundry.model.OrderItem;
import com.lms.laundry.model.POS;
import com.lms.laundry.model.Product;
import com.lms.laundry.model.Service;
import com.lms.laundry.model.ServiceType;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;

public class OrderController extends BaseFormController<Order> {
    private final ObjectProperty<Customer> customerProp = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> deliveryDateProp = new SimpleObjectProperty<>(LocalDate.now());
    private final ObjectProperty<Order.Status> statusProp = new SimpleObjectProperty<>(Order.Status.RECEIVED);
    private final DoubleProperty discountProp = new SimpleDoubleProperty(0.0);
    private final DoubleProperty paidProp = new SimpleDoubleProperty(0.0);
    private final ListProperty<Customer> customerOptions =
            new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<Order.Status> statusOptions =
            new SimpleListProperty<>(FXCollections.observableArrayList(Order.Status.values()));
    private final Customer placeholderCustomer = new Customer(-1, "-- Select Customer --", "", "", "");
    private final ObservableList<OrderItem> orderItems = FXCollections.observableArrayList();
    private final ObservableList<POS> serviceItems = FXCollections.observableArrayList();
    private final ObservableList<POS> productItems = FXCollections.observableArrayList();

    @FXML private ComboBox<String> cmbItemType;
    @FXML private ComboBox<POS> cmbItem;
    @FXML private Spinner<Integer> spnQuantity;
    @FXML private TableView<OrderItem> tblItems;
    @FXML private TableColumn<OrderItem, String> colItemType;
    @FXML private TableColumn<OrderItem, String> colItemName;
    @FXML private TableColumn<OrderItem, Double> colUnitPrice;
    @FXML private TableColumn<OrderItem, Integer> colQuantity;
    @FXML private TableColumn<OrderItem, Double> colLineTotal;
    @FXML private TableColumn<OrderItem, Void> colRemove;
    @FXML private Label lblSubtotal;
    @FXML private Label lblTotal;

    @FXML
    public void initialize() {
        customerOptions.set(FXCollections.observableArrayList());
        customerOptions.add(placeholderCustomer);
        customerOptions.addAll(CustomerManager.getAll());
        setupItemEditor();
        loadSaleItems();
    }

    @Override
    public void setEntity(Order entity) {
        super.setEntity(entity);
        orderItems.setAll(entity != null && entity.getItems() != null ? entity.getItems() : FXCollections.observableArrayList());
        if (tblItems != null) tblItems.setItems(orderItems);
        updateItemTotals();
    }

    @Override
    protected void clearFormFields() {
        customerProp.set(placeholderCustomer);
        deliveryDateProp.set(LocalDate.now());
        statusProp.set(Order.Status.RECEIVED);
        discountProp.set(0);
        paidProp.set(0);
    }

    @Override
    protected Form initFormStructure(Order order) {
        if (isEditMode && order != null) {
            customerProp.set(order.getCustomer() != null ? order.getCustomer() : new Customer(order.getCustomerId(), order.getCustomerName(), "", "", ""));
            deliveryDateProp.set(order.getDeliveryDate());
            statusProp.set(order.getStatus());
            discountProp.set(order.getDiscount());
            paidProp.set(order.getPaidAmount());
        }
        return Form.of(
                Group.of(
                        Field.ofSingleSelectionType(customerOptions, customerProp).label("Customer"),
                        Field.ofDate(deliveryDateProp).label("Delivery Date"),
                        Field.ofSingleSelectionType(statusOptions, statusProp).label("Status"),
                        Field.ofDoubleType(discountProp).label("Discount")
                                .validate(DoubleRangeValidator.atLeast(0, "Discount cannot be negative."))
                )
        );
    }

    private void setupItemEditor() {
        if (tblItems == null) return;
        cmbItemType.setItems(FXCollections.observableArrayList("SERVICE", "PRODUCT"));
        cmbItemType.getSelectionModel().selectFirst();
        cmbItemType.valueProperty().addListener((obs, old, value) -> switchItemSource(value));
        spnQuantity.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1));
        colItemType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getItemType().name()));
        colItemName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colLineTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colUnitPrice.getStyleClass().add("col-right");
        colQuantity.getStyleClass().add("col-center");
        colLineTotal.getStyleClass().add("col-right");
        colRemove.setCellFactory(param -> new TableCell<>() {
            private final Button btnRemove = new Button("Remove");
            {
                btnRemove.getStyleClass().add("button-danger");
                IconManager.decorateButton(btnRemove);
                btnRemove.setOnAction(event -> {
                    orderItems.remove(getTableView().getItems().get(getIndex()));
                    updateItemTotals();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnRemove);
            }
        });
        tblItems.setItems(orderItems);
    }

    private void loadSaleItems() {
        serviceItems.clear();
        productItems.clear();
        for (Service service : ServiceManager.getAll()) {
            if (!service.isActive()) continue;
            for (ServiceType type : service.getServiceTypes()) {
                serviceItems.add(new POS(service.getName() + " - " + type.getName(), OrderItem.ItemType.SERVICE, service.getId(), type.getPrice()));
            }
        }
        for (Product product : ProductManager.getAll()) {
            if (product.isActive()) {
                productItems.add(new POS(product.getName(), OrderItem.ItemType.PRODUCT, product.getId(), product.getPrice()));
            }
        }
        switchItemSource(cmbItemType != null ? cmbItemType.getValue() : "SERVICE");
    }

    private void switchItemSource(String type) {
        if (cmbItem == null) return;
        cmbItem.setItems("PRODUCT".equals(type) ? productItems : serviceItems);
        if (!cmbItem.getItems().isEmpty()) cmbItem.getSelectionModel().selectFirst();
    }

    @FXML
    public void addLineItem() {
        POS selected = cmbItem.getValue();
        if (selected == null) {
            showToastError("Validation Error", "Please select an item to add.");
            return;
        }
        orderItems.add(new OrderItem(selected.getItemType(), selected.getItemId(), selected.getLabel(), selected.getPrice(), spnQuantity.getValue()));
        updateItemTotals();
    }

    private void updateItemTotals() {
        double subtotal = orderItems.stream().mapToDouble(OrderItem::getTotal).sum();
        double total = Math.max(0, subtotal - discountProp.get());
        if (lblSubtotal != null) lblSubtotal.setText(String.format("%.2f", subtotal));
        if (lblTotal != null) lblTotal.setText(String.format("%.2f", total));
    }

    @Override protected String getEditHeaderTitle() { return "Edit Order"; }
    @Override protected String getCreateHeaderTitle() { return "Order Details"; }
    @Override protected String getEditButtonText() { return "Update Order"; }
    @Override protected String getCreateButtonText() { return "Save Order"; }
    @Override protected String getTargetFxmlName() { return "orders-view.fxml"; }
    @Override protected String getTargetViewTitle() { return "Orders"; }

    @Override
    protected void executeSave() {
        if (selectedEntity == null) {
            showToastError("Order Error", "Create new orders from the POS screen.");
            return;
        }
        if (customerProp.get() == null || customerProp.get().getId() == -1) {
            showToastError("Validation Error", "Please select a valid customer.");
            return;
        }
        selectedEntity.setCustomer(customerProp.get());
        selectedEntity.setDeliveryDate(deliveryDateProp.get());
        selectedEntity.setStatus(statusProp.get());
        selectedEntity.setDiscount(discountProp.get());
        selectedEntity.setItems(FXCollections.observableArrayList(orderItems));
        selectedEntity.calculateTotals();
        if (OrderManager.update(selectedEntity)) {
            showToastSuccess("Success", "Order updated successfully.");
            closeWindow();
        } else {
            showToastError("Database Error", "Unable to update order.");
        }
    }

    @Override
    protected void executeFormDeletion() {
        if (selectedEntity != null && OrderManager.delete(selectedEntity.getId())) {
            showToastSuccess("Success", "Order deleted successfully.");
            closeWindow();
        } else {
            showToastError("Database Error", "Unable to delete selected order.");
        }
    }
}
