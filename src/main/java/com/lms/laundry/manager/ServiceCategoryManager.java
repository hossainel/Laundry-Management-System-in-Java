package com.lms.laundry.manager;

import com.lms.laundry.model.ServiceCategory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ServiceCategoryManager {
    private static final String BASE_SELECT = "SELECT * FROM fx_service_categories WHERE ";
    private static final String BASE_UPDATE = "UPDATE fx_service_categories SET ";
    private static final String BASE_WHERE = " WHERE id=?";
    private static final String BASE_INSERT = "INSERT INTO fx_service_categories ";

    public static ServiceCategory getById(int id) {
        String sql = BASE_SELECT+"id=? LIMIT 1";
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapServiceCategory(rs);
            }
        } catch (Exception e) {
            P.pf("Error fetching service category: " + id);
        }
        return null;
    }

    public static List<ServiceCategory> getAll() {
        List<ServiceCategory> list = new ArrayList<>();
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(BASE_SELECT+"1=1");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapServiceCategory(rs));
            }
        } catch (Exception e) {
            P.pf("Error fetching all categories");
        }
        return list;
    }

    public static boolean create(ServiceCategory service_category) {
        String sql = BASE_INSERT+"(name, is_active) VALUES (?,?)";
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, service_category.getName());
            ps.setInt(2, service_category.isActive() ? 1 : 0);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            P.pf("Error creating service category: " + e.getMessage());
        }
        return false;
    }

    public static boolean update(ServiceCategory service_category) {
        String sql = BASE_UPDATE+"name=?, is_active=?"+BASE_WHERE;
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, service_category.getName());
            ps.setInt(2, service_category.isActive() ? 1 : 0);
            ps.setInt(3, service_category.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            P.pf("Error updating service category: " + service_category.getName());
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
            P.pf("Error disabling service category: " + id);
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
            P.pf("Error enabling service category: " + id);
        }
        return false;
    }

    public static boolean delete(int id) {
        String sql = "DELETE FROM fx_service_categories"+BASE_WHERE;
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            P.pf("Error deleting service category " + id);
        }
        return false;
    }

    private static ServiceCategory mapServiceCategory(ResultSet rs) throws Exception {
        return new ServiceCategory(
                rs.getInt("id"),
                rs.getString("name"),
                (rs.getInt("is_active") == 1)
        );
    }
}
