package com.lms.laundry.controller;

import com.lms.laundry.manager.ExpenseManager;
import com.lms.laundry.manager.IconManager;
import com.lms.laundry.model.Expense;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import java.time.LocalDate;
import java.util.List;

public class ExpenseListController extends BaseListController<Expense> {

    @FXML
    private TableView<Expense> tblExpenses;
    @FXML
    private TableColumn<Expense, Integer> colId;
    @FXML
    private TableColumn<Expense, String> colTitle;
    @FXML
    private TableColumn<Expense, String> colCategory;
    @FXML
    private TableColumn<Expense, Double> colAmount;
    @FXML
    private TableColumn<Expense, LocalDate> colDate;
    @FXML
    private TableColumn<Expense, String> colMethod;
    @FXML
    private TableColumn<Expense, String> colNote;
    @FXML
    private TableColumn<Expense, Void> colActions;

    @FXML
    public void initialize() {
        this.tblData = tblExpenses;
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colMethod.setCellValueFactory(new PropertyValueFactory<>("paymentMethodName"));
        colNote.setCellValueFactory(new PropertyValueFactory<>("note"));
        colId.getStyleClass().add("col-center");
        colAmount.getStyleClass().add("col-right");
        colDate.getStyleClass().add("col-center");
        if (colActions != null) { colActions.getStyleClass().add("col-center"); }
        addActionButtonsToTable();
        setupTableAndFiltering();
    }
    /**
     * Appends an inline visual action row mapping HBox container housing
     * explicit 'Edit' and 'Delete' triggers directly into the TableView framework.
     */
    private void addActionButtonsToTable() {
        if (colActions == null) return;
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("Edit");
            private final Button btnDelete = new Button("Delete");
            private final HBox container = new HBox(8, btnEdit, btnDelete); // 8px spacing between layout items
            {
                container.setAlignment(Pos.CENTER);
                btnEdit.getStyleClass().addAll("btn-action", "btn-edit-action");
                btnDelete.getStyleClass().addAll("btn-action", "btn-delete-action");
                IconManager.decorateButton(btnEdit);
                IconManager.decorateButton(btnDelete);
                btnEdit.setOnAction(event -> {
                    Expense expense = getTableView().getItems().get(getIndex());
                    handleView(expense);
                });
                btnDelete.setOnAction(event -> {
                    Expense expense = getTableView().getItems().get(getIndex());
                    if (canDeleteEntity(expense)) {
                        executeDeletion(expense);
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        });
    }
    @Override
    protected boolean matchesSearch(Expense expense, String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) return true;
        String filter = searchTerm.toLowerCase().trim();
        boolean matchesTitle = expense.getTitle() != null && expense.getTitle().toLowerCase().contains(filter);
        boolean matchesCategory = expense.getCategoryName() != null && expense.getCategoryName().toLowerCase().contains(filter);
        boolean matchesNote = expense.getNote() != null && expense.getNote().toLowerCase().contains(filter);
        return matchesTitle || matchesCategory || matchesNote;
    }
    @Override
    protected List<Expense> loadEntities() { return ExpenseManager.getAll(); }
    @Override
    protected String getEntityContextName() { return "Expenses Ledger"; }
    @Override
    protected void handleView(Expense expense) {
        ExpenseController controller = MainController.navigate("expense-view.fxml", "Edit Expense Record");
        if (controller != null) { controller.setEntity(expense); }
    }
    @Override
    protected boolean canDeleteEntity(Expense expense) { return true; }
    @Override
    protected void executeDeletion(Expense expense) {
        if (ExpenseManager.delete(expense.getId())) {
            refreshTable();
        } else {
            showTemporaryError("Database error: Could not complete expense deletion.");
        }
    }

    @FXML
    public void addExpense(ActionEvent actionEvent) {
        ExpenseController controller = MainController.navigate("expense-view.fxml", "Log New Expense");
        if (controller != null) { controller.setEntity(null); }
    }
}
