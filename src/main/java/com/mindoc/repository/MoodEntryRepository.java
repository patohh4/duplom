package com.mindoc.repository;

import com.mindoc.model.MoodEntry;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for managing mood entries in the database
 */
public class MoodEntryRepository {
    private Connection connection;
    
    public MoodEntryRepository(Connection connection) {
        this.connection = connection;
    }
    
    public void create(MoodEntry entry) throws SQLException {
        String sql = "INSERT INTO mood_entries (user_id, mood_level, mood_emoji, note, context, symptoms, entry_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, entry.getUserId());
            pstmt.setInt(2, entry.getMoodLevel());
            pstmt.setString(3, entry.getMoodEmoji());
            pstmt.setString(4, entry.getNote());
            pstmt.setString(5, entry.getContext());
            pstmt.setString(6, entry.getSymptoms());
            pstmt.setString(7, entry.getEntryDate().toString());
            pstmt.executeUpdate();
        }
    }
    
    public MoodEntry findById(int id) throws SQLException {
        String sql = "SELECT * FROM mood_entries WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMoodEntry(rs);
                }
            }
        }
        return null;
    }
    
    public List<MoodEntry> findByUserId(int userId) throws SQLException {
        List<MoodEntry> entries = new ArrayList<>();
        String sql = "SELECT * FROM mood_entries WHERE user_id = ? ORDER BY entry_date DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    entries.add(mapResultSetToMoodEntry(rs));
                }
            }
        }
        return entries;
    }
    
    public List<MoodEntry> findByUserIdAndDateRange(int userId, LocalDate startDate, LocalDate endDate) throws SQLException {
        List<MoodEntry> entries = new ArrayList<>();
        String sql = "SELECT * FROM mood_entries WHERE user_id = ? AND entry_date BETWEEN ? AND ? ORDER BY entry_date DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, startDate.toString());
            pstmt.setString(3, endDate.toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    entries.add(mapResultSetToMoodEntry(rs));
                }
            }
        }
        return entries;
    }
    
    public MoodEntry findByUserIdAndDate(int userId, LocalDate date) throws SQLException {
        String sql = "SELECT * FROM mood_entries WHERE user_id = ? AND entry_date = ? LIMIT 1";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, date.toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMoodEntry(rs);
                }
            }
        }
        return null;
    }
    
    public double getAverageMoodForUser(int userId, LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT AVG(mood_level) FROM mood_entries WHERE user_id = ? AND entry_date BETWEEN ? AND ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, startDate.toString());
            pstmt.setString(3, endDate.toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }
    
    public void update(MoodEntry entry) throws SQLException {
        String sql = "UPDATE mood_entries SET mood_level = ?, mood_emoji = ?, note = ?, context = ?, symptoms = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, entry.getMoodLevel());
            pstmt.setString(2, entry.getMoodEmoji());
            pstmt.setString(3, entry.getNote());
            pstmt.setString(4, entry.getContext());
            pstmt.setString(5, entry.getSymptoms());
            pstmt.setInt(6, entry.getId());
            pstmt.executeUpdate();
        }
    }
    
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM mood_entries WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
    
    private MoodEntry mapResultSetToMoodEntry(ResultSet rs) throws SQLException {
        return new MoodEntry(
            rs.getInt("id"),
            rs.getInt("user_id"),
            rs.getInt("mood_level"),
            rs.getString("mood_emoji"),
            rs.getString("note"),
            rs.getString("context"),
            rs.getString("symptoms"),
            LocalDate.parse(rs.getString("entry_date"))
        );
    }
}
