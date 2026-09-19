package com.lms.laundry.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class Payment {
    private int id;
    private int methodId;
    private String methodName;
    private PaymentMethod paymentMethod;
    private int customerId;
    private String customerName;
    private Customer customer;
    private double amount;
    private LocalDate paymentDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Payment() {}

    // Database mapper constructor
    public Payment(int id, int methodId, String methodName, double amount, int customerId,
                   String customerName, LocalDate paymentDate, LocalDateTime createdAt,
                   LocalDateTime updatedAt) {
        this.id = id;
        this.methodId = methodId;
        this.methodName = methodName;
        this.paymentMethod = new PaymentMethod(methodId, methodName, true);
        this.amount = amount;
        this.customerId = customerId;
        this.customerName = customerName;
        this.paymentDate = paymentDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Application layer constructor
    public Payment(int id, PaymentMethod paymentMethod, double amount, Customer customer,
                   LocalDate paymentDate, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.setPaymentMethod(paymentMethod);
        this.amount = amount;
        this.setCustomer(customer);
        this.paymentDate = paymentDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getMethodId() { return methodId; }
    public void setMethodId(int methodId) { this.methodId = methodId; }

    public String getMethodName() { return methodName; }
    public void setMethodName(String methodName) { this.methodName = methodName; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
        if (paymentMethod != null) {
            this.methodId = paymentMethod.getId();
            this.methodName = paymentMethod.getName();
        }
    }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

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

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate() { this.paymentDate = LocalDate.now(); }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    @Override
    public String toString() {
        return customerName + " - " + amount;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment that = (Payment) o;
        return this.id == that.id;
    }
    @Override
    public int hashCode() { return Objects.hash(id); }
}
