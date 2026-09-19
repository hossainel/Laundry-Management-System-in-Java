package com.lms.laundry.model;

public class POS {
    private String label;
    private OrderItem.ItemType itemType;
    private int itemId;
    private double price;

    public POS() {}

    public POS(String label, OrderItem.ItemType itemType, int itemId, double price) {
        this.label = label;
        this.itemType = itemType;
        this.itemId = itemId;
        this.price = price;
    }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public OrderItem.ItemType getItemType() { return itemType; }
    public void setItemType(OrderItem.ItemType itemType) { this.itemType = itemType; }
    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    @Override
    public String toString() { return label; }
}
