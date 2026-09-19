package com.lms.laundry.controller;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.Form;
import com.dlsc.formsfx.model.structure.Group;
import com.dlsc.formsfx.model.structure.SingleSelectionField;
import com.dlsc.formsfx.model.validators.DoubleRangeValidator;
import com.dlsc.formsfx.model.validators.StringLengthValidator;
import com.lms.laundry.manager.ExpenseManager;
import com.lms.laundry.manager.ExpenseCategoryManager;
import com.lms.laundry.manager.PaymentMethodManager;
import com.lms.laundry.model.Expense;
import com.lms.laundry.model.ExpenseCategory;
import com.lms.laundry.model.PaymentMethod;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import java.time.LocalDate;
import java.util.List;

public class ExpenseController extends BaseFormController<Expense> {

    private final StringProperty titleProp = new SimpleStringProperty("");
    private final DoubleProperty amountProp = new SimpleDoubleProperty(0.0);
    private final ObjectProperty<LocalDate> dateProp = new SimpleObjectProperty<>(LocalDate.now());
    private final StringProperty noteProp = new SimpleStringProperty("");

    private final ObjectProperty<ExpenseCategory> categoryProp = new SimpleObjectProperty<>();
    private final ObjectProperty<PaymentMethod> paymentMethodProp = new SimpleObjectProperty<>();

    private final ListProperty<ExpenseCategory> categoryOptions =
            new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<PaymentMethod> paymentMethodOptions =
            new SimpleListProperty<>(FXCollections.observableArrayList());

    private final ExpenseCategory placeholderCategory = new ExpenseCategory(-1, "-- Select Category --", true);
    private final PaymentMethod placeholderMethod = new PaymentMethod(-1, "-- Select Payment Method --", true);

    @FXML
    public void initialize() { loadDropdownDataFromDatabase(); }

    private void loadDropdownDataFromDatabase() {
        try {
            List<ExpenseCategory> categories = ExpenseCategoryManager.getAll();
            List<PaymentMethod> methods = PaymentMethodManager.getAll();
            this.categoryOptions.clear();
            this.paymentMethodOptions.clear();
            this.categoryOptions.add(placeholderCategory);
            this.paymentMethodOptions.add(placeholderMethod);
            if (categories != null) this.categoryOptions.addAll(categories);
            if (methods != null) this.paymentMethodOptions.addAll(methods);
        } catch (Exception e) {
            System.err.println("Failed to pre-populate form dropdown lists: " + e.getMessage());
        }
    }

    @Override
    protected void clearFormFields() {
        titleProp.set("");
        amountProp.set(0.0);
        dateProp.set(LocalDate.now());
        noteProp.set("");
        categoryProp.set(placeholderCategory);
        paymentMethodProp.set(placeholderMethod);
    }
    @Override
    protected Form initFormStructure(Expense expense) {
        if (isEditMode && expense != null) {
            titleProp.set(expense.getTitle());
            amountProp.set(expense.getAmount());
            dateProp.set(expense.getDate() != null ? expense.getDate() : LocalDate.now());
            noteProp.set(expense.getNote());
            categoryProp.set(
                    expense.getExpenseCategory() != null
                            ? expense.getExpenseCategory() : placeholderCategory
            );
            paymentMethodProp.set(
                    expense.getPaymentMethod() != null
                            ? expense.getPaymentMethod() : placeholderMethod
            );
        } else { clearFormFields(); }
        return Form.of(
            Group.of(
                Field.ofStringType(titleProp)
                    .label("Title")
                    .placeholder("e.g., Electric Bill")
                    .validate(
                        StringLengthValidator.atLeast(
                            2,
                            "Title must be at least 2 characters long."
                        )
                ),

                Field.ofSingleSelectionType(
                    categoryOptions,
                    categoryProp
                ).label("Expense Category"),

                Field.ofDoubleType(amountProp)
                    .label("Amount")
                    .placeholder("0.00")
                    .validate(
                        DoubleRangeValidator.atLeast(
                            0.01,
                            "Amount must be greater than 0."
                        )
                    ),

                Field.ofDate(dateProp).label("Expense Date"),

                Field.ofSingleSelectionType(
                    paymentMethodOptions,
                    paymentMethodProp
                ).label("Payment Method"),

                Field.ofStringType(noteProp)
                    .label("Note / Details")
                    .placeholder("Additional description details...")
            )
        );
    }

    @Override
    protected String getEditHeaderTitle() { return "Edit Expense Logging"; }

    @Override
    protected String getCreateHeaderTitle() { return "Log New Business Expense"; }

    @Override
    protected String getEditButtonText() { return "Update Expense"; }

    @Override
    protected String getCreateButtonText() { return "Save Expense"; }

    @Override
    protected String getTargetFxmlName() { return "expenses-view.fxml"; }

    @Override
    protected String getTargetViewTitle() { return "Expenses Management"; }

    @Override
    protected void executeSave() {
        if (categoryProp.get() == null || categoryProp.get().getId() == -1 ||
                paymentMethodProp.get() == null || paymentMethodProp.get().getId() == -1) {
            showToastError("Validation Error", "Please select both a valid Category and a Payment Method.");
            return;
        }

        if (isEditMode) {
            selectedEntity.setTitle(titleProp.get());
            selectedEntity.setAmount(amountProp.get());
            selectedEntity.setDate(dateProp.get());
            selectedEntity.setNote(noteProp.get());
            selectedEntity.setExpenseCategory(categoryProp.get());
            selectedEntity.setPaymentMethod(paymentMethodProp.get());

            if (ExpenseManager.update(selectedEntity)) {
                showToastSuccess("Success", "Expense profile altered successfully.");
                closeWindow();
            } else {
                showToastError("Database Error", "Unable to commit tracking updates.");
            }
        } else {
            Expense newExpense = new Expense(
                    0,
                    titleProp.get(),
                    categoryProp.get(),
                    amountProp.get(),
                    dateProp.get(),
                    paymentMethodProp.get(),
                    noteProp.get()
            );
            if (ExpenseManager.create(newExpense)) {
                showToastSuccess("Success", "New expense entry saved successfully.");
                closeWindow();
            } else {
                showToastError("Database Error", "Failed to compile background configurations.");
            }
        }
    }

    @Override
    protected void executeFormDeletion() {
        if (selectedEntity != null && ExpenseManager.delete(selectedEntity.getId())) {
            showToastSuccess("Success", "Operational expense entry has been completely removed.");
            closeWindow();
        } else {
            showToastError("Database Error", "Unable to safely drop selected tracking entry.");
        }
    }
}
