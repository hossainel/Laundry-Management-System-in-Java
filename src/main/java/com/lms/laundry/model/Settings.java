package com.lms.laundry.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Settings {
    private int id;
    private String settingKey;
    private String settingValue;
    private String description;
    private LocalDateTime updatedAt;

    public Settings() {}

    public Settings(int id, String key, String value) {
        this.id = id;
        this.settingKey = key;
        this.settingValue = value;
    }
    public Settings(int id, String key, String value, String description) {
        this.id = id;
        this.settingKey = key;
        this.settingValue = value;
        this.description = description;
    }

    public void setId(int id) { this.id = id; }
    public void setKey(String key) { this.settingKey = key; }
    public void setValue(String value) { this.settingValue = value; }
    public void setDescription(String description) { this.description = description; }
    public int getId() { return this.id; }
    public String getKey() { return this.settingKey; }
    public String getValue() { return this.settingValue; }
    public String getDescription() { return this.description; }
    public LocalDateTime getUpdatedAt() { return this.updatedAt; }
    @Override
    public String toString() { return this.settingKey; }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Settings that = (Settings) o;
        return this.id == that.id;
    }
    @Override
    public int hashCode() { return Objects.hash(id); }
}
