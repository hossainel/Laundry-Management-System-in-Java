package com.lms.laundry.manager;

import com.lms.laundry.model.Order;
import com.lms.laundry.model.OrderItem;
import com.lms.laundry.model.Payment;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrderManager {
    private static final String BASE_SELECT =
            "SELECT o.*, c.name AS customer_name FROM fx_orders o " +
            "LEFT JOIN fx_customers c ON c.id=o.customer_id WHERE ";

    public static Order getById(int id) {
        String sql = BASE_SELECT + "o.id=? LIMIT 1";
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order order = mapOrder(rs);
                    order.setItems(getItems(order.getId(), conn));
                    return order;
                }
            }
        } catch (Exception e) {
            P.pf("Error fetching order ID " + id + ": " + e.getMessage());
        }
        return null;
    }

    public static List<Order> getAll() {
        List<Order> orders = new ArrayList<>();
        String sql = BASE_SELECT + "1=1 ORDER BY o.id DESC";
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                orders.add(mapOrder(rs));
            }
        } catch (Exception e) {
            P.pf("Error fetching orders: " + e.getMessage());
        }
        return orders;
    }

    public static boolean create(Order order, Payment payment) {
        return create(order, payment, true);
    }

    public static boolean create(Order order, Payment payment, boolean adjustPreviousAdvance) {
        String sql = "INSERT INTO fx_orders (customer_id, delivery_date, status, subtotal, discount, total, paid_amount, due_amount) " +
                "VALUES (?,?,?,?,?,?,?,?)";
        Connection conn = null;
        try {
            conn = MyJDBC.getConnection();
            conn.setAutoCommit(false);
            order.calculateTotals();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                bindOrder(order, ps);
                if (ps.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) order.setId(keys.getInt(1));
                }
            }
            saveItems(order, conn);
            if (payment != null && payment.getAmount() > 0) {
                insertPayment(payment, conn);
            }
            conn.commit();
            if (payment != null && payment.getAmount() > 0 && adjustPreviousAdvance) {
                PaymentManager.syncCustomerOrderPayments(order.getCustomerId());
            }
            return true;
        } catch (Exception e) {
            P.pf("Error creating order: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { P.pf("Rollback failed: " + ex.getMessage()); }
            }
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
        return false;
    }

    public static boolean update(Order order) {
        String sql = "UPDATE fx_orders SET customer_id=?, delivery_date=?, status=?, subtotal=?, discount=?, total=?, paid_amount=?, due_amount=? WHERE id=?";
        Order oldOrder = getById(order.getId());
        Connection conn = null;
        try {
            conn = MyJDBC.getConnection();
            conn.setAutoCommit(false);
            order.calculateTotals();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                bindOrder(order, ps);
                ps.setInt(9, order.getId());
                if (ps.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }
            }
            try (PreparedStatement del = conn.prepareStatement("DELETE FROM fx_order_items WHERE order_id=?")) {
                del.setInt(1, order.getId());
                del.executeUpdate();
            }
            saveItems(order, conn);
            conn.commit();
            if (oldOrder != null && oldOrder.getCustomerId() != order.getCustomerId()) {
                PaymentManager.syncCustomerOrderPayments(oldOrder.getCustomerId());
            }
            PaymentManager.syncCustomerOrderPayments(order.getCustomerId());
            return true;
        } catch (Exception e) {
            P.pf("Error updating order " + order.getId() + ": " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { P.pf("Rollback failed: " + ex.getMessage()); }
            }
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
        return false;
    }

    public static boolean updateStatus(int id, Order.Status status) {
        String sql = "UPDATE fx_orders SET status=? WHERE id=?";
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            P.pf("Error updating order status " + id + ": " + e.getMessage());
        }
        return false;
    }

    public static boolean delete(int id) {
        Connection conn = null;
        try {
            conn = MyJDBC.getConnection();
            conn.setAutoCommit(false);
            try (PreparedStatement items = conn.prepareStatement("DELETE FROM fx_order_items WHERE order_id=?")) {
                items.setInt(1, id);
                items.executeUpdate();
            }
            try (PreparedStatement order = conn.prepareStatement("DELETE FROM fx_orders WHERE id=?")) {
                order.setInt(1, id);
                int affected = order.executeUpdate();
                conn.commit();
                return affected > 0;
            }
        } catch (Exception e) {
            P.pf("Error deleting order " + id + ": " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { P.pf("Rollback failed: " + ex.getMessage()); }
            }
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
        return false;
    }

    public static List<OrderItem> getItems(int orderId) {
        try (Connection conn = MyJDBC.getConnection()) {
            return getItems(orderId, conn);
        } catch (Exception e) {
            P.pf("Error fetching order items: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    private static List<OrderItem> getItems(int orderId, Connection conn) throws SQLException {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT * FROM fx_order_items WHERE order_id=? ORDER BY id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new OrderItem(
                            rs.getInt("id"),
                            rs.getInt("order_id"),
                            OrderItem.ItemType.valueOf(rs.getString("item_type")),
                            rs.getInt("item_id"),
                            rs.getString("item_name"),
                            rs.getDouble("unit_price"),
                            rs.getInt("quantity"),
                            rs.getDouble("total")
                    ));
                }
            }
        }
        return items;
    }

    private static void saveItems(Order order, Connection conn) throws SQLException {
        String sql = "INSERT INTO fx_order_items (order_id, item_type, item_id, item_name, unit_price, quantity, total) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (OrderItem item : order.getItems()) {
                ps.setInt(1, order.getId());
                ps.setString(2, item.getItemType().name());
                ps.setInt(3, item.getItemId());
                ps.setString(4, item.getItemName());
                ps.setDouble(5, item.getUnitPrice());
                ps.setInt(6, item.getQuantity());
                ps.setDouble(7, item.getTotal());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void insertPayment(Payment payment, Connection conn) throws SQLException {
        String sql = "INSERT INTO fx_payments (customer_id, amount, method_id, payment_date) VALUES (?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, payment.getCustomer() != null ? payment.getCustomer().getId() : payment.getCustomerId());
            ps.setDouble(2, payment.getAmount());
            ps.setInt(3, payment.getPaymentMethod() != null ? payment.getPaymentMethod().getId() : payment.getMethodId());
            LocalDate paymentDate = payment.getPaymentDate() != null ? payment.getPaymentDate() : LocalDate.now();
            ps.setDate(4, Date.valueOf(paymentDate));
            ps.executeUpdate();
        }
    }

    private static void bindOrder(Order order, PreparedStatement ps) throws SQLException {
        if (order.getItems() != null && order.getItems().stream()
                .noneMatch(item -> item.getItemType() == OrderItem.ItemType.SERVICE)) {
            order.setStatus(Order.Status.DELIVERED);
        }
        ps.setInt(1, order.getCustomer() != null ? order.getCustomer().getId() : order.getCustomerId());
        ps.setDate(2, order.getDeliveryDate() != null ? Date.valueOf(order.getDeliveryDate()) : null);
        ps.setString(3, order.getStatus().name());
        ps.setDouble(4, order.getSubtotal());
        ps.setDouble(5, order.getDiscount());
        ps.setDouble(6, order.getTotal());
        ps.setDouble(7, order.getPaidAmount());
        ps.setDouble(8, order.getDueAmount());
    }

    private static Order mapOrder(ResultSet rs) throws SQLException {
        Timestamp orderDate = rs.getTimestamp("order_date");
        Date deliveryDate = rs.getDate("delivery_date");
        return new Order(
                rs.getInt("id"),
                rs.getInt("customer_id"),
                rs.getString("customer_name"),
                orderDate != null ? orderDate.toLocalDateTime() : null,
                deliveryDate != null ? deliveryDate.toLocalDate() : null,
                Order.Status.valueOf(rs.getString("status")),
                rs.getDouble("subtotal"),
                rs.getDouble("discount"),
                rs.getDouble("total"),
                rs.getDouble("paid_amount"),
                rs.getDouble("due_amount")
        );
    }
}
