package com.lms.laundry.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Customer {
    private int id;
    private String name;
    private String phone;
    private String email;
    private String address;
    private double total_order = 0.0;
    private double total_payment = 0.0;
    private double balance = 0.0;
    private boolean due = false;
    private LocalDateTime createdAt;

    public Customer() {}

    public Customer(int id, String name, String phone, String email, String address) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
    }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public void setAddress(String address) { this.address = address; }
    public void setTotalOrder(double total_order) {
        this.total_order = total_order ;
        this.due = this.total_order>this.total_payment;
        this.balance = this.total_order-this.total_payment;
    }
    public void setTotalPayment(double total_payment) {
        this.total_payment = total_payment  ;
        this.due = this.total_order>this.total_payment;
        this.balance = this.total_order-this.total_payment;
    }
    public int getId() { return this.id; }
    public String getName() { return this.name; }
    public String getPhone() { return this.phone; }
    public String getEmail() { return this.email; }
    public String getAddress() { return this.address; }
    public double getTotalOrder() { return this.total_order; }
    public double getTotalPayment() { return this.total_payment; }
    public double getBalance() {
        return this.balance;
    }
    public boolean isDue() { return this.balance > 0; }
    public boolean isAdvance() { return this.balance < 0; }
    public double getAbsoluteBalance() { return Math.abs(this.balance); }
    public String getBalanceType() {
        if (this.balance > 0) return "Due";
        if (this.balance < 0) return "Advance";
        return "Settled";
    }
    public String getBalanceDisplay() {
        return getBalanceType() + " " + String.format("%.2f", getAbsoluteBalance());
    }
    public LocalDateTime getCreatedAt() { return this.createdAt; }
    @Override
    public String toString() { return this.name; }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer that = (Customer) o;
        return this.id == that.id;
    }
    @Override
    public int hashCode() { return Objects.hash(id); }
}
