package com.mindoc.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Repository for tracking learning progress by user and item.
 */
public class LearningProgressRepository {
    private final Connection connection;

    public LearningProgressRepository(Connection connection) {
        this.connection = connection;
    }

    public Map<Integer, String> findStatusByUserAndType(int userId, String itemType) throws SQLException {
        Map<Integer, String> statuses = new HashMap<>();
        String sql = "SELECT item_id, status FROM learning_progress WHERE user_id = ? AND item_type = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, itemType);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    statuses.put(rs.getInt("item_id"), rs.getString("status"));
                }
            }
        }
        return statuses;
    }

    public void saveStatus(int userId, String itemType, int itemId, String status) throws SQLException {
        String sql =
            "INSERT INTO learning_progress (user_id, item_type, item_id, status, updated_at) " +
            "VALUES (?, ?, ?, ?, datetime('now')) " +
            "ON CONFLICT(user_id, item_type, item_id) DO UPDATE SET " +
            "status = excluded.status, updated_at = datetime('now')";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, itemType);
            pstmt.setInt(3, itemId);
            pstmt.setString(4, status);
            pstmt.executeUpdate();
        }
    }
}
