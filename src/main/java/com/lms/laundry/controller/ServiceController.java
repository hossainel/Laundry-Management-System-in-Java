package com.lms.laundry.controller;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.Form;
import com.dlsc.formsfx.model.structure.Group;
import com.dlsc.formsfx.model.validators.StringLengthValidator;
import com.lms.laundry.manager.ServiceCategoryManager;
import com.lms.laundry.manager.ImageAssetManager;
import com.lms.laundry.manager.IconManager;
import com.lms.laundry.manager.ServiceManager;
import com.lms.laundry.manager.ServiceTypeManager;
import com.lms.laundry.model.Service;
import com.lms.laundry.model.ServiceCategory;
import com.lms.laundry.model.ServiceType;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ServiceController extends BaseFormController<Service> {
    private final StringProperty imageProp = new SimpleStringProperty("");
    private final StringProperty nameProp = new SimpleStringProperty("");
    private final BooleanProperty isActive = new SimpleBooleanProperty(true);
    private final ObjectProperty<ServiceCategory> categoryProp = new SimpleObjectProperty<>();

    private final ListProperty<ServiceCategory> categoryOptions =
            new SimpleListProperty<>(FXCollections.observableArrayList());

    private final List<ServiceType> globalMasterTypesList = new ArrayList<>();
    private final ServiceCategory placeholderCategory = new ServiceCategory(-1, "-- Select Category --", true);
    private boolean isUpdatingOptions = false;

    @FXML
    private VBox vboxServiceTypes;
    @FXML
    private Button btnAddVariant;
    @FXML
    private ImageView imgPreview;
    @FXML
    private Label lblImagePath;
    @FXML
    private Button btnChooseImage;
    @FXML
    public void initialize() {
        loadDropdownDataFromDatabase();
        configureImageButtonIcon();
    }
    private void loadDropdownDataFromDatabase() {
        try {
            List<ServiceCategory> categories = ServiceCategoryManager.getAll();
            this.categoryOptions.clear();
            this.categoryOptions.add(placeholderCategory);
            if (categories != null) this.categoryOptions.addAll(categories);
            List<ServiceType> globalTypes = ServiceTypeManager.getAll();
            if (globalTypes != null) {
                this.globalMasterTypesList.addAll(globalTypes);
            }
        } catch (Exception e) {
            System.out.println("Failed to pre-populate service dropdown lists: " + e.getMessage());
        }
    }
    @Override
    protected void clearFormFields() {
        imageProp.set("");
        nameProp.set("");
        isActive.set(true);
        categoryProp.set(placeholderCategory);
        if (vboxServiceTypes != null) vboxServiceTypes.getChildren().clear();
        updateAddButtonAndDropdownOptions();
        updateImagePreview();
    }
    @Override
    protected Form initFormStructure(Service service) {
        if (isEditMode && service != null) {
            imageProp.set(service.getImage());
            nameProp.set(service.getName());
            isActive.set(service.isActive());
            categoryProp.set(service.getServiceCategory() != null ? service.getServiceCategory() : placeholderCategory);
            if (vboxServiceTypes != null) {
                vboxServiceTypes.getChildren().clear();
                List<Integer> ids = service.getTypeIds();
                List<Double> prices = service.getPrices();
                int size = Math.min(ids.size(), prices.size());
                for (int i = 0; i < size; i++) {
                    createVariantRowUI(ids.get(i), prices.get(i));
                }
            }
        } else { clearFormFields(); }
        updateImagePreview();
        return Form.of(
                Group.of(
                        Field.ofStringType(nameProp)
                                .label("Service Name")
                                .placeholder("e.g., Premium Dry Clean")
                                .validate(StringLengthValidator.atLeast(2, "Name must be at least 2 characters long.")),

                        Field.ofSingleSelectionType(categoryOptions, categoryProp)
                                .label("Service Category"),

                        Field.ofBooleanType(isActive).label("Is Active")
                )
        );
    }
    @FXML
    private void handleChooseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Service Photo");
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
    @FXML
    private void handleAddVariantRow() {
        if (vboxServiceTypes.getChildren().size() >= globalMasterTypesList.size()) {
            return;
        }
        createVariantRowUI(null, 0.0);
    }
    private void createVariantRowUI(Integer selectedTypeId, Double currentPrice) {
        HBox rowContainer = new HBox(12);
        rowContainer.setAlignment(Pos.CENTER_LEFT);
        rowContainer.setStyle("-fx-padding: 4; -fx-background-color: #fdfdfd; -fx-border-color: #e0e0e0; -fx-border-radius: 4;");
        Label lblIndex = new Label((vboxServiceTypes.getChildren().size() + 1) + ".");
        lblIndex.setStyle("-fx-font-weight: bold; -fx-min-width: 20px;");
        ObservableList<ServiceType> rowSpecificOptions = FXCollections.observableArrayList();
        ComboBox<ServiceType> comboTypes = new ComboBox<>(rowSpecificOptions);
        comboTypes.setPromptText("Select Type");
        comboTypes.setPrefWidth(180.0);
        if (selectedTypeId != null) {
            globalMasterTypesList.stream()
                    .filter(t -> t.getId() == selectedTypeId)
                    .findFirst()
                    .ifPresent(comboTypes::setValue);
        }
        TextField txtPrice = new TextField();
        txtPrice.setPromptText("0.00");
        txtPrice.setPrefWidth(100.0);
        if (currentPrice != null && currentPrice > 0) {
            txtPrice.setText(String.valueOf(currentPrice));
        }
        txtPrice.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) txtPrice.setText(oldVal);
        });
        Button btnDeleteRow = new Button("Delete");
        btnDeleteRow.setStyle("-fx-background-color: #fbe9e7; -fx-text-fill: #d84315; -fx-font-weight: bold;");
        IconManager.decorateButton(btnDeleteRow);
        btnDeleteRow.setOnAction(e -> {
            vboxServiceTypes.getChildren().remove(rowContainer);
            reindexRowLabels();
            updateAddButtonAndDropdownOptions();
        });
        comboTypes.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (!isUpdatingOptions && (newVal != null || oldVal != null)) {
                updateAddButtonAndDropdownOptions();
            }
        });
        rowContainer.getChildren().addAll(lblIndex, comboTypes, txtPrice, btnDeleteRow);
        vboxServiceTypes.getChildren().add(rowContainer);
        updateAddButtonAndDropdownOptions();
    }
    private void updateAddButtonAndDropdownOptions() {
        if (vboxServiceTypes == null || isUpdatingOptions) return;
        try {
            isUpdatingOptions = true;
            // 1. Collect all currently selected IDs across all rows
            Set<Integer> globallyAllocatedIds = new HashSet<>();
            for (Node node : vboxServiceTypes.getChildren()) {
                if (node instanceof HBox row) {
                    ComboBox<ServiceType> cb = (ComboBox<ServiceType>) row.getChildren().get(1);
                    ServiceType selected = cb.getValue();
                    if (selected != null) {
                        globallyAllocatedIds.add(selected.getId());
                    }
                }
            }
            // 2. Refresh the options list for each row individually without losing active item context
            for (Node node : vboxServiceTypes.getChildren()) {
                if (node instanceof HBox row) {
                    ComboBox<ServiceType> cb = (ComboBox<ServiceType>) row.getChildren().get(1);
                    ServiceType currentSelection = cb.getValue();
                    List<ServiceType> permissibleOptions = new ArrayList<>();
                    for (ServiceType type : globalMasterTypesList) {
                        if (!globallyAllocatedIds.contains(type.getId()) ||
                                (currentSelection != null && currentSelection.getId() == type.getId())) {
                            permissibleOptions.add(type);
                        }
                    }
                    ObservableList<ServiceType> currentItems = cb.getItems();
                    if (!currentItems.equals(permissibleOptions)) {
                        currentItems.setAll(permissibleOptions);
                        if (currentSelection != null) {
                            cb.setValue(currentSelection);
                        } else {
                            cb.getSelectionModel().select(null);
                        }
                    }
                }
            }
            // 3. Keep button availability synchronized
            if (btnAddVariant != null) {
                int currentActiveRowCount = vboxServiceTypes.getChildren().size();
                boolean isPoolExhausted = currentActiveRowCount >= globalMasterTypesList.size();
                btnAddVariant.setDisable(isPoolExhausted);
                if (isPoolExhausted) {
                    btnAddVariant.setOpacity(0.5);
                    btnAddVariant.setText("All Types Allocated");
                } else {
                    btnAddVariant.setOpacity(1.0);
                    btnAddVariant.setText("+ Add Variant Row");
                }
            }
        } finally {
            isUpdatingOptions = false;
        }
    }
    private void reindexRowLabels() {
        int index = 1;
        for (Node node : vboxServiceTypes.getChildren()) {
            if (node instanceof HBox row) {
                if (!row.getChildren().isEmpty() && row.getChildren().getFirst() instanceof Label lbl) {
                    lbl.setText(index + ".");
                    index++;
                }
            }
        }
    }
    @Override protected String getEditHeaderTitle() { return "Edit Service Specifications"; }
    @Override protected String getCreateHeaderTitle() { return "Log New Laundry Service"; }
    @Override protected String getEditButtonText() { return "Update Service"; }
    @Override protected String getCreateButtonText() { return "Save Service"; }
    @Override protected String getTargetFxmlName() { return "services-view.fxml"; }
    @Override protected String getTargetViewTitle() { return "Services Management"; }
    @Override
    protected void executeSave() {
        if (categoryProp.get() == null || categoryProp.get().getId() == -1) {
            showToastError("Validation Error", "Please select a valid Service Category.");
            return;
        }
        List<Integer> finalTypeIds = new ArrayList<>();
        List<String> finalTypeNames = new ArrayList<>();
        List<Double> finalPrices = new ArrayList<>();
        for (Node node : vboxServiceTypes.getChildren()) {
            if (node instanceof HBox row) {
                ComboBox<ServiceType> combo = (ComboBox<ServiceType>) row.getChildren().get(1);
                TextField txt = (TextField) row.getChildren().get(2);
                ServiceType selectedType = combo.getValue();
                String rawPrice = txt.getText().trim();
                if (selectedType == null || rawPrice.isEmpty()) {
                    showToastError("Validation Error", "Please fill out or delete uncompleted rows.");
                    return;
                }
                finalTypeIds.add(selectedType.getId());
                finalTypeNames.add(selectedType.getName());
                finalPrices.add(Double.parseDouble(rawPrice));
            }
        }
        if (isEditMode) {
            selectedEntity.setImage(imageProp.get());
            selectedEntity.setName(nameProp.get());
            selectedEntity.setActive(isActive.get());
            selectedEntity.setServiceCategory(categoryProp.get());
            selectedEntity.setTypeIds(finalTypeIds);
            selectedEntity.setTypeNames(finalTypeNames);
            selectedEntity.setPrices(finalPrices);
            if (ServiceManager.update(selectedEntity)) {
                showToastSuccess("Success", "Service variations altered successfully.");
                closeWindow();
            } else {
                showToastError("Database Error", "Unable to commit tracking updates.");
            }
        } else {
            Service newService = new Service(0, categoryProp.get().getId(), categoryProp.get().getName(),
                    imageProp.get(), nameProp.get(), isActive.get(), "", "", "");
            newService.setTypeIds(finalTypeIds);
            newService.setTypeNames(finalTypeNames);
            newService.setPrices(finalPrices);
            if (ServiceManager.create(newService)) {
                showToastSuccess("Success", "New transaction profile cataloged seamlessly.");
                closeWindow();
            } else {
                showToastError("Database Error", "Failed to compile background configurations.");
            }
        }
    }
    @Override
    protected void executeFormDeletion() {
        if (selectedEntity != null && ServiceManager.delete(selectedEntity.getId())) {
            showToastSuccess("Success", "Operational service entry completely removed.");
            closeWindow();
        } else {
            showToastError("Database Error", "Unable to safely drop selected tracking entry.");
        }
    }
}
