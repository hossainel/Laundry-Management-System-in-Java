package com.lms.laundry.manager;

import com.lms.laundry.model.Payment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentManager {
    private static final String BASE_SELECT = "SELECT fx_payments.*, fx_payment_methods.name" +
            " AS method_name, fx_customers.name AS customer_name  FROM fx_payments" +
            " LEFT JOIN fx_payment_methods ON fx_payment_methods.id=fx_payments.method_id" +
            " LEFT JOIN fx_customers ON fx_customers.id=fx_payments.customer_id WHERE ";
    private static final String BASE_UPDATE = "UPDATE fx_payments SET ";
    private static final String BASE_WHERE = " WHERE id=?";
    private static final String BASE_INSERT = "INSERT INTO fx_payments ";

    public static Payment getById(int id) {
        String sql = BASE_SELECT + "fx_payments.id=? LIMIT 1";
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapPayment(rs);
            }
        } catch (Exception e) {
            P.pf("Error fetching payment ID " + id + ": " + e.getMessage());
        }
        return null;
    }

    public static List<Payment> getAll() {
        List<Payment> list = new ArrayList<>();
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(BASE_SELECT + "1=1");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapPayment(rs));
            }
        } catch (Exception e) {
            P.pf("Error fetching all payments: " + e.getMessage());
        }
        return list;
    }

    public static boolean create(Payment payment) {
        String sql = BASE_INSERT + "(customer_id, amount, method_id, payment_date) VALUES (?,?,?,?)";
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            editUpdate(payment, ps);
            boolean success = ps.executeUpdate() > 0;
            if (success) {
                syncCustomerOrderPayments(payment.getCustomer() != null ? payment.getCustomer().getId() : payment.getCustomerId());
            }
            return success;
        } catch (Exception e) {
            P.pf("Error creating payment: " + e.getMessage());
        }
        return false;
    }

    public static boolean update(Payment payment) {
        String sql = BASE_UPDATE + "customer_id=?, amount=?, method_id=?, payment_date=?" + BASE_WHERE;
        Payment oldPayment = getById(payment.getId());
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            editUpdate(payment, ps);
            ps.setInt(5, payment.getId());

            boolean success = ps.executeUpdate() > 0;
            if (success) {
                if (oldPayment != null && oldPayment.getCustomerId() != payment.getCustomerId()) {
                    syncCustomerOrderPayments(oldPayment.getCustomerId());
                }
                syncCustomerOrderPayments(payment.getCustomer() != null ? payment.getCustomer().getId() : payment.getCustomerId());
            }
            return success;
        } catch (Exception e) {
            P.pf("Error updating payment " + payment.getId() + ": " + e.getMessage());
        }
        return false;
    }

    private static void editUpdate(Payment payment, PreparedStatement ps) throws SQLException {
        ps.setInt(1, payment.getCustomer() != null
                ? payment.getCustomer().getId() : payment.getCustomerId());
        ps.setDouble(2, payment.getAmount());
        ps.setInt(3, payment.getPaymentMethod() != null
                ? payment.getPaymentMethod().getId() : payment.getMethodId());
        ps.setDate(4, payment.getPaymentDate() != null
                ? Date.valueOf(payment.getPaymentDate()) : null);
    }

    public static boolean delete(int id) {
        String sql = "DELETE FROM fx_payments" + BASE_WHERE;
        Payment oldPayment = getById(id);
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            boolean success = ps.executeUpdate() > 0;
            if (success && oldPayment != null) {
                syncCustomerOrderPayments(oldPayment.getCustomerId());
            }
            return success;
        } catch (Exception e) {
            P.pf("Error deleting payment ID " + id + ": " + e.getMessage());
        }
        return false;
    }

    public static void syncCustomerOrderPayments(int customerId) {
        String sumSql = "SELECT COALESCE(SUM(amount),0) FROM fx_payments WHERE customer_id=?";
        String ordersSql = "SELECT id, total FROM fx_orders WHERE customer_id=? ORDER BY order_date, id";
        String updateSql = "UPDATE fx_orders SET paid_amount=?, due_amount=? WHERE id=?";
        Connection conn = null;
        try {
            conn = MyJDBC.getConnection();
            conn.setAutoCommit(false);
            double remainingPayment = 0;
            try (PreparedStatement ps = conn.prepareStatement(sumSql)) {
                ps.setInt(1, customerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) remainingPayment = rs.getDouble(1);
                }
            }
            try (PreparedStatement orders = conn.prepareStatement(ordersSql);
                 PreparedStatement update = conn.prepareStatement(updateSql)) {
                orders.setInt(1, customerId);
                try (ResultSet rs = orders.executeQuery()) {
                    while (rs.next()) {
                        int orderId = rs.getInt("id");
                        double total = rs.getDouble("total");
                        double paid = Math.min(total, Math.max(0, remainingPayment));
                        double due = Math.max(0, total - paid);
                        remainingPayment -= paid;
                        update.setDouble(1, paid);
                        update.setDouble(2, due);
                        update.setInt(3, orderId);
                        update.addBatch();
                    }
                }
                update.executeBatch();
            }
            conn.commit();
        } catch (Exception e) {
            P.pf("Error syncing customer payments " + customerId + ": " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { P.pf("Payment sync rollback failed: " + ex.getMessage()); }
            }
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    private static Payment mapPayment(ResultSet rs) throws Exception {
        java.sql.Date dbDate = rs.getDate("payment_date");
        Timestamp dbCreatedAt = rs.getTimestamp("created_at");
        Timestamp dbUpdatedAt = rs.getTimestamp("updated_at");

        return new Payment(
                rs.getInt("id"),
                rs.getInt("method_id"),
                rs.getString("method_name"),
                rs.getDouble("amount"),
                rs.getInt("customer_id"),
                rs.getString("customer_name"),
                dbDate != null ? dbDate.toLocalDate() : null,
                dbCreatedAt != null ? dbCreatedAt.toLocalDateTime() : null,
                dbUpdatedAt != null ? dbUpdatedAt.toLocalDateTime() : null
        );
    }
}
