package com.lms.laundry.controller;

import com.lms.laundry.manager.ServiceCategoryManager;
import com.lms.laundry.model.ServiceCategory;
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

public class ServiceCategoryListController extends BaseListController<ServiceCategory> {
    @FXML
    private TableView<ServiceCategory> tblServiceCategories;
    @FXML
    private TableColumn<ServiceCategory, Integer> colId;
    @FXML
    private TableColumn<ServiceCategory, String> colName;
    @FXML
    private TableColumn<ServiceCategory, Boolean> colStatus;

    @FXML
    public void initialize() {
        this.tblData = tblServiceCategories;
        // Map column
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colId.getStyleClass().add("col-center");
        colStatus.getStyleClass().add("col-center");
        addStatusToggleButtons();
        setupTableAndFiltering();
    }

    @Override
    protected boolean matchesSearch(ServiceCategory serviceCategory, String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) return true;
        String filter = searchTerm.toLowerCase().trim();
        return serviceCategory.getName().toLowerCase().contains(filter);
    }

    @Override
    protected List<ServiceCategory> loadEntities() {
        return ServiceCategoryManager.getAll();
    }

    @Override
    protected String getEntityContextName() {
        return "Service Categories";
    }

    @Override
    protected void handleView(ServiceCategory serviceCategory) {
        ServiceCategoryController controller = MainController.navigate("service-category-view.fxml", "Edit ServiceCategory");
        if (controller != null) {
            controller.setEntity(serviceCategory);
        }
    }

    @Override
    protected boolean canDeleteEntity(ServiceCategory serviceCategory) {
        return true;
    }

    @Override
    protected void executeDeletion(ServiceCategory serviceCategory) {
        if (ServiceCategoryManager.delete(serviceCategory.getId())) {
            refreshTable();
        } else {
            showTemporaryError("Database error: Could not complete serviceCategory deletion.");
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
                    ServiceCategory serviceCategory = getTableView().getItems().get(getIndex());
                    boolean success = serviceCategory.isActive() ? ServiceCategoryManager.disable(serviceCategory.getId()) : ServiceCategoryManager.enable(serviceCategory.getId());
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
    public void addServiceCategory(ActionEvent actionEvent) {
        ServiceCategoryController controller = MainController.navigate("service-category-view.fxml", "Add New Service Category");
        if (controller != null) {
            controller.setEntity(null);
        }
    }
}
