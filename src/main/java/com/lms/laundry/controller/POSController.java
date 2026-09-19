package com.lms.laundry.controller;

import com.lms.laundry.manager.CustomerManager;
import com.lms.laundry.manager.IconManager;
import com.lms.laundry.manager.OrderManager;
import com.lms.laundry.manager.PaymentMethodManager;
import com.lms.laundry.manager.ProductManager;
import com.lms.laundry.manager.ServiceManager;
import com.lms.laundry.model.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import org.controlsfx.control.Notifications;

import java.time.LocalDate;
import java.util.List;

public class POSController {
    @FXML private ComboBox<Customer> cmbCustomer;
    @FXML private ComboBox<String> cmbItemType;
    @FXML private ComboBox<POS> cmbItem;
    @FXML private Spinner<Integer> spnQuantity;
    @FXML private DatePicker dpDeliveryDate;
    @FXML private TextField txtDiscount;
    @FXML private TextField txtPaid;
    @FXML private ComboBox<PaymentMethod> cmbPaymentMethod;
    @FXML private TableView<OrderItem> tblCart;
    @FXML private TableColumn<OrderItem, String> colType;
    @FXML private TableColumn<OrderItem, String> colName;
    @FXML private TableColumn<OrderItem, Double> colPrice;
    @FXML private TableColumn<OrderItem, Integer> colQty;
    @FXML private TableColumn<OrderItem, Double> colTotal;
    @FXML private TableColumn<OrderItem, Void> colAction;
    @FXML private Label lblSubtotal;
    @FXML private Label lblTotal;
    @FXML private Label lblDue;
    @FXML private Label lblPreviousBalance;
    @FXML private Label lblCurrentDue;
    @FXML private Label lblAfterBalance;
    @FXML private CheckBox chkAdjustAdvance;
    @FXML private Label lblStatus;

    private final ObservableList<OrderItem> cartItems = FXCollections.observableArrayList();
    private final ObservableList<POS> serviceItems = FXCollections.observableArrayList();
    private final ObservableList<POS> productItems = FXCollections.observableArrayList();
    private final ObservableList<Customer> customerItems = FXCollections.observableArrayList();
    private boolean updatingCustomerFilter = false;
    private boolean updatingItemFilter = false;

    @FXML
    public void initialize() {
        customerItems.setAll(CustomerManager.getAll());
        cmbCustomer.setItems(FXCollections.observableArrayList(customerItems));
        cmbPaymentMethod.setItems(FXCollections.observableArrayList(PaymentMethodManager.getAll()));
        cmbItemType.setItems(FXCollections.observableArrayList("SERVICE", "PRODUCT"));
        cmbItemType.getSelectionModel().selectFirst();
        dpDeliveryDate.setValue(LocalDate.now().plusDays(2));
        spnQuantity.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1));

        loadSaleItems();
        setupCartTable();
        setupSearchableCustomerCombo();
        setupSearchableItemCombo();
        cmbItemType.valueProperty().addListener((obs, old, value) -> switchItemSource(value));
        switchItemSource(cmbItemType.getValue());
        selectDefaultCustomer();
        selectDefaultPaymentMethod();
        cmbCustomer.valueProperty().addListener((obs, old, value) -> updateTotals());
        txtDiscount.textProperty().addListener((obs, old, value) -> updateTotals());
        txtPaid.textProperty().addListener((obs, old, value) -> updateTotals());
        chkAdjustAdvance.selectedProperty().addListener((obs, old, value) -> updateTotals());
        tblCart.setItems(cartItems);
        updateTotals();
    }

    private void selectDefaultCustomer() {
        customerItems.stream()
                .filter(customer -> customer.getId() == 1 || "Walk-in Customer".equalsIgnoreCase(customer.getName()))
                .findFirst()
                .ifPresent(customer -> cmbCustomer.getSelectionModel().select(customer));
    }

    private void selectDefaultPaymentMethod() {
        cmbPaymentMethod.getItems().stream()
                .filter(method -> "Cash".equalsIgnoreCase(method.getName()))
                .findFirst()
                .ifPresent(method -> cmbPaymentMethod.getSelectionModel().select(method));
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
    }

    private void setupCartTable() {
        colType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getItemType().name()));
        colName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colPrice.getStyleClass().add("col-right");
        colQty.getStyleClass().add("col-center");
        colTotal.getStyleClass().add("col-right");
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnRemove = new Button("Remove");
            {
                btnRemove.getStyleClass().add("button-danger");
                IconManager.decorateButton(btnRemove);
                btnRemove.setOnAction(event -> {
                    cartItems.remove(getTableView().getItems().get(getIndex()));
                    updateTotals();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnRemove);
            }
        });
    }

    private void switchItemSource(String type) {
        if (cmbItem.getEditor() != null) cmbItem.getEditor().clear();
        cmbItem.setItems(FXCollections.observableArrayList("PRODUCT".equals(type) ? productItems : serviceItems));
        if (!cmbItem.getItems().isEmpty()) cmbItem.getSelectionModel().selectFirst();
    }

    private void setupSearchableCustomerCombo() {
        cmbCustomer.setEditable(true);
        cmbCustomer.setConverter(new StringConverter<>() {
            @Override
            public String toString(Customer customer) {
                if (customer == null) return "";
                String phone = customer.getPhone() == null || customer.getPhone().isBlank() ? "" : " - " + customer.getPhone();
                return customer.getName() + phone;
            }

            @Override
            public Customer fromString(String text) {
                return customerItems.stream()
                        .filter(customer -> matchesCustomer(customer, text))
                        .findFirst()
                        .orElse(cmbCustomer.getValue());
            }
        });
        cmbCustomer.getEditor().textProperty().addListener((obs, old, text) -> {
            if (updatingCustomerFilter) return;
            Customer selected = cmbCustomer.getValue();
            String selectedText = cmbCustomer.getConverter().toString(selected);
            if (selected != null && selectedText.equals(text)) return;
            filterCustomers(text);
        });
    }

    private void filterCustomers(String text) {
        updatingCustomerFilter = true;
        ObservableList<Customer> filtered = FXCollections.observableArrayList();
        for (Customer customer : customerItems) {
            if (matchesCustomer(customer, text)) filtered.add(customer);
        }
        cmbCustomer.setItems(filtered);
        cmbCustomer.show();
        cmbCustomer.getEditor().setText(text == null ? "" : text);
        cmbCustomer.getEditor().positionCaret(cmbCustomer.getEditor().getText().length());
        updatingCustomerFilter = false;
    }

    private boolean matchesCustomer(Customer customer, String text) {
        if (text == null || text.isBlank()) return true;
        String filter = text.toLowerCase().trim();
        return (customer.getName() != null && customer.getName().toLowerCase().contains(filter))
                || (customer.getPhone() != null && customer.getPhone().toLowerCase().contains(filter));
    }

    private void setupSearchableItemCombo() {
        cmbItem.setEditable(true);
        cmbItem.setConverter(new StringConverter<>() {
            @Override
            public String toString(POS item) {
                return item == null ? "" : item.getLabel();
            }

            @Override
            public POS fromString(String text) {
                return currentItemSource().stream()
                        .filter(item -> matchesSaleItem(item, text))
                        .findFirst()
                        .orElse(cmbItem.getValue());
            }
        });
        cmbItem.getEditor().textProperty().addListener((obs, old, text) -> {
            if (updatingItemFilter) return;
            POS selected = cmbItem.getValue();
            if (selected != null && selected.getLabel().equals(text)) return;
            filterSaleItems(text);
        });
    }

    private void filterSaleItems(String text) {
        updatingItemFilter = true;
        ObservableList<POS> filtered = FXCollections.observableArrayList();
        for (POS item : currentItemSource()) {
            if (matchesSaleItem(item, text)) filtered.add(item);
        }
        cmbItem.setItems(filtered);
        cmbItem.show();
        cmbItem.getEditor().setText(text == null ? "" : text);
        cmbItem.getEditor().positionCaret(cmbItem.getEditor().getText().length());
        updatingItemFilter = false;
    }

    private List<POS> currentItemSource() {
        return "PRODUCT".equals(cmbItemType.getValue()) ? productItems : serviceItems;
    }

    private boolean matchesSaleItem(POS item, String text) {
        if (text == null || text.isBlank()) return true;
        String filter = text.toLowerCase().trim();
        return item.getLabel() != null && item.getLabel().toLowerCase().contains(filter);
    }

    @FXML
    public void addToCart() {
        POS selected = resolveSelectedItem();
        if (selected == null) {
            setStatus("Please select an item.");
            return;
        }
        int qty = spnQuantity.getValue();
        cartItems.add(new OrderItem(selected.getItemType(), selected.getItemId(), selected.getLabel(), selected.getPrice(), qty));
        updateTotals();
    }

    @FXML
    public void saveOrder() {
        Customer customer = resolveSelectedCustomer();
        if (customer == null) {
            setStatus("Please select a customer.");
            return;
        }
        if (cartItems.isEmpty()) {
            setStatus("Add at least one item before saving.");
            return;
        }
        double paid = parseDouble(txtPaid.getText());
        PaymentMethod method = cmbPaymentMethod.getValue();
        if (paid > 0 && method == null) {
            setStatus("Select a payment method for paid amount.");
            return;
        }

        Order order = new Order();
        order.setCustomer(customer);
        order.setDeliveryDate(dpDeliveryDate.getValue());
        order.setStatus(Order.Status.RECEIVED);
        order.setItems(FXCollections.observableArrayList(cartItems));
        order.setDiscount(parseDouble(txtDiscount.getText()));
        double appliedAdvance = shouldApplyPreviousAdvance() ? getPreviousAdvance() : 0;
        order.setPaidAmount(paid + appliedAdvance);
        order.calculateTotals();

        Payment payment = null;
        if (paid > 0) {
            payment = new Payment(0, method, paid, customer, LocalDate.now(), null, null);
        }
        if (OrderManager.create(order, payment, shouldApplyPreviousAdvance())) {
            Notifications.create().title("Success").text("Order saved successfully.")
                    .position(Pos.TOP_RIGHT).showInformation();
            clearSale();
        } else {
            setStatus("Database error: Could not save order.");
        }
    }

    @FXML
    public void clearSale() {
        cartItems.clear();
        txtDiscount.clear();
        txtPaid.clear();
        spnQuantity.getValueFactory().setValue(1);
        dpDeliveryDate.setValue(LocalDate.now().plusDays(2));
        selectDefaultCustomer();
        selectDefaultPaymentMethod();
        if (chkAdjustAdvance != null) chkAdjustAdvance.setSelected(false);
        updateTotals();
        setStatus("Ready");
    }

    private void updateTotals() {
        double subtotal = cartItems.stream().mapToDouble(OrderItem::getTotal).sum();
        double discount = parseDouble(txtDiscount.getText());
        double paid = parseDouble(txtPaid.getText());
        double previousBalance = getPreviousBalance();
        double appliedAdvance = shouldApplyPreviousAdvance() ? Math.min(Math.abs(previousBalance), Math.max(0, subtotal - discount - paid)) : 0;
        double total = Math.max(0, subtotal - discount);
        double due = Math.max(0, total - paid - appliedAdvance);
        double afterBalance = previousBalance + due;
        lblSubtotal.setText(String.format("%.2f", subtotal));
        lblTotal.setText(String.format("%.2f", total));
        lblDue.setText(String.format("%.2f", due));
        if (lblCurrentDue != null) lblCurrentDue.setText(formatDueAdvance(due));
        if (lblPreviousBalance != null) lblPreviousBalance.setText(formatDueAdvance(previousBalance));
        if (lblAfterBalance != null) lblAfterBalance.setText(formatDueAdvance(afterBalance));
        if (chkAdjustAdvance != null) {
            chkAdjustAdvance.setDisable(previousBalance >= 0);
        }
    }

    private boolean shouldApplyPreviousAdvance() {
        return chkAdjustAdvance != null && chkAdjustAdvance.isSelected() && getPreviousBalance() < 0;
    }

    private double getPreviousAdvance() {
        double previousBalance = getPreviousBalance();
        if (previousBalance >= 0) return 0;
        double subtotal = cartItems.stream().mapToDouble(OrderItem::getTotal).sum();
        double total = Math.max(0, subtotal - parseDouble(txtDiscount.getText()));
        double paid = parseDouble(txtPaid.getText());
        return Math.min(Math.abs(previousBalance), Math.max(0, total - paid));
    }

    private double getPreviousBalance() {
        Customer customer = resolveSelectedCustomer();
        if (customer == null || customer.getId() <= 0) return 0;
        Customer refreshed = CustomerManager.getById(customer.getId());
        return refreshed != null ? refreshed.getBalance() : customer.getBalance();
    }

    private Customer resolveSelectedCustomer() {
        String text = cmbCustomer.getEditor() != null ? cmbCustomer.getEditor().getText() : "";
        Customer value = cmbCustomer.getValue();
        if (value != null && (text == null || text.isBlank() || matchesCustomer(value, text))) return value;
        return customerItems.stream()
                .filter(customer -> matchesCustomer(customer, text))
                .findFirst()
                .orElse(value);
    }

    private POS resolveSelectedItem() {
        String text = cmbItem.getEditor() != null ? cmbItem.getEditor().getText() : "";
        POS value = cmbItem.getValue();
        if (value != null && (text == null || text.isBlank() || matchesSaleItem(value, text))) return value;
        return currentItemSource().stream()
                .filter(item -> matchesSaleItem(item, text))
                .findFirst()
                .orElse(value);
    }

    private String formatDueAdvance(double balance) {
        if (balance > 0.005) return "Due " + String.format("%.2f", balance);
        if (balance < -0.005) return "Advance " + String.format("%.2f", Math.abs(balance));
        return "Settled 0.00";
    }

    private double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) return 0.0;
        try { return Double.parseDouble(value.trim()); }
        catch (NumberFormatException e) { return 0.0; }
    }

    private void setStatus(String message) {
        if (lblStatus != null) lblStatus.setText(message);
    }
}
