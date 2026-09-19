package com.lms.laundry.controller;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.Form;
import com.dlsc.formsfx.model.structure.Group;
import com.dlsc.formsfx.model.validators.DoubleRangeValidator;
import com.lms.laundry.manager.CustomerManager;
import com.lms.laundry.manager.PaymentManager;
import com.lms.laundry.manager.PaymentMethodManager;
import com.lms.laundry.model.Customer;
import com.lms.laundry.model.Payment;
import com.lms.laundry.model.PaymentMethod;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;

import java.time.LocalDate;
import java.util.List;

public class PaymentController extends BaseFormController<Payment> {
    private final ObjectProperty<Customer> customerProp = new SimpleObjectProperty<>();
    private final DoubleProperty amountProp = new SimpleDoubleProperty(0.0);
    private final ObjectProperty<PaymentMethod> methodProp = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> dateProp = new SimpleObjectProperty<>(LocalDate.now());

    private final ListProperty<Customer> customerOptions =
            new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<PaymentMethod> methodOptions =
            new SimpleListProperty<>(FXCollections.observableArrayList());

    private final Customer placeholderCustomer = new Customer(-1, "-- Select Customer --", "", "", "");
    private final PaymentMethod placeholderMethod = new PaymentMethod(-1, "-- Select Payment Method --", true);

    @FXML
    public void initialize() {
        loadDropdownData();
    }

    public void setPaymentPreset(Customer customer, double amount) {
        if (customer != null) {
            Customer match = customerOptions.stream()
                    .filter(option -> option.getId() == customer.getId())
                    .findFirst()
                    .orElse(customer);
            customerProp.set(match);
        }
        amountProp.set(Math.max(0, amount));
        methodProp.set(findCashMethod());
        dateProp.set(LocalDate.now());
    }

    private void loadDropdownData() {
        List<Customer> customers = CustomerManager.getAll();
        List<PaymentMethod> methods = PaymentMethodManager.getAll();
        customerOptions.clear();
        methodOptions.clear();
        customerOptions.add(placeholderCustomer);
        methodOptions.add(placeholderMethod);
        if (customers != null) customerOptions.addAll(customers);
        if (methods != null) methodOptions.addAll(methods);
    }

    @Override
    protected void clearFormFields() {
        customerProp.set(placeholderCustomer);
        amountProp.set(0.0);
        methodProp.set(findCashMethod());
        dateProp.set(LocalDate.now());
    }

    private PaymentMethod findCashMethod() {
        return methodOptions.stream()
                .filter(method -> "Cash".equalsIgnoreCase(method.getName()))
                .findFirst()
                .orElse(placeholderMethod);
    }

    @Override
    protected Form initFormStructure(Payment payment) {
        if (isEditMode && payment != null) {
            customerProp.set(payment.getCustomer() != null ? payment.getCustomer() : new Customer(payment.getCustomerId(), payment.getCustomerName(), "", "", ""));
            amountProp.set(payment.getAmount());
            methodProp.set(payment.getPaymentMethod() != null ? payment.getPaymentMethod() : new PaymentMethod(payment.getMethodId(), payment.getMethodName(), true));
            dateProp.set(payment.getPaymentDate() != null ? payment.getPaymentDate() : LocalDate.now());
        }

        return Form.of(
                Group.of(
                        Field.ofSingleSelectionType(customerOptions, customerProp).label("Customer"),
                        Field.ofDoubleType(amountProp).label("Amount").placeholder("0.00")
                                .validate(DoubleRangeValidator.atLeast(0.01, "Amount must be greater than 0.")),
                        Field.ofSingleSelectionType(methodOptions, methodProp).label("Payment Method"),
                        Field.ofDate(dateProp).label("Payment Date")
                )
        );
    }

    @Override protected String getEditHeaderTitle() { return "Edit Payment"; }
    @Override protected String getCreateHeaderTitle() { return "Record Payment"; }
    @Override protected String getEditButtonText() { return "Update Payment"; }
    @Override protected String getCreateButtonText() { return "Save Payment"; }
    @Override protected String getTargetFxmlName() { return "payments-view.fxml"; }
    @Override protected String getTargetViewTitle() { return "Payments"; }

    @Override
    protected void executeSave() {
        if (customerProp.get() == null || customerProp.get().getId() == -1 ||
                methodProp.get() == null || methodProp.get().getId() == -1) {
            showToastError("Validation Error", "Please select a valid customer and payment method.");
            return;
        }

        if (isEditMode) {
            selectedEntity.setCustomer(customerProp.get());
            selectedEntity.setAmount(amountProp.get());
            selectedEntity.setPaymentMethod(methodProp.get());
            selectedEntity.setPaymentDate(dateProp.get());
            if (PaymentManager.update(selectedEntity)) {
                showToastSuccess("Success", "Payment updated successfully.");
                closeWindow();
            } else {
                showToastError("Database Error", "Unable to update payment.");
            }
        } else {
            Payment payment = new Payment(0, methodProp.get(), amountProp.get(), customerProp.get(), dateProp.get(), null, null);
            if (PaymentManager.create(payment)) {
                showToastSuccess("Success", "Payment recorded successfully.");
                closeWindow();
            } else {
                showToastError("Database Error", "Unable to record payment.");
            }
        }
    }

    @Override
    protected void executeFormDeletion() {
        if (selectedEntity != null && PaymentManager.delete(selectedEntity.getId())) {
            showToastSuccess("Success", "Payment deleted successfully.");
            closeWindow();
        } else {
            showToastError("Database Error", "Unable to delete selected payment.");
        }
    }
}
