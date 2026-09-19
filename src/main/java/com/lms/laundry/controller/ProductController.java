package com.lms.laundry.controller;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.Form;
import com.dlsc.formsfx.model.structure.Group;
import com.dlsc.formsfx.model.validators.DoubleRangeValidator;
import com.dlsc.formsfx.model.validators.IntegerRangeValidator;
import com.dlsc.formsfx.model.validators.StringLengthValidator;
import com.lms.laundry.manager.ProductCategoryManager;
import com.lms.laundry.manager.ImageAssetManager;
import com.lms.laundry.manager.ProductManager;
import com.lms.laundry.model.Product;
import com.lms.laundry.model.ProductCategory;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

public class ProductController extends BaseFormController<Product> {
    private final StringProperty imageProp = new SimpleStringProperty("");
    private final StringProperty nameProp = new SimpleStringProperty("");
    private final StringProperty barcodeProp = new SimpleStringProperty("");
    private final StringProperty descriptionProp = new SimpleStringProperty("");
    private final DoubleProperty costPriceProp = new SimpleDoubleProperty(0.0);
    private final DoubleProperty priceProp = new SimpleDoubleProperty(0.0);
    private final IntegerProperty stockProp = new SimpleIntegerProperty(0);
    private final BooleanProperty isActive = new SimpleBooleanProperty(true);

    private final ObjectProperty<ProductCategory> categoryProp = new SimpleObjectProperty<>();

    private final ListProperty<ProductCategory> categoryOptions =
            new SimpleListProperty<>(FXCollections.observableArrayList());

    private final ProductCategory placeholderCategory = new ProductCategory(-1, "-- Select Category --", true);

    @FXML private ImageView imgPreview;
    @FXML private Label lblImagePath;
    @FXML private Button btnChooseImage;

    @FXML
    public void initialize() {
        loadDropdownDataFromDatabase();
        configureImageButtonIcon();
    }

    private void loadDropdownDataFromDatabase() {
        try {
            List<ProductCategory> categories = ProductCategoryManager.getAll();
            this.categoryOptions.clear();
            this.categoryOptions.add(placeholderCategory);
            if (categories != null) this.categoryOptions.addAll(categories);
        } catch (Exception e) {
            System.err.println("Failed to pre-populate form dropdown lists: " + e.getMessage());
        }
    }
    @Override
    protected void clearFormFields() {
        imageProp.set("");
        nameProp.set("");
        barcodeProp.set("");
        descriptionProp.set("");
        costPriceProp.set(0.0);
        priceProp.set(0.0);
        stockProp.set(0);
        isActive.set(true);
        categoryProp.set(placeholderCategory);
        updateImagePreview();
    }
    @Override
    protected Form initFormStructure(Product product) {
        if (isEditMode && product != null) {
            imageProp.set(product.getImage());
            nameProp.set(product.getName());
            barcodeProp.set(product.getBarcode());
            descriptionProp.set(product.getDescription());
            costPriceProp.set(product.getCostPrice());
            priceProp.set(product.getPrice());
            stockProp.set(product.getStock());
            isActive.set(product.isActive());
            categoryProp.set(
                    product.getProductCategory() != null
                            ? product.getProductCategory() : placeholderCategory
            );
        } else { clearFormFields(); }
        updateImagePreview();
        return Form.of(
                Group.of(
                        Field.ofStringType(nameProp)
                                .label("Name")
                                .placeholder("e.g., Detergent")
                                .validate(StringLengthValidator.atLeast(2, "Name must be at least 2 characters long.")),

                        Field.ofStringType(barcodeProp)
                                .label("Barcode")
                                .placeholder("e.g., 123456789")
                                .validate(StringLengthValidator.atLeast(2, "Barcode must be at least 2 characters long.")),

                        Field.ofSingleSelectionType(categoryOptions, categoryProp)
                                .label("Product Category"),

                        Field.ofDoubleType(costPriceProp)
                                .label("Cost Price")
                                .placeholder("0.00")
                                .validate(DoubleRangeValidator.atLeast(0.0, "Cost must be 0 or greater.")),

                        Field.ofDoubleType(priceProp)
                                .label("Retail Price")
                                .placeholder("0.00")
                                .validate(DoubleRangeValidator.atLeast(0.0, "Price must be 0 or greater.")),

                        Field.ofIntegerType(stockProp)
                                .label("Stock")
                                .placeholder("0")
                                .validate(IntegerRangeValidator.atLeast(0, "Stock must be 0 or greater.")),

                        Field.ofBooleanType(isActive)
                                .label("Is Active"),

                        Field.ofStringType(descriptionProp)
                                .label("Description")
                                .placeholder("Additional description details...")
                )
        );
    }

    @FXML
    private void handleChooseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Product Photo");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp")
        );
        File file = chooser.showOpenDialog(btnChooseImage.getScene().getWindow());
        if (file == null) return;
        try {
            imageProp.set(ImageAssetManager.saveImage(file, nameProp.get()));
            updateImagePreview();
        } catch (Exception e) {
            showToastError("Image Error", "Unable to copy selected image.");
        }
    }

    private void updateImagePreview() {
        if (lblImagePath != null) lblImagePath.setText(imageProp.get() == null || imageProp.get().isBlank() ? "No image selected" : imageProp.get());
        if (imgPreview == null) return;
        try {
            if (imageProp.get() == null || imageProp.get().isBlank()) {
                imgPreview.setImage(null);
                return;
            }
            imgPreview.setImage(new Image(getClass().getResource(imageProp.get()).toExternalForm(), true));
        } catch (Exception e) {
            imgPreview.setImage(null);
        }
    }

    private void configureImageButtonIcon() {
        if (btnChooseImage == null) return;
        try {
            ImageView icon = new ImageView(new Image(getClass().getResource("/com/lms/laundry/assets/icons/Image-File-Jpg--Streamline-Ultimate.png").toExternalForm()));
            icon.setFitWidth(16);
            icon.setFitHeight(16);
            icon.setPreserveRatio(true);
            btnChooseImage.setGraphic(icon);
        } catch (Exception ignored) {}
    }

    @Override
    protected String getEditHeaderTitle() { return "Edit Product Logging"; }

    @Override
    protected String getCreateHeaderTitle() { return "Log New Business Product"; }

    @Override
    protected String getEditButtonText() { return "Update Product"; }

    @Override
    protected String getCreateButtonText() { return "Save Product"; }

    @Override
    protected String getTargetFxmlName() { return "products-view.fxml"; }

    @Override
    protected String getTargetViewTitle() { return "Products Management"; }

    @Override
    protected void executeSave() {
        if (categoryProp.get() == null || categoryProp.get().getId() == -1) {
            showToastError("Validation Error", "Please select a valid Category.");
            return;
        }

        if (isEditMode) {
            selectedEntity.setImage(imageProp.get());
            selectedEntity.setName(nameProp.get());
            selectedEntity.setBarcode(barcodeProp.get());
            selectedEntity.setProductCategory(categoryProp.get());
            selectedEntity.setDescription(descriptionProp.get());
            selectedEntity.setCostPrice(costPriceProp.get());
            selectedEntity.setPrice(priceProp.get());
            selectedEntity.setStock(stockProp.get());
            selectedEntity.setActive(isActive.get());

            if (ProductManager.update(selectedEntity)) {
                showToastSuccess("Success", "Product profile altered successfully.");
                closeWindow();
            } else {
                showToastError("Database Error", "Unable to commit tracking updates.");
            }
        } else {
            Product newProduct = new Product(
                    0,
                    imageProp.get(),
                    nameProp.get(),
                    barcodeProp.get(),
                    categoryProp.get(),
                    descriptionProp.get(),
                    costPriceProp.get(),
                    priceProp.get(),
                    stockProp.get(),
                    isActive.get(),
                    null,
                    null
            );
            if (ProductManager.create(newProduct)) {
                showToastSuccess("Success", "New product entry saved successfully.");
                closeWindow();
            } else {
                showToastError("Database Error", "Failed to compile background configurations.");
            }
        }
    }

    @Override
    protected void executeFormDeletion() {
        if (selectedEntity != null && ProductManager.delete(selectedEntity.getId())) {
            showToastSuccess("Success", "Operational product entry has been completely removed.");
            closeWindow();
        } else {
            showToastError("Database Error", "Unable to safely drop selected tracking entry.");
        }
    }
}
