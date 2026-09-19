package com.lms.laundry.controller;

import com.lms.laundry.manager.ExpenseCategoryManager;
import com.lms.laundry.model.ExpenseCategory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.util.List;

public class ExpenseCategoryListController extends BaseListController<ExpenseCategory> {
    @FXML
    private TableView<ExpenseCategory> tblExpenseCategories;
    @FXML
    private TableColumn<ExpenseCategory, Integer> colId;
    @FXML
    private TableColumn<ExpenseCategory, String> colName;
    @FXML
    private TableColumn<ExpenseCategory, Boolean> colStatus;

    @FXML
    public void initialize() {
        this.tblData = tblExpenseCategories;
        // Map column
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colId.getStyleClass().add("col-center");
        colStatus.getStyleClass().add("col-center");
        addStatusToggleButtons();
        setupTableAndFiltering();
    }

    @Override
    protected boolean matchesSearch(ExpenseCategory expenseCategory, String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) return true;
        String filter = searchTerm.toLowerCase().trim();
        return expenseCategory.getName().toLowerCase().contains(filter);
    }

    @Override
    protected List<ExpenseCategory> loadEntities() {
        return ExpenseCategoryManager.getAll();
    }

    @Override
    protected String getEntityContextName() {
        return "Expense Categories";
    }

    @Override
    protected void handleView(ExpenseCategory expenseCategory) {
        ExpenseCategoryController controller = MainController.navigate("expense-category-view.fxml", "Edit Expense Category");
        if (controller != null) {
            controller.setEntity(expenseCategory);
        }
    }

    @Override
    protected boolean canDeleteEntity(ExpenseCategory expenseCategory) {
        return true;
    }

    @Override
    protected void executeDeletion(ExpenseCategory expenseCategory) {
        if (ExpenseCategoryManager.delete(expenseCategory.getId())) {
            refreshTable();
        } else {
            showTemporaryError("Database error: Could not complete expenseCategory deletion.");
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
                    ExpenseCategory expenseCategory = getTableView().getItems().get(getIndex());
                    boolean success = expenseCategory.isActive() ? ExpenseCategoryManager.disable(expenseCategory.getId()) : ExpenseCategoryManager.enable(expenseCategory.getId());
                    if (success) {
                        refreshTable();
                    } else {
                        showTemporaryError("Database error: Could not change category status.");
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
    public void addExpenseCategory(ActionEvent actionEvent) {
        ExpenseCategoryController controller = MainController.navigate("expense-category-view.fxml", "Add New Expense Category");
        if (controller != null) {
            controller.setEntity(null);
        }
    }
}
