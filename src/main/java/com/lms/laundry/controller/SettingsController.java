package com.lms.laundry.controller;

import com.lms.laundry.manager.SettingsManager;
import com.lms.laundry.manager.Session;
import com.lms.laundry.model.Settings;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.controlsfx.control.Notifications;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsController {

    private final Map<Settings, TextField> fieldBindingMap = new HashMap<>();
    @FXML
    private VBox settingsRowsContainer;

    @FXML
    public void initialize() {
        loadAndRenderSettings();
    }

    /**
     * Pulls settings values from database context, maps individual UI rows,
     * and positions them into the scrollable VBox view using HBox lines.
     */
    private void loadAndRenderSettings() {
        settingsRowsContainer.getChildren().clear();
        fieldBindingMap.clear();
        List<Settings> activeSettingsList = SettingsManager.getAll();
        if (activeSettingsList == null || activeSettingsList.isEmpty()) {
            Label lblEmpty = new Label("No system configuration keys found in the database.");
            lblEmpty.setStyle("-fx-font-style: italic; -fx-text-fill: gray;");
            settingsRowsContainer.getChildren().add(lblEmpty);
            return;
        }

        for (Settings setting : activeSettingsList) {
            // HBox
            HBox rowLine = new HBox(15);
            rowLine.setAlignment(Pos.CENTER_LEFT);
            rowLine.setStyle("-fx-padding: 4 0 4 0;");
            String formattedLabel = formatKeyToLabel(setting.getKey());
            Label lblKey = new Label(formattedLabel);
            lblKey.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 13px;");
            lblKey.setMinWidth(100.0);
            lblKey.setPrefWidth(160.0);
            lblKey.setMaxWidth(200.0);
            TextField txtValue = new TextField();
            txtValue.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 15px;");
            txtValue.setText(setting.getValue());
            txtValue.setPrefHeight(35.0);
            javafx.scene.layout.HBox.setHgrow(txtValue, javafx.scene.layout.Priority.ALWAYS);
            if (setting.getDescription() != null && !setting.getDescription().isBlank()) {
                txtValue.setPromptText(setting.getDescription());
            } else {
                txtValue.setPromptText("Enter value for " + formattedLabel);
            }
            rowLine.getChildren().addAll(lblKey, txtValue);
            settingsRowsContainer.getChildren().add(rowLine);
            fieldBindingMap.put(setting, txtValue);
        }
    }

    /**
     * Helper method to convert raw database snake_case keys into readable titles.
     * e.g., "shop_name" -> "Shop Name" or "TAX_ID" -> "Tax ID"
     */
    private String formatKeyToLabel(String key) {
        if (key == null || key.isBlank()) return "";
        return key.replace("_", " ").toUpperCase().trim();
    }

    @FXML
    protected void handleBulkSave() {
        boolean allSucceeded = true;
        List<Settings> updatedListForSession = new ArrayList<>();
        for (Map.Entry<Settings, TextField> entry : fieldBindingMap.entrySet()) {
            Settings setting = entry.getKey();
            TextField inputField = entry.getValue();
            String newValue = inputField.getText() != null ? inputField.getText().trim() : "";
            setting.setValue(newValue);
            updatedListForSession.add(setting);
            if (!SettingsManager.update(setting)) {
                allSucceeded = false;
            }
        }
        if (allSucceeded) {
            Session.setSettings(updatedListForSession);
            showToastSuccess("Configuration Updated", "All system changes saved successfully.");
            navigateToDashboard();
        } else {
            showToastError("Write Error", "Some parameters failed to update inside background records.");
            loadAndRenderSettings(); // Refresh UI values
        }
    }

    @FXML
    protected void handleCancel() {
        // Verification check
        boolean altered = fieldBindingMap.entrySet().stream()
                .anyMatch(entry -> !entry.getValue().getText().trim().equals(
                        entry.getKey().getValue() != null ? entry.getKey().getValue() : ""
                ));
        if (altered) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Discard Updates");
            alert.setHeaderText(null);
            alert.setContentText("Unsaved configuration tweaks exist. Discard changes and return to Dashboard?");

            ButtonType btnYes = new ButtonType("Discard", ButtonBar.ButtonData.YES);
            ButtonType btnNo = new ButtonType("Keep Editing", ButtonBar.ButtonData.NO);
            alert.getButtonTypes().setAll(btnYes, btnNo);

            if (alert.showAndWait().orElse(btnNo) != btnYes) {
                return;
            }
        }
        navigateToDashboard();
    }

    /**
     * Changes application layout frame back to the main management dashboard panel
     */
    private void navigateToDashboard() {
        MainController.navigate("dashboard-view.fxml", "Dashboard");
    }

    private void showToastSuccess(String title, String message) {
        Notifications.create().title(title).text(message).position(Pos.TOP_RIGHT).darkStyle().showInformation();
    }

    private void showToastError(String title, String message) {
        Notifications.create().title(title).text(message).position(Pos.TOP_RIGHT).darkStyle().showError();
    }
}