package com.lms.laundry.controller;

import com.dlsc.formsfx.model.structure.Form;
import com.dlsc.formsfx.view.renderer.FormRenderer;
import com.lms.laundry.manager.IconManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.controlsfx.control.Notifications;
//import org.kordamp.ikonli.javafx.FontIcon;
import net.synedra.validatorfx.Validator;

import java.util.Optional;

public abstract class BaseFormController<T> {
    protected final Validator manualValidator = new Validator();
    @FXML
    protected Label lblHeader;
    @FXML
    protected Button btnSave;
    @FXML
    protected Button btnDelete;
    @FXML
    protected VBox formContainer;
    protected T selectedEntity;
    protected boolean isEditMode = false;
    protected Form formStructure;

    public void setEntity(T entity) {
        this.selectedEntity = entity;
        this.isEditMode = (entity != null);
        clearFormFields();
        this.formStructure = initFormStructure(entity);
        FormRenderer renderer = new FormRenderer(formStructure);
        if (formContainer != null) {
            formContainer.getChildren().clear();
            formContainer.getChildren().add(renderer);
            formContainer.requestLayout();
            if (formContainer.getParent() != null) {
                formContainer.getParent().requestLayout();
            }
        }

        // Save Button Setup
        if (btnSave != null) {
            btnSave.setText(isEditMode ? getEditButtonText() : getCreateButtonText());
            IconManager.decorateButton(btnSave);
            //btnSave.setGraphic(new FontIcon(isEditMode ? "fa-save" : "fa-plus-circle"));
        }

        // Delete Button Setup (Only visible and active when editing)
        if (btnDelete != null) {
            btnDelete.setVisible(isEditMode);
            btnDelete.setManaged(isEditMode);
            IconManager.decorateButton(btnDelete);
            //btnDelete.setGraphic(new FontIcon("fa-trash"));
            btnDelete.setOnAction(event -> handleFormDeletion());
        }

        if (lblHeader != null) {
            lblHeader.setText(isEditMode ? getEditHeaderTitle() : getCreateHeaderTitle());
        }

        setupManualValidators();
    }

    /**
     * Optional life-cycle hook for implementing custom validation strategies using ValidatorFX.
     * Concrete subclasses can override this method to stitch manual cross-field constraint logic.
     */
    protected void setupManualValidators() {
        // Left open for optional subclass implementation overrides
    }

    protected abstract Form initFormStructure(T entity);

    protected abstract void clearFormFields();

    protected abstract String getEditHeaderTitle();

    protected abstract String getCreateHeaderTitle();

    protected abstract String getEditButtonText();

    protected abstract String getCreateButtonText();

    protected abstract String getTargetFxmlName();

    protected abstract String getTargetViewTitle();

    protected abstract void executeSave();

    protected abstract void executeFormDeletion();

    @FXML
    protected void handleSave() {
        if (formStructure == null) return;
        formStructure.persist();
        boolean isFormsFxValid = formStructure.isValid();
        boolean isValidatorFxValid = manualValidator.validate();
        if (isFormsFxValid && isValidatorFxValid) {
            executeSave();
        } else {
            showToastError(
                    "Validation Error",
                    "Please correct the invalid fields highlighted below."
            );
        }
    }

    @FXML
    protected void handleCancel() {
        if (formStructure != null && formStructure.hasChanged()) {
            boolean discardChanges = showConfirmationAlert(
                    "Unsaved Changes",
                    "You have modified fields on this form. Are you sure you want to discard your changes and exit?"
            );
            if (!discardChanges) return;
        }
        if (formStructure != null) formStructure.reset();
        closeWindow();
    }

    protected void handleFormDeletion() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText("Permanently remove this record?");
        alert.setContentText("This processing cannot be rolled back safely.");
        //alert.setGraphic(new FontIcon("fa-exclamation-triangle"));

        ButtonType btnYes = new ButtonType("Delete", ButtonBar.ButtonData.YES);
        ButtonType btnNo = new ButtonType("Cancel", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(btnYes, btnNo);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == btnYes) {
            executeFormDeletion();
        }
    }

    protected void closeWindow() {
        MainController.navigate(getTargetFxmlName(), getTargetViewTitle());
    }

    // --- CENTRALIZED UI UTILITIES WITH IKONLI VECTOR GRAPHICS ---
    protected void showToastSuccess(String title, String message) {
        Notifications.create().title(title).text(message)
                //.graphic(new FontIcon("fa-check-circle"))
                .position(Pos.TOP_RIGHT).darkStyle().showInformation();
    }

    protected void showToastError(String title, String message) {
        Notifications.create().title(title).text(message)
                //.graphic(new FontIcon("fa-exclamation-circle"))
                .position(Pos.TOP_RIGHT).darkStyle().showError();
    }

    protected boolean showConfirmationAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        //alert.setGraphic(new FontIcon("fa-exclamation-triangle"));
        ButtonType btnYes = new ButtonType("Discard Changes", ButtonBar.ButtonData.YES);
        ButtonType btnNo = new ButtonType("Keep Editing", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(btnYes, btnNo);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == btnYes;
    }
}
