package com.lms.laundry.controller;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.Form;
import com.dlsc.formsfx.model.structure.Group;
import com.dlsc.formsfx.model.validators.StringLengthValidator;
import com.lms.laundry.manager.ProductCategoryManager;
import com.lms.laundry.model.ProductCategory;
import javafx.beans.property.*;

public class ProductCategoryController extends BaseFormController<ProductCategory> {

    private final StringProperty nameProp = new SimpleStringProperty("");
    private final BooleanProperty activeProp = new SimpleBooleanProperty(true);

    @Override
    protected void clearFormFields() {
        nameProp.set("");
        activeProp.set(true);
    }

    @Override
    protected Form initFormStructure(ProductCategory productCategory) {
        if (isEditMode && productCategory != null) {
            nameProp.set(productCategory.getName());
            activeProp.set(productCategory.isActive());
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
        return "Edit Product Category";
    }

    @Override
    protected String getCreateHeaderTitle() {
        return "Add New Product Category";
    }

    @Override
    protected String getEditButtonText() {
        return "Update Product Category";
    }

    @Override
    protected String getCreateButtonText() {
        return "Save Product Category";
    }

    @Override
    protected String getTargetFxmlName() {
        return "product-categories-view.fxml";
    }

    @Override
    protected String getTargetViewTitle() {
        return "Product Categories";
    }

    @Override
    protected void executeSave() {
        if (isEditMode) {
            selectedEntity.setName(nameProp.get());
            selectedEntity.setActive(activeProp.get());
            if (ProductCategoryManager.update(selectedEntity)) {
                showToastSuccess("Success", "Product category adjusted successfully.");
                closeWindow();
            } else {
                showToastError("Database Error", "Unable to commit tracking updates.");
            }
        } else {
            ProductCategory newProductCategory = new ProductCategory(0, nameProp.get(), activeProp.get());
            if (ProductCategoryManager.create(newProductCategory)) {
                showToastSuccess("Success", "New product category saved successfully.");
                closeWindow();
            } else {
                showToastError("Database Error", "Failed to compile background configurations.");
            }
        }
    }

    @Override
    protected void executeFormDeletion() {
        if (selectedEntity != null && ProductCategoryManager.delete(selectedEntity.getId())) {
            showToastSuccess("Success", "Product category has been completely removed.");
            closeWindow();
        } else {
            showToastError("Database Error", "Unable to safely drop selected category.");
        }
    }
}
