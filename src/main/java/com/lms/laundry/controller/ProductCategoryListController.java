package com.lms.laundry.controller;

import com.lms.laundry.manager.ProductCategoryManager;
import com.lms.laundry.model.ProductCategory;
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

public class ProductCategoryListController extends BaseListController<ProductCategory> {
    @FXML
    private TableView<ProductCategory> tblProductCategories;
    @FXML
    private TableColumn<ProductCategory, Integer> colId;
    @FXML
    private TableColumn<ProductCategory, String> colName;
    @FXML
    private TableColumn<ProductCategory, Boolean> colStatus;

    @FXML
    public void initialize() {
        this.tblData = tblProductCategories;
        // Map column
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colId.getStyleClass().add("col-center");
        colStatus.getStyleClass().add("col-center");
        addStatusToggleButtons();
        setupTableAndFiltering();
    }

    @Override
    protected boolean matchesSearch(ProductCategory productCategory, String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) return true;
        String filter = searchTerm.toLowerCase().trim();
        return productCategory.getName().toLowerCase().contains(filter);
    }

    @Override
    protected List<ProductCategory> loadEntities() {
        return ProductCategoryManager.getAll();
    }

    @Override
    protected String getEntityContextName() {
        return "Product Categories";
    }

    @Override
    protected void handleView(ProductCategory productCategory) {
        ProductCategoryController controller = MainController.navigate("product-category-view.fxml", "Edit Product Category");
        if (controller != null) {
            controller.setEntity(productCategory);
        }
    }

    @Override
    protected boolean canDeleteEntity(ProductCategory productCategory) {
        return true;
    }

    @Override
    protected void executeDeletion(ProductCategory productCategory) {
        if (ProductCategoryManager.delete(productCategory.getId())) {
            refreshTable();
        } else {
            showTemporaryError("Database error: Could not complete productCategory deletion.");
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
                    ProductCategory productCategory = getTableView().getItems().get(getIndex());
                    boolean success = productCategory.isActive() ? ProductCategoryManager.disable(productCategory.getId()) : ProductCategoryManager.enable(productCategory.getId());
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
    public void addProductCategory(ActionEvent actionEvent) {
        ProductCategoryController controller = MainController.navigate("product-category-view.fxml", "Add New Product Category");
        if (controller != null) {
            controller.setEntity(null);
        }
    }
}
