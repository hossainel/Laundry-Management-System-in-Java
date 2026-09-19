package com.lms.laundry.view;

import com.lms.laundry.manager.IconManager;
import com.lms.laundry.manager.P;
import com.lms.laundry.manager.Session;
import com.lms.laundry.manager.UserManager;
import com.lms.laundry.model.User;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LaundryApplication extends Application {
    private static Stage primaryStage;
    private static Scene scene;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        try {
            if (Session.isRemembered()) {
                String username = Session.getUsername();
                if (username != null) {
                    User user = UserManager.getByUsername(username);
                    if (user != null) {
                        Session.setCurrentUser(user);
                        loadScene("main-view.fxml", "Dashboard");
                    } else {
                        Session.clearSession();
                        loadScene("login-view.fxml", "Login");
                    }
                } else {
                    loadScene("login-view.fxml", "Login");
                }
            } else {
                loadScene("login-view.fxml", "Login");
            }
            primaryStage.show();
        } catch (Exception e) {
            P.pf("App startup failed: " + e.getMessage());
        }
    }
    /**
     * Global Screen Loader (FULL APP CONTROLLER)
     */
    public static void loadScene(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    LaundryApplication.class.getResource(fxml)
            );
            Parent root = loader.load();
            IconManager.applyIcons(root);
            if (scene == null) {
                scene = new Scene(root, 1200, 700);
                primaryStage.setMinWidth(1000);
                primaryStage.setMinHeight(600);
            } else {
                scene.setRoot(root);
            }
            primaryStage.setScene(scene);
            setTitle(title);
            primaryStage.centerOnScreen();
        } catch (Exception e) {
            P.pf("Failed to load view: " + fxml);
        }
    }
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    public static void setTitle(String title) {
        if (primaryStage != null) {
            primaryStage.setTitle("LMS (Laundry Management System) | " + title);
        }
    }
}
