package com.lms.laundry.manager;

import com.lms.laundry.model.PaymentMethod;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class PaymentMethodManager {
    private static final String BASE_SELECT = "SELECT * FROM fx_payment_methods WHERE ";
    private static final String BASE_UPDATE = "UPDATE fx_payment_methods SET ";
    private static final String BASE_WHERE = " WHERE id=?";
    private static final String BASE_INSERT = "INSERT INTO fx_payment_methods ";

    public static PaymentMethod getById(int id) {
        String sql = BASE_SELECT+"id=? LIMIT 1";
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapPaymentMethod(rs);
            }
        } catch (Exception e) {
            P.pf("Error fetching payment method: " + id);
        }
        return null;
    }

    public static List<PaymentMethod> getAll() {
        List<PaymentMethod> list = new ArrayList<>();
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(BASE_SELECT+"1=1");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapPaymentMethod(rs));
            }
        } catch (Exception e) {
            P.pf("Error fetching all users");
        }
        return list;
    }

    public static boolean create(PaymentMethod product_category) {
        String sql = BASE_INSERT+"(name, is_active) VALUES (?,?)";
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product_category.getName());
            ps.setInt(2, product_category.isActive() ? 1 : 0);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            P.pf("Error creating payment method: " + e.getMessage());
        }
        return false;
    }

    public static boolean update(PaymentMethod product_category) {
        String sql = BASE_UPDATE+"name=?, is_active=?"+BASE_WHERE;
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product_category.getName());
            ps.setInt(2, product_category.isActive() ? 1 : 0);
            ps.setInt(3, product_category.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            P.pf("Error updating payment method: " + product_category.getName());
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
            P.pf("Error disabling payment method: " + id);
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
            P.pf("Error enabling payment method: " + id);
        }
        return false;
    }

    public static boolean delete(int id) {
        String sql = "DELETE FROM fx_payment_methods"+BASE_WHERE;
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            P.pf("Error deleting payment method " + id);
        }
        return false;
    }

    private static PaymentMethod mapPaymentMethod(ResultSet rs) throws Exception {
        return new PaymentMethod(
                rs.getInt("id"),
                rs.getString("name"),
                (rs.getInt("is_active") == 1)
        );
    }
}
