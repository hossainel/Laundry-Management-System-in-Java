package com.lms.laundry.manager;

import com.lms.laundry.model.Settings;
import com.lms.laundry.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SettingsManager {
    private static final String BASE_SELECT = "SELECT * FROM fx_settings WHERE ";
    private static final String BASE_UPDATE = "UPDATE fx_settings SET ";
    private static final String BASE_WHERE = " WHERE id=?";

    public static Settings getById(int id) {
        String sql = BASE_SELECT+"id=? LIMIT 1";
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapSettings(rs);
            }
        } catch (Exception e) {
            P.pf("Error fetching settings ID: " + id);
        }
        return null;
    }

    public static List<Settings> getAll() {
        List<Settings> list = new ArrayList<>();
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(BASE_SELECT+"1=1");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapSettings(rs));
            }
        } catch (Exception e) {
            P.pf("Error fetching all settings: " + e.getMessage());
        }
        return list;
    }

    public static boolean update(Settings settings) {
        String sql = BASE_UPDATE+"setting_value=?, description=?"+BASE_WHERE;
        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, settings.getValue());
            ps.setString(2, settings.getDescription());
            ps.setInt(3, settings.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            P.pf("Error updating settings: " + settings.getKey());
        }
        return false;
    }

    private static Settings mapSettings(ResultSet rs) throws Exception {
        return new Settings(
                rs.getInt("id"),
                rs.getString("setting_key"),
                rs.getString("setting_value"),
                rs.getString("description")
        );
    }
}
