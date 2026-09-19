package com.lms.laundry.manager;

import com.lms.laundry.model.ProductCategory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ProductCategoryManager {
    private static final String BASE_SELECT = "SELECT * FROM fx_product_categories WHERE ";
    private static final String BASE_UPDATE = "UPDATE fx_product_categories SET ";
    private static final String BASE_WHERE = " WHERE id=?";
    private static final String BASE_INSERT = "INSERT INTO fx_product_categories ";

    public static ProductCategory getById(int id) {
        String sql = BASE_SELECT+"id=? LIMIT 1";
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapProductCategory(rs);
            }
        } catch (Exception e) {
            P.pf("Error fetching product category: " + id);
        }
        return null;
    }

    public static List<ProductCategory> getAll() {
        List<ProductCategory> list = new ArrayList<>();
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(BASE_SELECT+"1=1");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapProductCategory(rs));
            }
        } catch (Exception e) {
            P.pf("Error fetching all users");
        }
        return list;
    }

    public static boolean create(ProductCategory product_category) {
        String sql = BASE_INSERT+"(name, is_active) VALUES (?,?)";
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product_category.getName());
            ps.setInt(2, product_category.isActive() ? 1 : 0);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            P.pf("Error creating product category: " + e.getMessage());
        }
        return false;
    }

    public static boolean update(ProductCategory product_category) {
        String sql = BASE_UPDATE+"name=?, is_active=?"+BASE_WHERE;
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product_category.getName());
            ps.setInt(2, product_category.isActive() ? 1 : 0);
            ps.setInt(3, product_category.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            P.pf("Error updating product category: " + product_category.getName());
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
            P.pf("Error disabling product category: " + id);
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
            P.pf("Error enabling product category: " + id);
        }
        return false;
    }

    public static boolean delete(int id) {
        String sql = "DELETE FROM fx_product_categories"+BASE_WHERE;
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            P.pf("Error deleting product category " + id);
        }
        return false;
    }

    private static ProductCategory mapProductCategory(ResultSet rs) throws Exception {
        return new ProductCategory(
                rs.getInt("id"),
                rs.getString("name"),
                (rs.getInt("is_active") == 1)
        );
    }
}
