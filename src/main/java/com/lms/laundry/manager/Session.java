package com.lms.laundry.manager;

import com.lms.laundry.model.User;
import com.lms.laundry.model.Settings;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class Session {
    private static final Preferences PREFS = Preferences.userRoot().node("LaundrySystem");

    private static volatile User currentUser;
    private static volatile List<Settings> settingsList = Collections.emptyList();

    private Session() {}

    public static void saveLogin(String name, String username, String role) {
        PREFS.put("remember_name", Objects.requireNonNull(name));
        PREFS.put("remember_username", Objects.requireNonNull(username));
        PREFS.put("remember_role", Objects.requireNonNull(role));
        PREFS.putBoolean("remember_me", true);
    }

    public static String getName() { return PREFS.get("remember_name", null); }
    public static String getUsername() { return PREFS.get("remember_username", null); }
    public static String getRole() { return PREFS.get("remember_role", null); }
    public static boolean isRemembered() { return PREFS.getBoolean("remember_me", false); }

    public static void setCurrentUser(User user) {
        currentUser = user;
        if (user != null) {
            PREFS.put("remember_name", Objects.requireNonNull(user.getName()));
            PREFS.put("remember_username", Objects.requireNonNull(user.getUsername()));
            PREFS.put("remember_role", Objects.requireNonNull(user.getRole().toString()));
        }
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setSettings(List<Settings> settings) {
        if (settings == null) return;
        settingsList = List.copyOf(settings); // Immutability defense
        for (Settings setting : settingsList) {
            if (setting.getKey() != null && setting.getValue() != null) {
                PREFS.put(setting.getKey(), setting.getValue());
            }
        }
    }

    public static List<Settings> getSettingsList() {
        return settingsList;
    }

    /**
     * Completely wipes the session, including the user registry keys
     */
    public static void clearSession() {
        currentUser = null;
        settingsList = Collections.emptyList();
        try {
            PREFS.clear();
        } catch (BackingStoreException e) {
            PREFS.remove("remember_name");
            PREFS.remove("remember_username");
            PREFS.remove("remember_role");
            PREFS.putBoolean("remember_me", false);
        }
    }
}
