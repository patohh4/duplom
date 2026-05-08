package com.mindoc.repository;

import com.mindoc.model.SymptomLog;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for managing symptom logs in the database
 */
public class SymptomLogRepository {
    private Connection connection;
    
    public SymptomLogRepository(Connection connection) {
        this.connection = connection;
    }
    
    public void create(SymptomLog log) throws SQLException {
        String sql = "INSERT INTO symptom_logs (user_id, symptom_id, severity, date, notes) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, log.getUserId());
            pstmt.setInt(2, log.getSymptomId());
            pstmt.setInt(3, log.getSeverity());
            pstmt.setString(4, log.getDate());
            pstmt.setString(5, log.getNotes());
            pstmt.executeUpdate();
        }
    }
    
    public SymptomLog findById(int id) throws SQLException {
        String sql = "SELECT * FROM symptom_logs WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSymptomLog(rs);
                }
            }
        }
        return null;
    }
    
    public List<SymptomLog> findByUserId(int userId) throws SQLException {
        List<SymptomLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM symptom_logs WHERE user_id = ? ORDER BY date DESC LIMIT 50";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToSymptomLog(rs));
                }
            }
        }
        return logs;
    }
    
    public List<SymptomLog> findBySymptomId(int symptomId) throws SQLException {
        List<SymptomLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM symptom_logs WHERE symptom_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, symptomId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToSymptomLog(rs));
                }
            }
        }
        return logs;
    }
    
    public void update(SymptomLog log) throws SQLException {
        String sql = "UPDATE symptom_logs SET severity = ?, notes = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, log.getSeverity());
            pstmt.setString(2, log.getNotes());
            pstmt.setInt(3, log.getId());
            pstmt.executeUpdate();
        }
    }
    
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM symptom_logs WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
    
    private SymptomLog mapResultSetToSymptomLog(ResultSet rs) throws SQLException {
        return new SymptomLog(
            rs.getInt("id"),
            rs.getInt("user_id"),
            rs.getInt("symptom_id"),
            rs.getInt("severity"),
            rs.getString("date"),
            rs.getString("notes")
        );
    }
}
