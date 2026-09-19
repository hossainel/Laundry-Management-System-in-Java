package com.lms.laundry.controller;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.Form;
import com.dlsc.formsfx.model.structure.Group;
import com.dlsc.formsfx.model.validators.StringLengthValidator;
import com.lms.laundry.manager.UserManager;
import com.lms.laundry.model.User;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;

public class UserController extends BaseFormController<User> {

    private final StringProperty nameProp = new SimpleStringProperty("");
    private final StringProperty usernameProp = new SimpleStringProperty("");
    private final StringProperty passwordProp = new SimpleStringProperty("");

    private final ListProperty<User.Role> rolesList =
            new SimpleListProperty<>(FXCollections.observableArrayList(User.Role.values()));

    private final ObjectProperty<User.Role> roleProp =
            new SimpleObjectProperty<>(User.Role.STAFF);

    private final BooleanProperty activeProp = new SimpleBooleanProperty(true);

    @Override
    protected void clearFormFields() {
        nameProp.set("");
        usernameProp.set("");
        passwordProp.set("");
        roleProp.set(User.Role.STAFF);
        activeProp.set(true);
    }

    @Override
    protected Form initFormStructure(User user) {
        if (isEditMode && user != null) {
            nameProp.set(user.getName());
            usernameProp.set(user.getUsername());
            roleProp.set(user.getRole());
            activeProp.set(user.isActive());
            passwordProp.set("");
        }

        return Form.of(
                Group.of(
                        Field.ofStringType(nameProp)
                                .label("Full Name")
                                .placeholder("Enter full name")
                                .validate(StringLengthValidator.atLeast(2, "Name must be at least 2 characters long.")),

                        Field.ofStringType(usernameProp)
                                .label("Username")
                                .placeholder("Enter username")
                                .validate(StringLengthValidator.atLeast(3, "Username must be at least 3 characters long.")),

                        Field.ofStringType(passwordProp)
                                .label("Password")
                                .placeholder(isEditMode ? "Leave blank to keep current password" : "Enter account password")
                                .required(!isEditMode),

                        Field.ofSingleSelectionType(rolesList, roleProp)
                                .label("Account Role"),

                        Field.ofBooleanType(activeProp)
                                .label("Account Status Active")
                )
        );
    }

    @Override
    protected String getEditHeaderTitle() {
        return "Edit System User";
    }

    @Override
    protected String getCreateHeaderTitle() {
        return "Register New User";
    }

    @Override
    protected String getEditButtonText() {
        return "Update Profile";
    }

    @Override
    protected String getCreateButtonText() {
        return "Save Registration";
    }

    @Override
    protected String getTargetFxmlName() {
        return "users-view.fxml";
    }

    @Override
    protected String getTargetViewTitle() {
        return "Users Directory";
    }

    @Override
    protected void executeSave() {
        User.Role selectedRole = roleProp.get();
        if (isEditMode) {
            selectedEntity.setName(nameProp.get());
            selectedEntity.setUsername(usernameProp.get());
            selectedEntity.setRole(selectedRole);
            selectedEntity.setActive(activeProp.get());
            if (UserManager.update(selectedEntity)) {
                if (passwordProp.get() != null && !passwordProp.get().trim().isEmpty()) {
                    UserManager.updatePassword(selectedEntity.getId(), passwordProp.get().trim());
                }
                User currentLoggedInUser = com.lms.laundry.manager.Session.getCurrentUser();
                if (currentLoggedInUser != null && currentLoggedInUser.getId() == selectedEntity.getId()) {
                    com.lms.laundry.manager.Session.setCurrentUser(selectedEntity);
                }
                showToastSuccess("Success", "User profile adjusted successfully.");
                closeWindow();
            } else {
                showToastError("Database Error", "Unable to commit tracking updates.");
            }
        } else {
            User newUser = new User(0, nameProp.get(), usernameProp.get(), passwordProp.get(), selectedRole, activeProp.get());
            if (UserManager.create(newUser)) {
                showToastSuccess("Success", "New profile saved successfully.");
                closeWindow();
            } else {
                showToastError("Database Error", "Failed to compile background configurations.");
            }
        }
    }

    @Override
    protected void executeFormDeletion() {
        if (selectedEntity != null && UserManager.delete(selectedEntity.getId())) {
            showToastSuccess("Success", "User profile has been completely removed.");
            closeWindow();
        } else {
            showToastError("Database Error", "Unable to safely drop selected account.");
        }
    }
}
