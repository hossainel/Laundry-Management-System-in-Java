package com.lms.laundry.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DashboardManager {
    public static class DashboardSummary {
        private int totalOrders;
        private int totalCustomers;
        private double todaySales;
        private double totalDue;
        private int receivedOrders;
        private int washingOrders;
        private int readyOrders;
        private int deliveredOrders;
        private final List<String> activity = new ArrayList<>();

        public int getTotalOrders() { return totalOrders; }
        public int getTotalCustomers() { return totalCustomers; }
        public double getTodaySales() { return todaySales; }
        public double getTotalDue() { return totalDue; }
        public int getReceivedOrders() { return receivedOrders; }
        public int getWashingOrders() { return washingOrders; }
        public int getReadyOrders() { return readyOrders; }
        public int getDeliveredOrders() { return deliveredOrders; }
        public List<String> getActivity() { return activity; }
    }

    public static DashboardSummary getSummary() {
        DashboardSummary summary = new DashboardSummary();
        try (Connection conn = MyJDBC.getConnection()) {
            summary.totalOrders = intValue(conn, "SELECT COUNT(*) FROM fx_orders");
            summary.totalCustomers = intValue(conn, "SELECT COUNT(*) FROM fx_customers");
            summary.todaySales = doubleValue(conn, "SELECT COALESCE(SUM(total),0) FROM fx_orders WHERE DATE(order_date)=CURDATE()");
            summary.totalDue = doubleValue(conn, "SELECT COALESCE(SUM(due_amount),0) FROM fx_orders");
            loadStatusCounts(conn, summary);
            loadActivity(conn, summary);
        } catch (Exception e) {
            P.pf("Dashboard summary failed: " + e.getMessage());
        }
        return summary;
    }

    private static int intValue(Connection conn, String sql) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private static double doubleValue(Connection conn, String sql) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0;
        }
    }

    private static void loadStatusCounts(Connection conn, DashboardSummary summary) throws Exception {
        String sql = "SELECT status, COUNT(*) AS total FROM fx_orders GROUP BY status";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                switch (rs.getString("status")) {
                    case "RECEIVED" -> summary.receivedOrders = rs.getInt("total");
                    case "WASHING" -> summary.washingOrders = rs.getInt("total");
                    case "READY" -> summary.readyOrders = rs.getInt("total");
                    case "DELIVERED" -> summary.deliveredOrders = rs.getInt("total");
                    default -> { }
                }
            }
        }
    }

    private static void loadActivity(Connection conn, DashboardSummary summary) throws Exception {
        String sql = "SELECT o.id, c.name, o.status, o.total, o.order_date FROM fx_orders o " +
                "LEFT JOIN fx_customers c ON c.id=o.customer_id ORDER BY o.id DESC LIMIT 8";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                summary.activity.add("Order #" + rs.getInt("id") + " - " + rs.getString("name")
                        + " - " + rs.getString("status") + " - " + String.format("%.2f", rs.getDouble("total")));
            }
        }
        if (summary.activity.isEmpty()) {
            summary.activity.add("No orders recorded yet.");
            summary.activity.add("Today is " + LocalDate.now() + ".");
        }
    }
}
