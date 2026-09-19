package com.lms.laundry.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

public class Service {
    private int id;
    private int categoryId;
    private String categoryName;
    private ServiceCategory serviceCategory;
    private String image;
    private String name;
    private List<Integer> typeIds = new ArrayList<>();
    private List<String> typeNames = new ArrayList<>();
    private List<Double> prices = new ArrayList<>();
    private List<ServiceType> serviceTypes = new ArrayList<>();
    private boolean active;

    public Service() {}

    public Service(int id, int categoryId, String categoryName, String image, String name,
                   boolean active, String typeIdsStr, String typeNamesStr, String pricesStr) {
        this.id = id;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.image = image;
        this.name = name;
        this.active = active;
        this.serviceCategory = new ServiceCategory(categoryId, categoryName, true);
        if (typeIdsStr != null && !typeIdsStr.isEmpty()) {
            for (String val : typeIdsStr.split(",")) {
                this.typeIds.add(Integer.parseInt(val.trim()));
            }
        }
        if (typeNamesStr != null && !typeNamesStr.isEmpty()) {
            for (String val : typeNamesStr.split(java.util.regex.Pattern.quote("??"))) {
                this.typeNames.add(val.trim());
            }
        }
        if (pricesStr != null && !pricesStr.isEmpty()) {
            for (String val : pricesStr.split(",")) {
                this.prices.add(Double.parseDouble(val.trim()));
            }
        }
        syncServiceTypesFromFlatLists();
    }
    public Service(int id, ServiceCategory serviceCategory, String image, String name,
                   List<ServiceType> serviceTypes, boolean active) {
        this.id = id;
        this.image = image;
        this.name = name;
        this.active = active;
        this.setServiceCategory(serviceCategory);
        this.setServiceTypes(serviceTypes);
    }
    private void syncServiceTypesFromFlatLists() {
        this.serviceTypes = new ArrayList<>();
        int size = Math.min(typeIds.size(), typeNames.size());
        size = Math.min(size, prices.size());
        for (int i = 0; i < size; i++) {
            this.serviceTypes.add(new ServiceType(
                    typeIds.get(i),
                    typeNames.get(i),
                    prices.get(i),
                    true
            ));
        }
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public ServiceCategory getServiceCategory() { return serviceCategory; }
    public void setServiceCategory(ServiceCategory serviceCategory) {
        this.serviceCategory = serviceCategory;
        if (serviceCategory != null) {
            this.categoryId = serviceCategory.getId();
            this.categoryName = serviceCategory.getName();
        }
    }
    public List<Integer> getTypeIds() { return typeIds; }
    public void setTypeIds(List<Integer> typeIds) { this.typeIds = typeIds; }
    public List<String> getTypeNames() { return typeNames; }
    public void setTypeNames(List<String> typeNames) { this.typeNames = typeNames; }
    public List<Double> getPrices() { return prices; }
    public void setPrices(List<Double> prices) { this.prices = prices; }
    public List<ServiceType> getServiceTypes() { return serviceTypes; }
    public void setServiceTypes(List<ServiceType> serviceTypes) {
        this.serviceTypes = serviceTypes != null ? serviceTypes : new ArrayList<>();
        this.typeIds = new ArrayList<>();
        this.typeNames = new ArrayList<>();
        this.prices = new ArrayList<>();
        for (ServiceType serviceType : this.serviceTypes) {
            if (serviceType != null) {
                this.typeIds.add(serviceType.getId());
                this.typeNames.add(serviceType.getName());
                this.prices.add(serviceType.getPrice());
            }
        }
    }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    @Override
    public String toString() { return this.name; }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Service that = (Service) o;
        return this.id == that.id;
    }
    @Override
    public int hashCode() { return Objects.hash(id); }
}
