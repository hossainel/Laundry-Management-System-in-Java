package com.lms.laundry.controller;

import com.lms.laundry.manager.ImageAssetManager;
import com.lms.laundry.manager.ServiceManager;
import com.lms.laundry.model.Service;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.io.File;
import java.net.URL;
import java.util.List;

public class ServiceListController extends BaseListController<Service> {

    @FXML
    private TableView<Service> tblServices;
    @FXML
    private TableColumn<Service, Integer> colId;
    @FXML
    private TableColumn<Service, String> colImage;
    @FXML
    private TableColumn<Service, String> colName;
    @FXML
    private TableColumn<Service, String> colCategory;
    @FXML
    private TableColumn<Service, Integer> colSubTypesCount;
    @FXML
    private TableColumn<Service, Boolean> colStatus;

    @FXML
    public void initialize() {
        this.tblData = tblServices;
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("categoryName"));

        colSubTypesCount.setCellValueFactory(cellData -> {
            int totalTypes = 0;
            if (cellData.getValue().getTypeIds() != null) {
                totalTypes = cellData.getValue().getTypeIds().size();
            }
            return new SimpleIntegerProperty(totalTypes).asObject();
        });

        setupImageColumnCells();

        colId.getStyleClass().add("col-center");
        colImage.getStyleClass().add("col-center");
        colSubTypesCount.getStyleClass().add("col-center");

        if (colStatus != null) {
            colStatus.getStyleClass().add("col-center");
            addStatusToggleButtons();
        }
        setupTableAndFiltering();
    }

    /**
     * Configures the image column cells to load service variations images.
     * Automatically falls back to the local "no-image.png" asset if empty.
     */
    private void setupImageColumnCells() {
        colImage.setCellValueFactory(new PropertyValueFactory<>("image"));
        colImage.setCellFactory(param -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            private final HBox container = new HBox(imageView);

            {
                container.setAlignment(Pos.CENTER);
                imageView.setFitWidth(44);
                imageView.setFitHeight(44);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(String imageName, boolean empty) {
                super.updateItem(imageName, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    try {
                        String resourcePath = ImageAssetManager.getImage(imageName);
                        URL resourceUrl = getClass().getResource(resourcePath);

                        if (resourceUrl != null) {
                            imageView.setImage(new Image(resourceUrl.toExternalForm(), true));
                        } else {
                            File rawFile = new File("src/main/resources" + resourcePath);
                            if (rawFile.exists()) {
                                imageView.setImage(new Image(rawFile.toURI().toString(), true));
                            } else {
                                imageView.setImage(null);
                            }
                        }
                    } catch (Exception e) {
                        imageView.setImage(null);
                    }
                    setGraphic(container);
                }
            }
        });
    }

    private void addStatusToggleButtons() {
        colStatus.setCellValueFactory(new PropertyValueFactory<>("active"));
        colStatus.setCellFactory(param -> new TableCell<>() {
            private final Button btnToggle = new Button();
            private final HBox container = new HBox(btnToggle);
            {
                container.setAlignment(Pos.CENTER);
                btnToggle.getStyleClass().add("status-toggle-btn");
                btnToggle.setOnAction(event -> {
                    Service service = getTableView().getItems().get(getIndex());
                    boolean success = service.isActive()
                            ? ServiceManager.disable(service.getId())
                            : ServiceManager.enable(service.getId());
                    if (success) {
                        refreshTable();
                    } else {
                        showTemporaryError("Database error: Could not change service operational status.");
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
                        btnToggle.getStyleClass().add("btn-active");
                    } else {
                        btnToggle.setText("Inactive");
                        btnToggle.getStyleClass().add("btn-inactive");
                    }
                    setGraphic(container);
                }
            }
        });
    }

    @Override
    protected boolean matchesSearch(Service service, String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) return true;
        String filter = searchTerm.toLowerCase().trim();

        boolean matchesName = service.getName() != null && service.getName().toLowerCase().contains(filter);
        boolean matchesCategory = service.getCategoryName() != null && service.getCategoryName().toLowerCase().contains(filter);

        return matchesName || matchesCategory;
    }

    @Override
    protected List<Service> loadEntities() {
        return ServiceManager.getAll();
    }

    @Override
    protected String getEntityContextName() {
        return "Services Ledger";
    }

    @Override
    protected void handleView(Service service) {
        ServiceController controller = MainController.navigate("service-view.fxml", "Edit Service Record");
        if (controller != null) {
            controller.setEntity(service);
        }
    }

    @Override
    protected boolean canDeleteEntity(Service service) {
        return true;
    }

    @Override
    protected void executeDeletion(Service service) {
        if (ServiceManager.delete(service.getId())) {
            refreshTable();
        } else {
            showTemporaryError("Database error: Could not complete service deletion.");
        }
    }

    @FXML
    public void addService(ActionEvent actionEvent) {
        ServiceController controller = MainController.navigate("service-view.fxml", "Log New Service");
        if (controller != null) {
            controller.setEntity(null);
        }
    }
}
