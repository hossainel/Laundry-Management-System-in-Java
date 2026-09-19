package com.lms.laundry.controller;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.Form;
import com.dlsc.formsfx.model.structure.Group;
import com.dlsc.formsfx.model.validators.StringLengthValidator;
import com.lms.laundry.manager.ServiceCategoryManager;
import com.lms.laundry.model.ServiceCategory;
import javafx.beans.property.*;

public class ServiceCategoryController extends BaseFormController<ServiceCategory> {

    private final StringProperty nameProp = new SimpleStringProperty("");
    private final BooleanProperty activeProp = new SimpleBooleanProperty(true);

    @Override
    protected void clearFormFields() {
        nameProp.set("");
        activeProp.set(true);
    }

    @Override
    protected Form initFormStructure(ServiceCategory serviceCategory) {
        if (isEditMode && serviceCategory != null) {
            nameProp.set(serviceCategory.getName());
            activeProp.set(serviceCategory.isActive());
        }

        return Form.of(
                Group.of(
                        Field.ofStringType(nameProp).label("Full Name")
                                .placeholder("Enter full name")
                                .validate(StringLengthValidator.atLeast(2, "Name must be at least 2 characters long.")),
                        Field.ofBooleanType(activeProp).label("Category Status Active")
                )
        );
    }

    @Override
    protected String getEditHeaderTitle() {
        return "Edit Service Category";
    }

    @Override
    protected String getCreateHeaderTitle() {
        return "Add New Service Category";
    }

    @Override
    protected String getEditButtonText() {
        return "Update Service Category";
    }

    @Override
    protected String getCreateButtonText() {
        return "Save Service Category";
    }

    @Override
    protected String getTargetFxmlName() {
        return "service-categories-view.fxml";
    }

    @Override
    protected String getTargetViewTitle() {
        return "Service Categories";
    }

    @Override
    protected void executeSave() {
        if (isEditMode) {
            selectedEntity.setName(nameProp.get());
            selectedEntity.setActive(activeProp.get());
            if (ServiceCategoryManager.update(selectedEntity)) {
                showToastSuccess("Success", "Service category adjusted successfully.");
                closeWindow();
            } else {
                showToastError("Database Error", "Unable to commit tracking updates.");
            }
        } else {
            ServiceCategory newServiceCategory = new ServiceCategory(0, nameProp.get(), activeProp.get());
            if (ServiceCategoryManager.create(newServiceCategory)) {
                showToastSuccess("Success", "New service category saved successfully.");
                closeWindow();
            } else {
                showToastError("Database Error", "Failed to compile background configurations.");
            }
        }
    }

    @Override
    protected void executeFormDeletion() {
        if (selectedEntity != null && ServiceCategoryManager.delete(selectedEntity.getId())) {
            showToastSuccess("Success", "Service category has been completely removed.");
            closeWindow();
        } else {
            showToastError("Database Error", "Unable to safely drop selected category.");
        }
    }
}
