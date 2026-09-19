package com.lms.laundry.manager;

import com.lms.laundry.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UserManager {
    private static final String BASE_SELECT = "SELECT * FROM fx_users WHERE ";
    private static final String BASE_UPDATE = "UPDATE fx_users SET ";
    private static final String BASE_WHERE = " WHERE id=?";
    private static final String BASE_INSERT = "INSERT INTO fx_users ";
    public static User login(String username, String password) {
        String sql = BASE_SELECT+"username=? AND password=? AND is_active=1";
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapUser(rs);
            }
        } catch (Exception e) {
            P.pf("Error during login for: " + username);
        }
        return null;
    }

    public static User getById(int id) {
        String sql = BASE_SELECT+"id=? LIMIT 1";
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapUser(rs);
            }
        } catch (Exception e) {
            P.pf("Error fetching user ID: " + id);
        }
        return null;
    }

    public static User getByUsername(String username) {
        String sql = BASE_SELECT+"username=? LIMIT 1";
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapUser(rs);
            }
        } catch (Exception e) {
            P.pf("Error fetching user username: " + username);
        }
        return null;
    }

    public static List<User> getAll() {
        List<User> list = new ArrayList<>();
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(BASE_SELECT+"1=1");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapUser(rs));
            }
        } catch (Exception e) {
            P.pf("Error fetching all users");
        }
        return list;
    }

    public static boolean create(User user) {
        String sql = BASE_INSERT+"(name, username, password, role, is_active) VALUES (?,?,?,?,?)";
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getRole().name());
            ps.setInt(5, user.isActive() ? 1 : 0);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            P.pf("Error creating user: " + e.getMessage());
        }
        return false;
    }

    public static boolean update(User user) {
        String sql = BASE_UPDATE+"name=?, username=?, role=?, is_active=?"+BASE_WHERE;
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getRole().name());
            ps.setInt(4, user.isActive() ? 1 : 0);
            ps.setInt(5, user.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            P.pf("Error updating user: " + user.getUsername());
        }
        return false;
    }

    public static boolean updatePassword(int userId, String newPassword) {
        String sql = BASE_UPDATE+"password=?"+BASE_WHERE;
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            P.pf("Error updating password for ID: " + userId);
        }
        return false;
    }

    public static boolean disable(int id) {
        String sql = BASE_UPDATE+"is_active=0"+BASE_WHERE;
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            P.pf("Error disabling user: " + id);
        }
        return false;
    }
    public static boolean enable(int id) {
        String sql = BASE_UPDATE+"is_active=1"+BASE_WHERE;
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            P.pf("Error enabling user: " + id);
        }
        return false;
    }

    public static boolean delete(int id) {
        if (id == 1) return false; // Protected System User
        String sql = "DELETE FROM fx_users"+BASE_WHERE;
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            P.pf("Error deleting user " + id);
        }
        return false;
    }

    private static User mapUser(ResultSet rs) throws Exception {
        return new User(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("username"),
                rs.getString("password"),
                User.Role.valueOf(rs.getString("role")),
                (rs.getInt("is_active") == 1)
        );
    }
}
