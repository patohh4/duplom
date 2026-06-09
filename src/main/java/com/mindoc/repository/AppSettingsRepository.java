package com.mindoc.repository;

import com.mindoc.model.AppSettings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Repository for application settings.
 */
public class AppSettingsRepository {
    private final Connection connection;

    public AppSettingsRepository(Connection connection) {
        this.connection = connection;
    }

    public AppSettings getOrCreateByUserId(int userId) throws SQLException {
        AppSettings settings = findByUserId(userId);
        if (settings != null) {
            return settings;
        }

        String insertSql =
            "INSERT INTO app_settings (user_id, theme, language, text_size, notifications_enabled, updated_at) " +
            "VALUES (?, 'Light', 'English', 100, 1, datetime('now'))";
        try (PreparedStatement pstmt = connection.prepareStatement(insertSql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        }
        return findByUserId(userId);
    }

    public AppSettings findByUserId(int userId) throws SQLException {
        String sql =
            "SELECT user_id, theme, language, text_size, notifications_enabled " +
            "FROM app_settings WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new AppSettings(
                        rs.getInt("user_id"),
                        rs.getString("theme"),
                        rs.getString("language"),
                        rs.getInt("text_size"),
                        rs.getInt("notifications_enabled") == 1
                    );
                }
            }
        }
        return null;
    }

    public void save(AppSettings settings) throws SQLException {
        String sql =
            "UPDATE app_settings SET theme = ?, language = ?, text_size = ?, " +
            "notifications_enabled = ?, updated_at = datetime('now') WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, settings.getTheme());
            pstmt.setString(2, settings.getLanguage());
            pstmt.setInt(3, settings.getTextSize());
            pstmt.setInt(4, settings.isNotificationsEnabled() ? 1 : 0);
            pstmt.setInt(5, settings.getUserId());
            pstmt.executeUpdate();
        }
    }
}
