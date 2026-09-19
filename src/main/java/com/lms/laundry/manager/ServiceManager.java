package com.lms.laundry.manager;

import com.lms.laundry.model.Service;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceManager {
    private static final String BASE_SELECT =
            "SELECT s.id, s.category_id, s.image, s.name, s.is_active, " +
                    "c.name AS category_name, " +
                    "GROUP_CONCAT(t.id ORDER BY t.id) AS type_ids_str, " +
                    "GROUP_CONCAT(t.name ORDER BY t.id SEPARATOR '??') AS type_names_str, " +
                    "GROUP_CONCAT(p.price ORDER BY t.id) AS prices_str " +
                    "FROM fx_services s " +
                    "INNER JOIN fx_service_categories c ON s.category_id = c.id " +
                    "LEFT JOIN fx_service_prices p ON s.id = p.service_id " +
                    "LEFT JOIN fx_service_types t ON p.type_id = t.id " +
                    "WHERE ";

    private static final String BASE_GROUP_BY = " GROUP BY s.id, c.id";
    private static final String BASE_UPDATE = "UPDATE fx_services SET ";
    private static final String BASE_WHERE = " WHERE id=?";
    private static final String BASE_INSERT = "INSERT INTO fx_services ";

    public static Service getById(int id) {
        String sql = BASE_SELECT + "s.id=? " + BASE_GROUP_BY + " LIMIT 1";
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapService(rs);
            }
        } catch (Exception e) {
            P.pf("Error fetching service ID " + id + ": " + e.getMessage());
        }
        return null;
    }

    public static List<Service> getAll() {
        List<Service> list = new ArrayList<>();
        String sql = BASE_SELECT + "1=1" + BASE_GROUP_BY;
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapService(rs));
            }
        } catch (Exception e) {
            P.pf("Error fetching all services: " + e.getMessage());
        }
        return list;
    }
    public static boolean create(Service service) {
        String insertSql = BASE_INSERT + "(category_id, image, name, is_active) VALUES (?,?,?,?)";
        Connection conn = null;
        try {
            conn = MyJDBC.getConnection();
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                editUpdate(service, ps);
                int rows = ps.executeUpdate();
                if (rows == 0) {
                    conn.rollback();
                    return false;
                }

                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        service.setId(generatedKeys.getInt(1));
                    }
                }
            }
            saveServicePrices(service, conn);
            conn.commit();
            return true;
        } catch (Exception e) {
            P.pf("Error creating service: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { P.pf("Rollback failed: " + ex.getMessage()); }
            }
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { P.pf("Error in Service Type: " + ex.getMessage()); }
            }
        }
        return false;
    }

    private static void editUpdate(Service service, PreparedStatement ps) throws SQLException {
        ps.setInt(1, service.getServiceCategory() != null ? service.getServiceCategory().getId() : service.getCategoryId());
        ps.setString(2, service.getImage());
        ps.setString(3, service.getName());
        ps.setInt(4, service.isActive() ? 1 : 0);
    }

    public static boolean update(Service service) {
        String updateSql = BASE_UPDATE + "category_id=?, image=?, name=?, is_active=?" + BASE_WHERE;
        Connection conn = null;
        try {
            conn = MyJDBC.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                editUpdate(service, ps);
                ps.setInt(5, service.getId());
                ps.executeUpdate();
            }

            try (PreparedStatement delPs = conn.prepareStatement("DELETE FROM fx_service_prices WHERE service_id=?")) {
                delPs.setInt(1, service.getId());
                delPs.executeUpdate();
            }
            saveServicePrices(service, conn);
            conn.commit();
            return true;
        } catch (Exception e) {
            P.pf("Error updating service " + service.getName() + ": " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { P.pf("Rollback failed: " + ex.getMessage()); }
            }
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { /* Close quietly */ }
            }
        }
        return false;
    }
    private static void saveServicePrices(Service service, Connection conn) throws SQLException {
        if (service.getTypeIds() == null || service.getTypeIds().isEmpty()) return;
        String priceInsertSql = "INSERT INTO fx_service_prices (service_id, type_id, price) VALUES (?,?,?)";
        try (PreparedStatement pricePs = conn.prepareStatement(priceInsertSql)) {
            int size = Math.min(service.getTypeIds().size(), service.getPrices().size());
            for (int i = 0; i < size; i++) {
                pricePs.setInt(1, service.getId());
                pricePs.setInt(2, service.getTypeIds().get(i));
                pricePs.setDouble(3, service.getPrices().get(i));
                pricePs.addBatch();
            }
            pricePs.executeBatch();
        }
    }
    public static boolean disable(int id) {
        String sql = BASE_UPDATE + "is_active=0" + BASE_WHERE;
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            P.pf("Error disabling service ID " + id + ": " + e.getMessage());
        }
        return false;
    }
    public static boolean enable(int id) {
        String sql = BASE_UPDATE + "is_active=1" + BASE_WHERE;
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            P.pf("Error enabling service ID " + id + ": " + e.getMessage());
        }
        return false;
    }
    public static boolean delete(int id) {
        Connection conn = null;
        try {
            conn = MyJDBC.getConnection();
            conn.setAutoCommit(false);
            try (PreparedStatement psPrices = conn.prepareStatement("DELETE FROM fx_service_prices WHERE service_id=?")) {
                psPrices.setInt(1, id);
                psPrices.executeUpdate();
            }
            try (PreparedStatement psService = conn.prepareStatement("DELETE FROM fx_services" + BASE_WHERE)) {
                psService.setInt(1, id);
                int affectedRows = psService.executeUpdate();
                conn.commit();
                return affectedRows > 0;
            }
        } catch (Exception e) {
            P.pf("Error deleting service ID " + id + ": " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { P.pf("Rollback failed: " + ex.getMessage()); }
            }
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { /* Close quietly */ }
            }
        }
        return false;
    }
    private static Service mapService(ResultSet rs) throws Exception {
        return new Service(
                rs.getInt("id"),
                rs.getInt("category_id"),
                rs.getString("category_name"),
                rs.getString("image"),
                rs.getString("name"),
                rs.getBoolean("is_active"),
                rs.getString("type_ids_str"),
                rs.getString("type_names_str"),
                rs.getString("prices_str")
        );
    }
}
