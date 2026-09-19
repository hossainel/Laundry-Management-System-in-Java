package com.lms.laundry.controller;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.Form;
import com.dlsc.formsfx.model.structure.Group;
import com.dlsc.formsfx.model.validators.StringLengthValidator;
import com.lms.laundry.manager.ServiceTypeManager;
import com.lms.laundry.model.ServiceType;
import javafx.beans.property.*;

public class ServiceTypeController extends BaseFormController<ServiceType> {

    private final StringProperty nameProp = new SimpleStringProperty("");
    private final BooleanProperty activeProp = new SimpleBooleanProperty(true);

    @Override
    protected void clearFormFields() {
        nameProp.set("");
        activeProp.set(true);
    }

    @Override
    protected Form initFormStructure(ServiceType serviceType) {
        if (isEditMode && serviceType != null) {
            nameProp.set(serviceType.getName());
            activeProp.set(serviceType.isActive());
        }

        return Form.of(
                Group.of(
                        Field.ofStringType(nameProp).label("Full Name")
                                .placeholder("Enter full name")
                                .validate(StringLengthValidator.atLeast(2, "Name must be at least 2 characters long.")),
                        Field.ofBooleanType(activeProp).label("Type Status Active")
                )
        );
    }

    @Override
    protected String getEditHeaderTitle() {
        return "Edit Service Type";
    }

    @Override
    protected String getCreateHeaderTitle() {
        return "Add New Service Type";
    }

    @Override
    protected String getEditButtonText() {
        return "Update Service Type";
    }

    @Override
    protected String getCreateButtonText() {
        return "Save Service Type";
    }

    @Override
    protected String getTargetFxmlName() {
        return "service-types-view.fxml";
    }

    @Override
    protected String getTargetViewTitle() {
        return "Service Types";
    }

    @Override
    protected void executeSave() {
        if (isEditMode) {
            selectedEntity.setName(nameProp.get());
            selectedEntity.setActive(activeProp.get());
            if (ServiceTypeManager.update(selectedEntity)) {
                showToastSuccess("Success", "Service type adjusted successfully.");
                closeWindow();
            } else {
                showToastError("Database Error", "Unable to commit tracking updates.");
            }
        } else {
            ServiceType newServiceType = new ServiceType(0, nameProp.get(), activeProp.get());
            if (ServiceTypeManager.create(newServiceType)) {
                showToastSuccess("Success", "New service type saved successfully.");
                closeWindow();
            } else {
                showToastError("Database Error", "Failed to compile background configurations.");
            }
        }
    }

    @Override
    protected void executeFormDeletion() {
        if (selectedEntity != null && ServiceTypeManager.delete(selectedEntity.getId())) {
            showToastSuccess("Success", "Service type has been completely removed.");
            closeWindow();
        } else {
            showToastError("Database Error", "Unable to safely drop selected type.");
        }
    }
}
