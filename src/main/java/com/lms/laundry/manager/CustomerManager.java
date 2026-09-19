package com.lms.laundry.manager;

import com.lms.laundry.model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CustomerManager {
    private static final String BASE_VIEW = "SELECT c.*, " +
            "COALESCE((SELECT SUM(o.total) FROM fx_orders o WHERE o.customer_id=c.id), 0) AS total_orders, " +
            "COALESCE((SELECT SUM(p.amount) FROM fx_payments p WHERE p.customer_id=c.id), 0) AS total_payments " +
            "FROM fx_customers c WHERE ";
    private static final String BASE_UPDATE = "UPDATE fx_customers SET ";
    private static final String BASE_WHERE = " WHERE id=?";
    private static final String BASE_INSERT = "INSERT INTO fx_customers ";

    public static Customer getById(int id) {
        String sql = BASE_VIEW + "id=? LIMIT 1";
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapCustomer(rs);
            }
        } catch (Exception e) {
            P.pf("Error getting customer " + id + ": " + e.getMessage());
        }
        return null;
    }

    public static Customer getBy(String column, String value) {
        if (!column.matches("name|phone|email")) return null;

        String sql = BASE_VIEW + column + "=? LIMIT 1";
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapCustomer(rs);
            }
        } catch (Exception e) {
            P.pf("Error getting customer by " + column + ": " + value);
        }
        return null;
    }

    public static List<Customer> getAll() {
        List<Customer> list = new ArrayList<>();
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(BASE_VIEW+"1=1");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapCustomer(rs));
            }
        } catch (Exception e) {
            P.pf("Error getting customers: " + e.getMessage());
        }
        return list;
    }

    public static List<Customer> search(String value) {
        List<Customer> list = new ArrayList<>();
        String sql = BASE_VIEW+"name LIKE CONCAT('%', ?, '%') "
                              +"OR phone LIKE CONCAT('%', ?, '%') "
                              +"OR email LIKE CONCAT('%', ?, '%')";
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             ps.setString(1, value);
             ps.setString(2, value);
             ps.setString(3, value);
             try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapCustomer(rs));
                }
            }
        } catch (Exception e) {
            P.pf("Search error: " + e.getMessage());
        }
        return list;
    }

    public static boolean create(Customer customer) {
        String sql = BASE_INSERT+"(name, phone, email, address) VALUES (?,?,?,?)";
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customer.getName());
            ps.setString(2, customer.getPhone());
            ps.setString(3, customer.getEmail());
            ps.setString(4, customer.getAddress() == null ? "" : customer.getAddress());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            P.pf("Error creating customer: " + e.getMessage());
        }
        return false;
    }

    public static boolean update(Customer customer) {
        String sql = BASE_UPDATE+"name=?, phone=?, email=?, address=?"+BASE_WHERE;
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customer.getName());
            ps.setString(2, customer.getPhone());
            ps.setString(3, customer.getEmail());
            ps.setString(4, customer.getAddress());
            ps.setInt(5, customer.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            P.pf("Error updating customer: " + customer.getName());
        }
        return false;
    }

    public static boolean delete(int id) {
        if (id == 1) return false; // Protected Walk-in Customer
        String sql = "DELETE FROM fx_customers"+BASE_WHERE;
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            P.pf("Error deleting customer " + id);
        }
        return false;
    }

    private static Customer mapCustomer(ResultSet rs) throws Exception {
        Customer customer = new Customer(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("address")
        );
        customer.setTotalOrder(rs.getDouble("total_orders"));
        customer.setTotalPayment(rs.getDouble("total_payments"));
        return customer;
    }
}
