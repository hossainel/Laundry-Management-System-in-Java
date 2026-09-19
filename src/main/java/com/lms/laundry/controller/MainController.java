package com.lms.laundry.controller;

import com.lms.laundry.manager.P;
import com.lms.laundry.manager.IconManager;
import com.lms.laundry.manager.Session;
import com.lms.laundry.model.MenuItem;
import com.lms.laundry.model.User;
import com.lms.laundry.view.LaundryApplication;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.List;

public class MainController {
    public static MainController mainController;
    private final List<MenuItem> menuItems = new ArrayList<>();
    public Label statusLabel;
    @FXML
    public TreeView<MenuItem> menuTreeView;
    @FXML
    private StackPane contentArea;
    @FXML
    private Label topBarTitle;
    private Button activeButton;

    public MainController() {
    }

    /**
     * Public global navigation endpoint.
     * Swaps the main panel layout and synchronizes sidebar visual selections.
     */
    public static <T> T navigate(String fxml, String title) {
        if (mainController != null) {
            T controller = mainController.loadView(fxml, title);
            mainController.syncTreeViewSelection(mainController.menuTreeView.getRoot(), fxml);
            return controller;
        }
        return null;
    }

    @FXML
    public void initialize() {
        mainController = this;
        buildMenu();
        setupTreeViewBehavior();
        // Load the default landing view
        if (!menuItems.isEmpty()) {
            MenuItem first = menuItems.getFirst();
            openPage(first.getTitle(), first.getFxml(), null);
        }
    }

    /**
     * Initializes TreeView presentation properties and event listeners.
     */
    private void setupTreeViewBehavior() {
        // 1. Tell JavaFX how to display the text label for custom MenuItem objects
        menuTreeView.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(MenuItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getTitle());
                }
            }
        });

        // 2. Respond to sidebar element selection changes
        menuTreeView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.getValue() != null) {
                MenuItem clickedItem = newVal.getValue();
                if (clickedItem.getFxml() != null && !clickedItem.getFxml().equals("disabled-view.fxml")) {
                    openPage(clickedItem.getTitle(), clickedItem.getFxml(), null);
                }
            }
        });
    }

    /**
     * Traverses the menu structure recursively to force structural highlights
     * matching arbitrary external page navigations.
     */
    private boolean syncTreeViewSelection(TreeItem<MenuItem> currentItem, String targetFxml) {
        if (currentItem == null) return false;
        MenuItem item = currentItem.getValue();
        if (item != null && targetFxml.equals(item.getFxml())) {
            menuTreeView.getSelectionModel().select(currentItem);
            return true;
        }
        for (TreeItem<MenuItem> child : currentItem.getChildren()) {
            if (syncTreeViewSelection(child, targetFxml)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Constructs structural domain items taking role assignments into account.
     * Removes direct .fxml names from constructors to counter double suffix generation.
     */
    private void buildMenu() {
        boolean is_admin = Session.getRole().equals(User.Role.ADMIN.toString());
        menuItems.clear();
        menuItems.add(new MenuItem("Dashboard"));
        menuItems.add(new MenuItem("POS"));
        MenuItem ordersMenu = new MenuItem("Orders", "disabled");
        ordersMenu.addChild(new MenuItem("Order List", "orders"));
        ordersMenu.addChild(new MenuItem("Order Calendar", "order-calender"));
        menuItems.add(ordersMenu);
        MenuItem servicesMenu = new MenuItem("Services", "disabled");
        servicesMenu.addChild(new MenuItem("Service List", "services"));
        servicesMenu.addChild(new MenuItem("Service Type", "service-types"));
        servicesMenu.addChild(new MenuItem("Service Categories", "service-categories"));
        menuItems.add(servicesMenu);
        MenuItem productsMenu = new MenuItem("Products", "disabled");
        productsMenu.addChild(new MenuItem("Product List", "products"));
        productsMenu.addChild(new MenuItem("Product Categories", "product-categories"));
        menuItems.add(productsMenu);
        MenuItem expenseMenu = new MenuItem("Expenses", "disabled");
        expenseMenu.addChild(new MenuItem("Expense List", "expenses"));
        expenseMenu.addChild(new MenuItem("Expense Categories", "expense-categories"));
        menuItems.add(expenseMenu);
        MenuItem paymentsMenu = new MenuItem("Payments", "disabled");
        paymentsMenu.addChild(new MenuItem("Payment List", "payments"));
        paymentsMenu.addChild(new MenuItem("Payment Methods", "payment-methods"));
        menuItems.add(paymentsMenu);
        menuItems.add(new MenuItem("Customers"));
        MenuItem reportsMenu = new MenuItem("Reports", "disabled");
        reportsMenu.addChild(new MenuItem("Trial balance", "trial-balance"));
        reportsMenu.addChild(new MenuItem("Customer Ledger", "customer-ledger"));
        reportsMenu.addChild(new MenuItem("Daily Reports", "daily-reports"));
        reportsMenu.addChild(new MenuItem("Order Reports", "order-reports"));
        reportsMenu.addChild(new MenuItem("Order Calendar", "order-calender"));
        reportsMenu.addChild(new MenuItem("Sales Reports", "sales-reports"));
        reportsMenu.addChild(new MenuItem("Tax Reports", "tax-reports"));
        menuItems.add(reportsMenu);
        if (is_admin) {
            menuItems.add(new MenuItem("Users", "users", MenuItem.Role.ADMIN));
            menuItems.add(new MenuItem("Settings", "settings", MenuItem.Role.ADMIN));
        }
        TreeItem<MenuItem> rootItem = new TreeItem<>(new MenuItem("Root", "root"));
        rootItem.setExpanded(true);
        for (MenuItem item : menuItems) {
            rootItem.getChildren().add(createTreeBranch(item));
        }
        menuTreeView.setRoot(rootItem);
        menuTreeView.setShowRoot(false);
    }

    /**
     * Helper method to recursively build sub-menu nodes down the tree structural chain.
     */
    private TreeItem<MenuItem> createTreeBranch(MenuItem item) {
        TreeItem<MenuItem> branch = new TreeItem<>(item);
        if (item.hasChildren()) {
            branch.setExpanded(false); // Keeps secondary sub-menus collapsed by default
            for (MenuItem child : item.getChildren()) {
                branch.getChildren().add(createTreeBranch(child));
            }
        }
        return branch;
    }

    private void openPage(String name, String fxml, Button btn) {
        topBarTitle.setText(name);
        setActiveButton(btn);
        loadView(fxml, name);
    }

    /**
     * Performs core loader routines pulling assets directly onto the main frame.
     */
    private <T> T loadView(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/lms/laundry/view/" + fxml));
            Parent view = loader.load();
            IconManager.applyIcons(view);
            contentArea.getChildren().setAll(view);
            topBarTitle.setText(title);
            LaundryApplication.setTitle(title);
            return (T) loader.getController();
        } catch (Exception e) {
            P.pf("Error loading view: " + fxml);
            P.pf(e.getMessage());
            return null;
        }
    }

    private void setActiveButton(Button button) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("sidebar-button-active");
        }
        activeButton = button;
        if (activeButton != null) {
            activeButton.getStyleClass().add("sidebar-button-active");
        }
    }

    @FXML
    public void logout() {
        Session.clearSession();
        Session.setCurrentUser(null);
        LaundryApplication.loadScene("login-view.fxml", "Login");
    }
}
