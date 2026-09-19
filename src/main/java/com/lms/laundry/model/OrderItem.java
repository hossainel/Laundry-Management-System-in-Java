package com.lms.laundry.model;

import java.util.Objects;

public class OrderItem {
    public enum ItemType { SERVICE, PRODUCT }

    private int id;
    private int orderId;
    private ItemType itemType;
    private int itemId;
    private String itemName;
    private double unitPrice;
    private int quantity;
    private double total;

    public OrderItem() {}

    public OrderItem(int id, int orderId, ItemType itemType, int itemId, String itemName,
                     double unitPrice, int quantity, double total) {
        this.id = id;
        this.orderId = orderId;
        this.itemType = itemType;
        this.itemId = itemId;
        this.itemName = itemName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.total = total;
    }

    public OrderItem(ItemType itemType, int itemId, String itemName, double unitPrice, int quantity) {
        this(0, 0, itemType, itemId, itemName, unitPrice, quantity, unitPrice * quantity);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public ItemType getItemType() { return itemType; }
    public void setItemType(ItemType itemType) { this.itemType = itemType; }
    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        recalculateTotal();
    }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        recalculateTotal();
    }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public void recalculateTotal() {
        this.total = this.unitPrice * this.quantity;
    }

    @Override
    public String toString() { return itemName; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return id == orderItem.id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
