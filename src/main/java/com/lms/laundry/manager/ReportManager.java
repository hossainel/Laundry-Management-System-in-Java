package com.lms.laundry.manager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class ReportManager {
    public static class ReportRow {
        private final String label;
        private final int count;
        private final double amount;

        public ReportRow(String label, int count, double amount) {
            this.label = label;
            this.count = count;
            this.amount = amount;
        }

        public String getLabel() { return label; }
        public int getCount() { return count; }
        public double getAmount() { return amount; }
    }

    public static class LedgerRow {
        private final LocalDate date;
        private final String type;
        private final String reference;
        private final double debit;
        private final double credit;
        private final double balance;

        public LedgerRow(LocalDate date, String type, String reference, double debit, double credit, double balance) {
            this.date = date;
            this.type = type;
            this.reference = reference;
            this.debit = debit;
            this.credit = credit;
            this.balance = balance;
        }

        public LocalDate getDate() { return date; }
        public String getType() { return type; }
        public String getReference() { return reference; }
        public double getDebit() { return debit; }
        public double getCredit() { return credit; }
        public double getBalance() { return balance; }
    }

    public static ObservableList<ReportRow> getDailySummary(LocalDate from, LocalDate to) {
        String sql = "SELECT DATE(o.order_date) AS label, COUNT(*) AS row_count, SUM(o.total) AS amount " +
                "FROM fx_orders o WHERE DATE(o.order_date) BETWEEN ? AND ? GROUP BY DATE(o.order_date) ORDER BY DATE(o.order_date)";
        return rows(sql, from, to);
    }

    public static ObservableList<ReportRow> getOrderStatusSummary(LocalDate from, LocalDate to) {
        String sql = "SELECT o.status AS label, COUNT(*) AS row_count, SUM(o.total) AS amount " +
                "FROM fx_orders o WHERE DATE(o.order_date) BETWEEN ? AND ? GROUP BY o.status ORDER BY o.status";
        return rows(sql, from, to);
    }

    public static ObservableList<ReportRow> getSalesSummary(LocalDate from, LocalDate to) {
        String sql = "SELECT i.item_name AS label, SUM(i.quantity) AS row_count, SUM(i.total) AS amount " +
                "FROM fx_order_items i JOIN fx_orders o ON o.id=i.order_id " +
                "WHERE DATE(o.order_date) BETWEEN ? AND ? GROUP BY i.item_name ORDER BY amount DESC";
        ObservableList<ReportRow> list = rows(sql, from, to);
        if (!list.isEmpty()) return list;

        String fallbackSql = "SELECT DATE(o.order_date) AS label, COUNT(*) AS row_count, SUM(o.total) AS amount " +
                "FROM fx_orders o WHERE DATE(o.order_date) BETWEEN ? AND ? GROUP BY DATE(o.order_date) ORDER BY DATE(o.order_date)";
        list = rows(fallbackSql, from, to);
        if (!list.isEmpty()) return list;

        return rowsWithoutDates(
                "SELECT i.item_name AS label, SUM(i.quantity) AS row_count, SUM(i.total) AS amount " +
                        "FROM fx_order_items i GROUP BY i.item_name ORDER BY amount DESC"
        );
    }

    public static ObservableList<LedgerRow> getCustomerLedger(int customerId) {
        ObservableList<LedgerRow> list = FXCollections.observableArrayList();
        String sql = "SELECT DATE(o.order_date) AS row_date, 'ORDER' AS row_type, CONCAT('Order #', o.id) AS ref, o.total AS debit, 0 AS credit " +
                "FROM fx_orders o WHERE o.customer_id=? " +
                "UNION ALL " +
                "SELECT DATE(p.payment_date) AS row_date, 'PAYMENT' AS row_type, CONCAT('Payment #', p.id) AS ref, 0 AS debit, p.amount AS credit " +
                "FROM fx_payments p WHERE p.customer_id=? " +
                "ORDER BY row_date, ref";
        double runningBalance = 0;
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.setInt(2, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double debit = rs.getDouble("debit");
                    double credit = rs.getDouble("credit");
                    runningBalance += debit - credit;
                    java.sql.Date rowDate = rs.getDate("row_date");
                    list.add(new LedgerRow(
                            rowDate != null ? rowDate.toLocalDate() : null,
                            rs.getString("row_type"),
                            rs.getString("ref"),
                            debit,
                            credit,
                            runningBalance
                    ));
                }
            }
        } catch (Exception e) {
            P.pf("Customer ledger failed: " + e.getMessage());
        }
        return list;
    }

    public static ObservableList<ReportRow> getTaxSummary(LocalDate from, LocalDate to) {
        String sql = "SELECT 'Taxable Sales' AS label, COUNT(*) AS row_count, SUM(o.total) AS amount " +
                "FROM fx_orders o WHERE DATE(o.order_date) BETWEEN ? AND ?";
        return rows(sql, from, to);
    }

    public static ObservableList<ReportRow> getTrialBalance(LocalDate from, LocalDate to) {
        ObservableList<ReportRow> list = FXCollections.observableArrayList();
        list.add(singleRow("Orders", "SELECT COUNT(*) AS row_count, SUM(o.total) AS amount FROM fx_orders o WHERE DATE(o.order_date) BETWEEN ? AND ?", from, to));
        list.add(singleRow("Payments", "SELECT COUNT(*) AS row_count, SUM(p.amount) AS amount FROM fx_payments p WHERE DATE(p.payment_date) BETWEEN ? AND ?", from, to));
        list.add(singleRow("Expenses", "SELECT COUNT(*) AS row_count, SUM(e.amount) AS amount FROM fx_expenses e WHERE e.expense_date BETWEEN ? AND ?", from, to));
        return list;
    }

    private static ObservableList<ReportRow> rowsWithoutDates(String sql) {
        ObservableList<ReportRow> list = FXCollections.observableArrayList();
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new ReportRow(
                        rs.getString("label"),
                        rs.getInt("row_count"),
                        rs.getDouble("amount")
                ));
            }
        } catch (Exception e) {
            P.pf("Report query failed: " + e.getMessage());
        }
        return list;
    }

    private static ObservableList<ReportRow> rows(String sql, LocalDate from, LocalDate to) {
        ObservableList<ReportRow> list = FXCollections.observableArrayList();
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ReportRow(
                            rs.getString("label"),
                            rs.getInt("row_count"),
                            rs.getDouble("amount")
                    ));
                }
            }
        } catch (Exception e) {
            P.pf("Report query failed: " + e.getMessage());
        }
        return list;
    }

    private static ReportRow singleRow(String label, String sql, LocalDate from, LocalDate to) {
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new ReportRow(label, rs.getInt("row_count"), rs.getDouble("amount"));
            }
        } catch (Exception e) {
            P.pf("Report row failed: " + e.getMessage());
        }
        return new ReportRow(label, 0, 0);
    }
}
