package com.lms.laundry.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MenuItem {
    private String title;
    private String fxml;
    private Role role;
    private final List<MenuItem> children = new ArrayList<>();

    public enum Role {
        ALL, ADMIN, STAFF
    }

    public MenuItem(String title) {
        this.title = title;
        this.fxml = title.toLowerCase() + "-view.fxml";
        this.role = Role.ALL;
    }

    public MenuItem(String title, String fxml) {
        this.title = title;
        this.fxml = fxml + "-view.fxml";
        this.role = Role.ALL;
    }

    public MenuItem(String title, String fxml, Role role) {
        this.title = title;
        this.fxml = fxml + "-view.fxml";
        this.role = role;
    }

    // Helper method to add secondary sub-menus
    public void addChild(MenuItem child) {
        this.children.add(child);
    }

    public List<MenuItem> getChildren() {
        return children;
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public String getTitle() { return title; }
    public String getFxml() { return fxml; }
    public Role getRole() { return role; }
    public void setTitle(String title) { this.title = title; }
    public void setFxml(String fxml) { this.fxml = fxml; }
    public void setRole(Role role) { this.role = role; }

    @Override
    public String toString() { return this.title; }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuItem that = (MenuItem) o;
        return this.title.equals(that.title);
    }
    @Override
    public int hashCode() { return Objects.hash(title); }
}
