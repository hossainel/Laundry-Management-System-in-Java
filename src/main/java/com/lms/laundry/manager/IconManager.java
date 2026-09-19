package com.lms.laundry.manager;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class IconManager {
    private static final String ICON_PREFIX = "/com/lms/laundry/assets/icons/";

    private IconManager() {}

    public static void applyIcons(Parent parent) {
        if (parent == null) return;
        for (Node node : parent.lookupAll(".button")) {
            if (node instanceof Button button) {
                decorateButton(button);
            }
        }
    }

    public static void decorateButton(Button button) {
        if (button == null || button.getGraphic() != null) return;
        String icon = iconFor(button.getText());
        if (icon == null) return;
        setButtonIcon(button, icon);
    }

    public static void setButtonIcon(Button button, String iconFile) {
        if (button == null || iconFile == null) return;
        try {
            Image image = new Image(IconManager.class.getResource(ICON_PREFIX + iconFile).toExternalForm());
            ImageView view = new ImageView(image);
            view.setFitWidth(16);
            view.setFitHeight(16);
            view.setPreserveRatio(true);
            button.setGraphic(view);
        } catch (Exception ignored) {
            return;
        }
    }

    private static String iconFor(String text) {
        if (text == null) return null;
        String value = text.trim().toLowerCase();
        if (value.isEmpty()) return null;

        if (value.contains("add") || value.contains("new")) {
            return "add-new.png";
        }
        if (value.contains("save") || value.contains("update")) {
            return "save-update.png";
        }
        if (value.contains("edit")) {
            return "edit.png";
        }
        if (value.contains("delete") || value.contains("remove")) {
            return "delete-remove.png";
        }
        if (value.contains("cancel") || value.contains("clear")) {
            return "cancel-clear.png";
        }
        if (value.contains("refresh")) {
            return "refresh.png";
        }
        if (value.contains("choose") || value.contains("upload") || value.contains("photo")) {
            return "image.png";
        }
        if (value.contains("pay") || value.contains("payment")) {
            return "payment.png";
        }
        if (value.contains("ledger") || value.contains("report")) {
            return "ledger.png";
        }
        if (value.contains("logout")) {
            return "logout.png";
        }
        return null;
    }
}
