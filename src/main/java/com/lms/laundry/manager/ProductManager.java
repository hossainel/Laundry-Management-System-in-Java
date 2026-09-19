package com.lms.laundry.manager;

import com.lms.laundry.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductManager {
    private static final String BASE_SELECT = "SELECT fx_products.*, fx_product_categories.name" +
            " AS category_name FROM fx_products LEFT JOIN fx_product_categories" +
            " ON fx_product_categories.id=fx_products.category_id WHERE ";
    private static final String BASE_UPDATE = "UPDATE fx_products SET ";
    private static final String BASE_WHERE = " WHERE id=?";
    private static final String BASE_INSERT = "INSERT INTO fx_products ";

    public static Product getById(int id) {
        String sql = BASE_SELECT + "fx_products.id=? LIMIT 1";
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapProduct(rs);
            }
        } catch (Exception e) {
            P.pf("Error fetching product ID " + id + ": " + e.getMessage());
        }
        return null;
    }

    public static List<Product> getAll() {
        List<Product> list = new ArrayList<>();
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(BASE_SELECT + "1=1");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapProduct(rs));
            }
        } catch (Exception e) {
            P.pf("Error fetching all products: " + e.getMessage());
        }
        return list;
    }

    public static boolean create(Product product) {
        String sql = BASE_INSERT + "(image, name, barcode, category_id, description, cost_price, " +
                "price, stock, is_active) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            editUpdate(product, ps);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            P.pf("Error creating product: " + e.getMessage());
        }
        return false;
    }

    public static boolean update(Product product) {
        String sql = BASE_UPDATE + "image=?, name=?, barcode=?, category_id=?, description=?," +
                " cost_price=?, price=?, stock=?, is_active=?" + BASE_WHERE;
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            editUpdate(product, ps);
            ps.setInt(10, product.getId());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            P.pf("Error updating product " + product.getName() + ": " + e.getMessage());
        }
        return false;
    }

    private static void editUpdate(Product product, PreparedStatement ps) throws SQLException {
        ps.setString(1, product.getImage());
        ps.setString(2, product.getName());
        ps.setString(3, product.getBarcode());
        ps.setInt(4, product.getProductCategory() != null ? product.getProductCategory().getId() : product.getCategoryId());
        ps.setString(5, product.getDescription());
        ps.setDouble(6, product.getCostPrice());
        ps.setDouble(7, product.getPrice());
        ps.setInt(8, product.getStock());
        ps.setInt(9, product.isActive() ? 1 : 0);
    }

    public static boolean disable(int id) {
        String sql = BASE_UPDATE+"is_active=0"+BASE_WHERE;
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            P.pf("Error disabling product: " + id);
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
            P.pf("Error enabling product: " + id);
        }
        return false;
    }

    public static boolean delete(int id) {
        String sql = "DELETE FROM fx_products" + BASE_WHERE;
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            P.pf("Error deleting product ID " + id + ": " + e.getMessage());
        }
        return false;
    }

    private static Product mapProduct(ResultSet rs) throws Exception {
        Timestamp dbCreatedAt = rs.getTimestamp("created_at");
        Timestamp dbUpdatedAt = rs.getTimestamp("updated_at");

        return new Product(
                rs.getInt("id"),
                rs.getString("image"),
                rs.getString("name"),
                rs.getString("barcode"),
                rs.getInt("category_id"),
                rs.getString("category_name"),
                rs.getString("description"),
                rs.getDouble("cost_price"),
                rs.getDouble("price"),
                rs.getInt("stock"),
                rs.getBoolean("is_active"),
                dbCreatedAt != null ? dbCreatedAt.toLocalDateTime() : null,
                dbUpdatedAt != null ? dbUpdatedAt.toLocalDateTime() : null
        );
    }
}
