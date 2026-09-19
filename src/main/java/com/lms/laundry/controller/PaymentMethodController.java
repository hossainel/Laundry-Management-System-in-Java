package com.lms.laundry.controller;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.Form;
import com.dlsc.formsfx.model.structure.Group;
import com.dlsc.formsfx.model.validators.StringLengthValidator;
import com.lms.laundry.manager.PaymentMethodManager;
import com.lms.laundry.model.PaymentMethod;
import javafx.beans.property.*;

public class PaymentMethodController extends BaseFormController<PaymentMethod> {

    private final StringProperty nameProp = new SimpleStringProperty("");
    private final BooleanProperty activeProp = new SimpleBooleanProperty(true);

    @Override
    protected void clearFormFields() {
        nameProp.set("");
        activeProp.set(true);
    }

    @Override
    protected Form initFormStructure(PaymentMethod paymentMethod) {
        if (isEditMode && paymentMethod != null) {
            nameProp.set(paymentMethod.getName());
            activeProp.set(paymentMethod.isActive());
        }

        return Form.of(
                Group.of(
                        Field.ofStringType(nameProp).label("Full Name")
                                .placeholder("Enter full name")
                                .validate(StringLengthValidator.atLeast(2, "Name must be at least 2 characters long.")),
                        Field.ofBooleanType(activeProp).label("Method Status Active")
                )
        );
    }

    @Override
    protected String getEditHeaderTitle() {
        return "Edit Payment Method";
    }

    @Override
    protected String getCreateHeaderTitle() {
        return "Add New Payment Method";
    }

    @Override
    protected String getEditButtonText() {
        return "Update Payment Method";
    }

    @Override
    protected String getCreateButtonText() {
        return "Save Payment Method";
    }

    @Override
    protected String getTargetFxmlName() {
        return "payment-methods-view.fxml";
    }

    @Override
    protected String getTargetViewTitle() {
        return "Payment Methods";
    }

    @Override
    protected void executeSave() {
        if (isEditMode) {
            selectedEntity.setName(nameProp.get());
            selectedEntity.setActive(activeProp.get());
            if (PaymentMethodManager.update(selectedEntity)) {
                showToastSuccess("Success", "Payment Method adjusted successfully.");
                closeWindow();
            } else {
                showToastError("Database Error", "Unable to commit tracking updates.");
            }
        } else {
            PaymentMethod newPaymentMethod = new PaymentMethod(0, nameProp.get(), activeProp.get());
            if (PaymentMethodManager.create(newPaymentMethod)) {
                showToastSuccess("Success", "New payment Method saved successfully.");
                closeWindow();
            } else {
                showToastError("Database Error", "Failed to compile background configurations.");
            }
        }
    }

    @Override
    protected void executeFormDeletion() {
        if (selectedEntity != null && PaymentMethodManager.delete(selectedEntity.getId())) {
            showToastSuccess("Success", "Payment Method has been completely removed.");
            closeWindow();
        } else {
            showToastError("Database Error", "Unable to safely drop selected category.");
        }
    }
}
