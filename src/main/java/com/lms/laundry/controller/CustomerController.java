package com.lms.laundry.controller;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.Form;
import com.dlsc.formsfx.model.structure.Group;
import com.dlsc.formsfx.model.validators.StringLengthValidator;
import com.lms.laundry.manager.CustomerManager;
import com.lms.laundry.model.Customer;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CustomerController extends BaseFormController<Customer> {

    private final StringProperty nameProp = new SimpleStringProperty("");
    private final StringProperty phoneProp = new SimpleStringProperty("");
    private final StringProperty emailProp = new SimpleStringProperty("");
    private final StringProperty addressProp = new SimpleStringProperty("");

    @Override
    protected void clearFormFields() {
        nameProp.set("");
        phoneProp.set("");
        emailProp.set("");
        addressProp.set("");
    }

    @Override
    protected Form initFormStructure(Customer customer) {
        if (isEditMode && customer != null) {
            nameProp.set(customer.getName());
            phoneProp.set(customer.getPhone());
            emailProp.set(customer.getEmail() != null ? customer.getEmail() : "");
            addressProp.set(customer.getAddress() != null ? customer.getAddress() : "");
        }

        return Form.of(
                Group.of(
                        Field.ofStringType(nameProp)
                                .label("Full Name")
                                .placeholder("Enter full name")
                                .validate(StringLengthValidator.atLeast(2, "Name must be at least 2 characters long.")),

                        Field.ofStringType(phoneProp)
                                .label("Phone Number")
                                .placeholder("e.g. 09123456789")
                                .validate(StringLengthValidator.atLeast(5, "Please enter a valid operational phone number.")),

                        Field.ofStringType(emailProp)
                                .label("Email Address")
                                .placeholder("customer@example.com")
                                .required(false),

                        Field.ofStringType(addressProp)
                                .label("Home Address")
                                .placeholder("Street, City, Zip Code")
                                .multiline(true)
                                .required(false)
                )
        );
    }

    @Override
    protected String getEditHeaderTitle() {
        return "Edit Customer";
    }

    @Override
    protected String getCreateHeaderTitle() {
        return "Register New Customer";
    }

    @Override
    protected String getEditButtonText() {
        return "Update Customer";
    }

    @Override
    protected String getCreateButtonText() {
        return "Save Customer";
    }

    @Override
    protected String getTargetFxmlName() {
        return "customers-view.fxml";
    }

    @Override
    protected String getTargetViewTitle() {
        return "Customers Directory";
    }

    @Override
    protected void executeSave() {
        String cleanEmail = emailProp.get() == null ? "" : emailProp.get().trim();
        String cleanAddress = addressProp.get() == null ? "" : addressProp.get().trim();

        if (isEditMode) {
            selectedEntity.setName(nameProp.get().trim());
            selectedEntity.setPhone(phoneProp.get().trim());
            selectedEntity.setEmail(cleanEmail);
            selectedEntity.setAddress(cleanAddress);

            if (CustomerManager.update(selectedEntity)) {
                showToastSuccess("Success", "Customer profile adjusted successfully.");
                closeWindow();
            } else {
                showToastError("Database Error", "Unable to commit tracking updates.");
            }
        } else {
            Customer newCustomer = new Customer(0, nameProp.get().trim(), phoneProp.get().trim(), cleanEmail, cleanAddress);
            if (CustomerManager.create(newCustomer)) {
                showToastSuccess("Success", "New customer profile saved successfully.");
                closeWindow();
            } else {
                showToastError("Database Error", "Failed to compile customer profiles.");
            }
        }
    }

    @Override
    protected void executeFormDeletion() {
        if (selectedEntity != null && CustomerManager.delete(selectedEntity.getId())) {
            showToastSuccess("Success", "Customer file was scrubbed cleanly.");
            closeWindow();
        } else {
            showToastError("Database Error", "Unable to remove target customer.");
        }
    }
}
