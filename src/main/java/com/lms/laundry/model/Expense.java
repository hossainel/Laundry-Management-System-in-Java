package com.lms.laundry.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class Expense {
    private int id;
    private String title;
    private int categoryId;
    private String categoryName;
    private ExpenseCategory expenseCategory;
    private double amount;
    private LocalDate date;
    private int paymentMethodId;
    private String paymentMethodName;
    private PaymentMethod paymentMethod;
    private String note;
    private LocalDateTime createdAt;

    public Expense() {}

    // Database mapper constructor
    public Expense(int id, String title, int categoryId, String categoryName,
                   double amount, LocalDate date, int paymentMethodId,
                   String paymentMethodName, String note, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.expenseCategory = new ExpenseCategory(categoryId, categoryName, true);
        this.amount = amount;
        this.date = date;
        this.paymentMethodId = paymentMethodId;
        this.paymentMethodName = paymentMethodName;
        this.paymentMethod = new PaymentMethod(paymentMethodId, paymentMethodName, true);
        this.note = note;
        this.createdAt = createdAt;
    }

    // Application layer constructor
    public Expense(int id, String title, ExpenseCategory expenseCategory, double amount,
                   LocalDate date, PaymentMethod paymentMethod, String note) {
        this.id = id;
        this.title = title;
        this.setExpenseCategory(expenseCategory);
        this.amount = amount;
        this.date = date;
        this.setPaymentMethod(paymentMethod);
        this.note = note;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public ExpenseCategory getExpenseCategory() { return expenseCategory; }
    public void setExpenseCategory(ExpenseCategory expenseCategory) {
        this.expenseCategory = expenseCategory;
        if (expenseCategory != null) {
            this.categoryId = expenseCategory.getId();
            this.categoryName = expenseCategory.getName();
        }
    }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public LocalDate getDate() { return date; }
    public void setDate() { this.date = LocalDate.now(); }
    public void setDate(LocalDate date) { this.date = date; }

    public int getPaymentMethodId() { return paymentMethodId; }
    public void setPaymentMethodId(int paymentMethodId) { this.paymentMethodId = paymentMethodId; }

    public String getPaymentMethodName() { return paymentMethodName; }
    public void setPaymentMethodName(String paymentMethodName) { this.paymentMethodName = paymentMethodName; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
        if (paymentMethod != null) {
            this.paymentMethodId = paymentMethod.getId();
            this.paymentMethodName = paymentMethod.getName();
        }
    }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    @Override
    public String toString() { return this.title; }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Expense that = (Expense) o;
        return this.id == that.id;
    }
    @Override
    public int hashCode() { return Objects.hash(id); }
}
