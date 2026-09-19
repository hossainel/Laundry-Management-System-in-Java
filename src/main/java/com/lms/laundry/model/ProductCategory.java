package com.lms.laundry.model;

import java.util.Objects;

public class ProductCategory {
    private int id;
    private String name;
    private boolean active;

    public ProductCategory() {}

    public ProductCategory(int id, String name, boolean active) {
        this.id = id;
        this.name = name;
        this.active = active;
    }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setActive(boolean active) { this.active = active; }
    public int getId() { return this.id; }
    public String getName() { return this.name; }
    public boolean isActive() { return this.active; }
    @Override
    public String toString() { return this.name; }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductCategory that = (ProductCategory) o;
        return this.id == that.id;
    }
    @Override
    public int hashCode() { return Objects.hash(id); }
}
