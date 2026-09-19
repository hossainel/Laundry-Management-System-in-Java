package com.lms.laundry.manager;

import com.lms.laundry.model.Expense;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExpenseManager {
    private static final String BASE_SELECT = "SELECT fx_expenses.*, fx_expense_categories.name" +
            " AS category_name, fx_payment_methods.name AS payment_method_name FROM" +
            " fx_expenses LEFT JOIN fx_expense_categories ON" +
            " fx_expense_categories.id=fx_expenses.category_id LEFT JOIN fx_payment_methods" +
            " ON fx_payment_methods.id=fx_expenses.method_id WHERE ";
    private static final String BASE_UPDATE = "UPDATE fx_expenses SET ";
    private static final String BASE_WHERE = " WHERE id=?";
    private static final String BASE_INSERT = "INSERT INTO fx_expenses ";

    public static Expense getById(int id) {
        String sql = BASE_SELECT + "fx_expenses.id=? LIMIT 1";
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapExpense(rs);
            }
        } catch (Exception e) {
            P.pf("Error fetching expense ID " + id + ": " + e.getMessage());
        }
        return null;
    }

    public static List<Expense> getAll() {
        List<Expense> list = new ArrayList<>();
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(BASE_SELECT + "1=1");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapExpense(rs));
            }
        } catch (Exception e) {
            P.pf("Error fetching all expenses: " + e.getMessage());
        }
        return list;
    }

    public static boolean create(Expense expense) {
        String sql = BASE_INSERT + "(title, category_id, amount, expense_date, method_id, note) VALUES (?,?,?,?,?,?)";
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            editUpdate(expense, ps);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            P.pf("Error creating expense: " + e.getMessage());
        }
        return false;
    }

    private static void editUpdate(Expense expense, PreparedStatement ps) throws SQLException {
        ps.setString(1, expense.getTitle());
        ps.setInt(2, expense.getExpenseCategory() != null ? expense.getExpenseCategory().getId() : expense.getCategoryId());
        ps.setDouble(3, expense.getAmount());
        ps.setDate(4, expense.getDate() != null ? Date.valueOf(expense.getDate()) : null);
        ps.setInt(5, expense.getPaymentMethod() != null ? expense.getPaymentMethod().getId() : expense.getPaymentMethodId());
        ps.setString(6, expense.getNote());
    }

    public static boolean update(Expense expense) {
        String sql = BASE_UPDATE + "title=?, category_id=?, amount=?, expense_date=?, method_id=?, note=?" + BASE_WHERE;
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            editUpdate(expense, ps);
            ps.setInt(7, expense.getId());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            P.pf("Error updating expense " + expense.getTitle() + ": " + e.getMessage());
        }
        return false;
    }

    public static boolean delete(int id) {
        String sql = "DELETE FROM fx_expenses" + BASE_WHERE;
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            P.pf("Error deleting expense ID " + id + ": " + e.getMessage());
        }
        return false;
    }

    private static Expense mapExpense(ResultSet rs) throws Exception {
        java.sql.Date dbDate = rs.getDate("expense_date");
        Timestamp dbCreatedAt = rs.getTimestamp("created_at");

        return new Expense(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getInt("category_id"),
                rs.getString("category_name"),
                rs.getDouble("amount"),
                dbDate != null ? dbDate.toLocalDate() : null,
                rs.getInt("method_id"),
                rs.getString("payment_method_name"),
                rs.getString("note"),
                dbCreatedAt != null ? dbCreatedAt.toLocalDateTime() : null
        );
    }
}
