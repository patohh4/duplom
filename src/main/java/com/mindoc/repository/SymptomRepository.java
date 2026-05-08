package com.mindoc.repository;

import com.mindoc.model.Symptom;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for managing symptoms in the database
 */
public class SymptomRepository {
    private Connection connection;
    
    public SymptomRepository(Connection connection) {
        this.connection = connection;
    }
    
    public void create(Symptom symptom) throws SQLException {
        String sql = "INSERT INTO symptoms (name, description, category, severity, icon) " +
                    "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, symptom.getName());
            pstmt.setString(2, symptom.getDescription());
            pstmt.setString(3, symptom.getCategory());
            pstmt.setInt(4, symptom.getSeverity());
            pstmt.setString(5, symptom.getIcon());
            pstmt.executeUpdate();
        }
    }
    
    public Symptom findById(int id) throws SQLException {
        String sql = "SELECT * FROM symptoms WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSymptom(rs);
                }
            }
        }
        return null;
    }
    
    public List<Symptom> findByCategory(String category) throws SQLException {
        List<Symptom> symptoms = new ArrayList<>();
        String sql = "SELECT * FROM symptoms WHERE category = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, category);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    symptoms.add(mapResultSetToSymptom(rs));
                }
            }
        }
        return symptoms;
    }
    
    public Symptom findByName(String name) throws SQLException {
        String sql = "SELECT * FROM symptoms WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSymptom(rs);
                }
            }
        }
        return null;
    }
    
    public List<Symptom> findAll() throws SQLException {
        List<Symptom> symptoms = new ArrayList<>();
        String sql = "SELECT * FROM symptoms";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                symptoms.add(mapResultSetToSymptom(rs));
            }
        }
        return symptoms;
    }
    
    public void update(Symptom symptom) throws SQLException {
        String sql = "UPDATE symptoms SET name = ?, description = ?, category = ?, severity = ?, icon = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, symptom.getName());
            pstmt.setString(2, symptom.getDescription());
            pstmt.setString(3, symptom.getCategory());
            pstmt.setInt(4, symptom.getSeverity());
            pstmt.setString(5, symptom.getIcon());
            pstmt.setInt(6, symptom.getId());
            pstmt.executeUpdate();
        }
    }
    
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM symptoms WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
    
    private Symptom mapResultSetToSymptom(ResultSet rs) throws SQLException {
        return new Symptom(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getString("category"),
            rs.getInt("severity"),
            rs.getString("icon")
        );
    }
}
