package com.lms.laundry.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Order {
    public enum Status { RECEIVED, WASHING, READY, DELIVERED }

    private int id;
    private int customerId;
    private String customerName;
    private Customer customer;
    private LocalDateTime orderDate;
    private LocalDate deliveryDate;
    private Status status = Status.RECEIVED;
    private double subtotal;
    private double discount;
    private double total;
    private double paidAmount;
    private double dueAmount;
    private List<OrderItem> items = new ArrayList<>();

    public Order() {}

    public Order(int id, int customerId, String customerName, LocalDateTime orderDate,
                 LocalDate deliveryDate, Status status, double subtotal, double discount,
                 double total, double paidAmount, double dueAmount) {
        this.id = id;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customer = new Customer(customerId, customerName, "", "", "");
        this.orderDate = orderDate;
        this.deliveryDate = deliveryDate;
        this.status = status;
        this.subtotal = subtotal;
        this.discount = discount;
        this.total = total;
        this.paidAmount = paidAmount;
        this.dueAmount = dueAmount;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) {
        this.customer = customer;
        if (customer != null) {
            this.customerId = customer.getId();
            this.customerName = customer.getName();
        }
    }
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public LocalDate getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(LocalDate deliveryDate) { this.deliveryDate = deliveryDate; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) {
        this.status = status;
        if(status== Status.DELIVERED) {
            this.deliveryDate = LocalDate.now();
        }
    }
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public double getPaidAmount() { return paidAmount; }
    public void setPaidAmount(double paidAmount) { this.paidAmount = paidAmount; }
    public double getDueAmount() { return dueAmount; }
    public void setDueAmount(double dueAmount) { this.dueAmount = dueAmount; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items != null ? items : new ArrayList<>(); }

    public void calculateTotals() {
        subtotal = items.stream().mapToDouble(OrderItem::getTotal).sum();
        total = Math.max(0, subtotal - discount);
        dueAmount = Math.max(0, total - paidAmount);
    }

    @Override
    public String toString() { return "Order #" + id; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return id == order.id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
