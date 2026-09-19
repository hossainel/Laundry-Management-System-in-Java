package com.lms.laundry.controller;

import com.lms.laundry.manager.Session;
import com.lms.laundry.manager.UserManager;
import com.lms.laundry.model.User;
import com.lms.laundry.view.LaundryApplication;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {
    @FXML
    private Label errorLabel;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private CheckBox rememberCheckBox;
    @FXML
    private Button loginButton;

    @FXML
    protected void onErrorLabelClick() {
        errorLabel.setText("");
    }

    @FXML
    protected void onLoginButtonClick() {
        loginButton.setDisable(true);
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        // Validation
        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Username and Password required!");
            loginButton.setDisable(false);
            return;
        }
        try {
            User user = UserManager.login(username, password);
            if (user != null) {
                // SUCCESS UI
                errorLabel.getStyleClass().removeAll("error-label");
                errorLabel.getStyleClass().add("success-label");
                errorLabel.setText("Login Successful!");
                // Remember me
                if (rememberCheckBox.isSelected()) {
                    Session.saveLogin(user.getName(), user.getUsername(), user.getRole().name());
                } else {
                    Session.clearSession();
                }
                Session.setCurrentUser(user);
                // Load dashboard
                LaundryApplication.loadScene("main-view.fxml", "Dashboard");
            } else {
                errorLabel.setText("Invalid Username or Password!");
            }
        } catch (Exception e) {
            errorLabel.setText("Login Failed!");
        }
        loginButton.setDisable(false);
    }
}
