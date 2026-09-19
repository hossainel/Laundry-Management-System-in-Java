package com.lms.laundry.manager;

import com.lms.laundry.model.ServiceType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ServiceTypeManager {
    private static final String BASE_SELECT = "SELECT * FROM fx_service_types WHERE ";
    private static final String BASE_UPDATE = "UPDATE fx_service_types SET ";
    private static final String BASE_WHERE = " WHERE id=?";
    private static final String BASE_INSERT = "INSERT INTO fx_service_types ";

    public static ServiceType getById(int id) {
        String sql = BASE_SELECT+"id=? LIMIT 1";
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapServiceType(rs);
            }
        } catch (Exception e) {
            P.pf("Error fetching service types: " + id);
        }
        return null;
    }

    public static List<ServiceType> getAll() {
        List<ServiceType> list = new ArrayList<>();
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(BASE_SELECT+"1=1");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapServiceType(rs));
            }
        } catch (Exception e) {
            P.pf("Error fetching all types");
        }
        return list;
    }

    public static boolean create(ServiceType service_category) {
        String sql = BASE_INSERT+"(name, is_active) VALUES (?,?)";
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, service_category.getName());
            ps.setInt(2, service_category.isActive() ? 1 : 0);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            P.pf("Error creating service types: " + e.getMessage());
        }
        return false;
    }

    public static boolean update(ServiceType service_category) {
        String sql = BASE_UPDATE+"name=?, is_active=?"+BASE_WHERE;
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, service_category.getName());
            ps.setInt(2, service_category.isActive() ? 1 : 0);
            ps.setInt(3, service_category.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            P.pf("Error updating service types: " + service_category.getName());
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
            P.pf("Error disabling service types: " + id);
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
            P.pf("Error enabling service types: " + id);
        }
        return false;
    }

    public static boolean delete(int id) {
        String sql = "DELETE FROM fx_service_types"+BASE_WHERE;
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            P.pf("Error deleting service types " + id);
        }
        return false;
    }

    private static ServiceType mapServiceType(ResultSet rs) throws Exception {
        return new ServiceType(
                rs.getInt("id"),
                rs.getString("name"),
                (rs.getInt("is_active") == 1)
        );
    }
}
