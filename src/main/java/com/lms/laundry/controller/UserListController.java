package com.lms.laundry.controller;

import com.lms.laundry.manager.UserManager;
import com.lms.laundry.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
//import org.kordamp.ikonli.javafx.FontIcon;
import java.util.List;

public class UserListController extends BaseListController<User> {

    @FXML
    private TableView<User> tblUsers;
    @FXML
    private TableColumn<User, Integer> colId;
    @FXML
    private TableColumn<User, String> colName;
    @FXML
    private TableColumn<User, String> colUsername;
    @FXML
    private TableColumn<User, User.Role> colRole;
    @FXML
    private TableColumn<User, Boolean> colStatus;

    @FXML
    public void initialize() {
        this.tblData = tblUsers;
        // Map column
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        colId.getStyleClass().add("col-center");
        colStatus.getStyleClass().add("col-center");
        addStatusToggleButtons();
        setupTableAndFiltering();
    }

    @Override
    protected boolean matchesSearch(User user, String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) return true;
        String filter = searchTerm.toLowerCase().trim();
        return user.getName().toLowerCase().contains(filter) ||
                user.getUsername().toLowerCase().contains(filter);
    }

    @Override
    protected List<User> loadEntities() {
        return UserManager.getAll();
    }

    @Override
    protected String getEntityContextName() {
        return "Users";
    }

    @Override
    protected void handleView(User user) {
        UserController controller = MainController.navigate("user-view.fxml", "Edit User");
        if (controller != null) {
            controller.setEntity(user);
        }
    }

    @Override
    protected boolean canDeleteEntity(User user) {
        if (user.getId() == 1) {
            showTemporaryError("System protection: Cannot delete primary Admin account.");
            return false;
        }
        return true;
    }

    @Override
    protected void executeDeletion(User user) {
        if (UserManager.delete(user.getId())) {
            refreshTable();
        } else {
            showTemporaryError("Database error: Could not complete user deletion.");
        }
    }

    /**
     * Renders explicit operational Active/Inactive state buttons inside the table tracking list.
     */
    private void addStatusToggleButtons() {
        colStatus.setCellValueFactory(new PropertyValueFactory<>("active"));
        colStatus.setCellFactory(param -> new TableCell<>() {
            private final Button btnToggle = new Button();
            private final HBox container = new HBox(btnToggle);

            {
                container.setAlignment(Pos.CENTER);
                btnToggle.getStyleClass().add("status-toggle-btn");
                btnToggle.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    if (user.getId() == 1 && user.isActive()) {
                        showTemporaryError("System protection: Cannot disable the primary Admin account.");
                        return;
                    }
                    boolean success = user.isActive() ? UserManager.disable(user.getId()) : UserManager.enable(user.getId());
                    if (success) {
                        refreshTable();
                    } else {
                        showTemporaryError("Database error: Could not change account status.");
                    }
                });
            }

            @Override
            protected void updateItem(Boolean isActive, boolean empty) {
                super.updateItem(isActive, empty);
                if (empty || isActive == null) {
                    setGraphic(null);
                } else {
                    btnToggle.getStyleClass().removeAll("btn-active", "btn-inactive");
                    if (isActive) {
                        btnToggle.setText("Active");
                        //btnToggle.setGraphic(new FontIcon("medal-check"));
                        btnToggle.getStyleClass().add("btn-active");
                    } else {
                        btnToggle.setText("Inactive");
                        //btnToggle.setGraphic(new FontIcon("modal-block"));
                        btnToggle.getStyleClass().add("btn-inactive");
                    }
                    setGraphic(container);
                }
            }
        });
    }

    @FXML
    public void addUser(ActionEvent actionEvent) {
        UserController controller = MainController.navigate("user-view.fxml", "Register New User");
        if (controller != null) {
            controller.setEntity(null);
        }
    }
}
