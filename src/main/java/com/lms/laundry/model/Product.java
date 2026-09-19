package com.lms.laundry.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Product {
    private int id;
    private String image;
    private String name;
    private String barcode;
    private int categoryId;
    private String categoryName;
    private ProductCategory productCategory;
    private String description;
    private double costPrice;
    private double price;
    private int stock;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Product() {}

    // Database mapper constructor
    public Product(int id, String image, String name, String barcode, int categoryId,
                   String categoryName, String description, double costPrice, double price,
                   int stock, boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.image = image;
        this.name = name;
        this.barcode = barcode;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.productCategory = new ProductCategory(categoryId, categoryName, true);
        this.description = description;
        this.costPrice = costPrice;
        this.price = price;
        this.stock = stock;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Application layer constructor
    public Product(int id, String image, String name, String barcode,
                   ProductCategory productCategory, String description, double costPrice,
                   double price, int stock, boolean active, LocalDateTime createdAt,
                   LocalDateTime updatedAt) {
        this.id = id;
        this.image = image;
        this.name = name;
        this.barcode = barcode;
        this.setProductCategory(productCategory);
        this.description = description;
        this.costPrice = costPrice;
        this.price = price;
        this.stock = stock;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public ProductCategory getProductCategory() { return productCategory; }
    public void setProductCategory(ProductCategory productCategory) {
        this.productCategory = productCategory;
        if (productCategory != null) {
            this.categoryId = productCategory.getId();
            this.categoryName = productCategory.getName();
        }
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getCostPrice() { return costPrice; }
    public void setCostPrice(double costPrice) { this.costPrice = costPrice; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public void addStock(int stock) { this.stock += stock; }
    public void subStock(int stock) { this.stock -= stock; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    @Override
    public String toString() { return this.name; }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product that = (Product) o;
        return this.id == that.id;
    }
    @Override
    public int hashCode() { return Objects.hash(id); }
}
