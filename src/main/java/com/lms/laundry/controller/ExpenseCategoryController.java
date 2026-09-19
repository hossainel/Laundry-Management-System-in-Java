package com.lms.laundry.controller;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.Form;
import com.dlsc.formsfx.model.structure.Group;
import com.dlsc.formsfx.model.validators.StringLengthValidator;
import com.lms.laundry.manager.ExpenseCategoryManager;
import com.lms.laundry.model.ExpenseCategory;
import javafx.beans.property.*;

public class ExpenseCategoryController extends BaseFormController<ExpenseCategory> {

    private final StringProperty nameProp = new SimpleStringProperty("");
    private final BooleanProperty activeProp = new SimpleBooleanProperty(true);

    @Override
    protected void clearFormFields() {
        nameProp.set("");
        activeProp.set(true);
    }

    @Override
    protected Form initFormStructure(ExpenseCategory expenseCategory) {
        if (isEditMode && expenseCategory != null) {
            nameProp.set(expenseCategory.getName());
            activeProp.set(expenseCategory.isActive());
        }

        return Form.of(
            Group.of(
                Field.ofStringType(nameProp).label("Expense Category Name")
                    .placeholder("Enter full name")
                    .validate(StringLengthValidator.atLeast(2, "Name must be at least 2 characters long.")),
                Field.ofBooleanType(activeProp).label("Category Status Active")
            )
        );
    }

    @Override
    protected String getEditHeaderTitle() {
        return "Edit Expense Category";
    }

    @Override
    protected String getCreateHeaderTitle() {
        return "Add New Expense Category";
    }

    @Override
    protected String getEditButtonText() {
        return "Update Expense Category";
    }

    @Override
    protected String getCreateButtonText() {
        return "Save Expense Category";
    }

    @Override
    protected String getTargetFxmlName() {
        return "expense-categories-view.fxml";
    }

    @Override
    protected String getTargetViewTitle() {
        return "Expense Categories";
    }

    @Override
    protected void executeSave() {
        if (isEditMode) {
            selectedEntity.setName(nameProp.get());
            selectedEntity.setActive(activeProp.get());
            if (ExpenseCategoryManager.update(selectedEntity)) {
                showToastSuccess("Success", "Expense category adjusted successfully.");
                closeWindow();
            } else {
                showToastError("Database Error", "Unable to commit tracking updates.");
            }
        } else {
            ExpenseCategory newExpenseCategory = new ExpenseCategory(0, nameProp.get(), activeProp.get());
            if (ExpenseCategoryManager.create(newExpenseCategory)) {
                showToastSuccess("Success", "New expense category saved successfully.");
                closeWindow();
            } else {
                showToastError("Database Error", "Failed to compile background configurations.");
            }
        }
    }

    @Override
    protected void executeFormDeletion() {
        if (selectedEntity != null && ExpenseCategoryManager.delete(selectedEntity.getId())) {
            showToastSuccess("Success", "Expense category has been completely removed.");
            closeWindow();
        } else {
            showToastError("Database Error", "Unable to safely drop selected category.");
        }
    }
}
