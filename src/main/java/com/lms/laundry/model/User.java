package com.lms.laundry.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class User {
    public enum Role { ADMIN, STAFF }
    private int id;
    private String name;
    private String username;
    private String password;
    private Role role;
    private boolean active;
    private LocalDateTime createdAt;

    public User() {}

    public User(int id, String name, String username, String password, Role role,
                boolean active) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.password = password;
        this.role = role;
        this.active = active;
    }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(Role role) { this.role = role; }
    public void setActive(boolean active) { this.active = active; }
    public int getId() { return this.id; }
    public String getName() { return this.name; }
    public String getUsername() { return this.username; }
    public String getPassword() { return this.password; }
    public boolean checkPassword(String password) { return this.password.equals(password); }
    public Role getRole() { return this.role; }
    public boolean isActive() { return this.active; }
    public LocalDateTime getCreatedAt() { return this.createdAt; }
    @Override
    public String toString() { return this.name; }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User that = (User) o;
        return this.id == that.id;
    }
    @Override
    public int hashCode() { return Objects.hash(id); }
}
