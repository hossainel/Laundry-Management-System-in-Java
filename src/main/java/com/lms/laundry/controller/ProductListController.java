package com.lms.laundry.controller;

import com.lms.laundry.manager.ImageAssetManager;
import com.lms.laundry.manager.ProductManager;
import com.lms.laundry.model.Product;
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

public class ProductListController extends BaseListController<Product> {

    @FXML
    private TableView<Product> tblProducts;
    @FXML
    private TableColumn<Product, Integer> colId;
    @FXML
    private TableColumn<Product, String> colImage;
    @FXML
    private TableColumn<Product, String> colName;
    @FXML
    private TableColumn<Product, String> colBarcode;
    @FXML
    private TableColumn<Product, String> colCategory;
    @FXML
    private TableColumn<Product, Double> colCostPrice;
    @FXML
    private TableColumn<Product, Double> colPrice;
    @FXML
    private TableColumn<Product, Integer> colStock;
    @FXML
    private TableColumn<Product, Boolean> colStatus;

    @FXML
    public void initialize() {
        this.tblData = tblProducts;

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colBarcode.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        colCostPrice.setCellValueFactory(new PropertyValueFactory<>("costPrice"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        setupImageColumnCells();

        colId.getStyleClass().add("col-center");
        colCostPrice.getStyleClass().add("col-right");
        colPrice.getStyleClass().add("col-right");
        colStock.getStyleClass().add("col-center");

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
                            // Fallback checks directory for target structural hot-swapping during execution
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
                    Product product = getTableView().getItems().get(getIndex());
                    boolean success = product.isActive()
                            ? ProductManager.disable(product.getId())
                            : ProductManager.enable(product.getId());
                    if (success) {
                        refreshTable();
                    } else {
                        showTemporaryError("Database error: Could not change product operational status.");
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
    protected boolean matchesSearch(Product product, String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) return true;
        String filter = searchTerm.toLowerCase().trim();

        boolean matchesName = product.getName() != null && product.getName().toLowerCase().contains(filter);
        boolean matchesBarcode = product.getBarcode() != null && product.getBarcode().toLowerCase().contains(filter);
        boolean matchesCategory = product.getCategoryName() != null && product.getCategoryName().toLowerCase().contains(filter);
        boolean matchesDesc = product.getDescription() != null && product.getDescription().toLowerCase().contains(filter);

        return matchesName || matchesBarcode || matchesCategory || matchesDesc;
    }

    @Override
    protected List<Product> loadEntities() {
        return ProductManager.getAll();
    }

    @Override
    protected String getEntityContextName() {
        return "Products Ledger";
    }

    @Override
    protected void handleView(Product product) {
        ProductController controller = MainController.navigate("product-view.fxml", "Edit Product Record");
        if (controller != null) {
            controller.setEntity(product);
        }
    }

    @Override
    protected boolean canDeleteEntity(Product product) {
        return true;
    }

    @Override
    protected void executeDeletion(Product product) {
        if (ProductManager.delete(product.getId())) {
            refreshTable();
        } else {
            showTemporaryError("Database error: Could not complete product deletion.");
        }
    }

    @FXML
    public void addProduct(ActionEvent actionEvent) {
        ProductController controller = MainController.navigate("product-view.fxml", "Log New Product");
        if (controller != null) {
            controller.setEntity(null);
        }
    }
}
