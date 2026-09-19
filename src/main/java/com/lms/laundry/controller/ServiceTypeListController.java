package com.lms.laundry.controller;

import com.lms.laundry.manager.ServiceTypeManager;
import com.lms.laundry.model.ServiceType;
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

public class ServiceTypeListController extends BaseListController<ServiceType> {
    @FXML
    private TableView<ServiceType> tblServiceTypes;
    @FXML
    private TableColumn<ServiceType, Integer> colId;
    @FXML
    private TableColumn<ServiceType, String> colName;
    @FXML
    private TableColumn<ServiceType, Boolean> colStatus;

    @FXML
    public void initialize() {
        this.tblData = tblServiceTypes;
        // Map column
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colId.getStyleClass().add("col-center");
        colStatus.getStyleClass().add("col-center");
        addStatusToggleButtons();
        setupTableAndFiltering();
    }

    @Override
    protected boolean matchesSearch(ServiceType serviceType, String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) return true;
        String filter = searchTerm.toLowerCase().trim();
        return serviceType.getName().toLowerCase().contains(filter);
    }

    @Override
    protected List<ServiceType> loadEntities() {
        return ServiceTypeManager.getAll();
    }

    @Override
    protected String getEntityContextName() {
        return "Service Types";
    }

    @Override
    protected void handleView(ServiceType serviceType) {
        ServiceTypeController controller = MainController.navigate("service-type-view.fxml", "Edit Service Type");
        if (controller != null) {
            controller.setEntity(serviceType);
        }
    }

    @Override
    protected boolean canDeleteEntity(ServiceType serviceType) {
        return true;
    }

    @Override
    protected void executeDeletion(ServiceType serviceType) {
        if (ServiceTypeManager.delete(serviceType.getId())) {
            refreshTable();
        } else {
            showTemporaryError("Database error: Could not complete serviceType deletion.");
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
                    ServiceType serviceType = getTableView().getItems().get(getIndex());
                    boolean success = serviceType.isActive() ? ServiceTypeManager.disable(serviceType.getId()) : ServiceTypeManager.enable(serviceType.getId());
                    if (success) {
                        refreshTable();
                    } else {
                        showTemporaryError("Database error: Could not change type status.");
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
    public void addServiceType(ActionEvent actionEvent) {
        ServiceTypeController controller = MainController.navigate("service-type-view.fxml", "Add New Service Type");
        if (controller != null) {
            controller.setEntity(null);
        }
    }
}
