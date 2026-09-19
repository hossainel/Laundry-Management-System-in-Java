package com.lms.laundry.model;

import java.util.Objects;

public class ServiceType {
    private int id;
    private String name;
    private double price;
    private boolean active;

    public ServiceType() {}

    public ServiceType(int id, String name, boolean active) {
        this.id = id;
        this.name = name;
        this.price = 0.0;
        this.active = active;
    }

    public ServiceType(int id, String name, double price, boolean active) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.active = active;
    }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setActive(boolean active) { this.active = active; }
    public int getId() { return this.id; }
    public String getName() { return this.name; }
    public boolean isActive() { return this.active; }
    public double getPrice() { return this.price; }
    public void setPrice(double price) { this.price = price; }
    @Override
    public String toString() { return this.name; }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceType that = (ServiceType) o;
        return this.id == that.id;
    }
    @Override
    public int hashCode() { return Objects.hash(id); }
}
