package com.mindoc.repository;

import com.mindoc.model.Exercise;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for managing exercises in the database
 */
public class ExerciseRepository {
    private Connection connection;
    
    public ExerciseRepository(Connection connection) {
        this.connection = connection;
    }
    
    public void create(Exercise exercise) throws SQLException {
        String sql = "INSERT INTO exercises (title, description, instructions, category, duration, difficulty, icon) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, exercise.getTitle());
            pstmt.setString(2, exercise.getDescription());
            pstmt.setString(3, exercise.getInstructions());
            pstmt.setString(4, exercise.getCategory());
            pstmt.setInt(5, exercise.getDuration());
            pstmt.setString(6, exercise.getDifficulty());
            pstmt.setString(7, exercise.getIcon());
            pstmt.executeUpdate();
        }
    }
    
    public Exercise findById(int id) throws SQLException {
        String sql = "SELECT * FROM exercises WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToExercise(rs);
                }
            }
        }
        return null;
    }
    
    public List<Exercise> findByCategory(String category) throws SQLException {
        List<Exercise> exercises = new ArrayList<>();
        String sql = "SELECT * FROM exercises WHERE category = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, category);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    exercises.add(mapResultSetToExercise(rs));
                }
            }
        }
        return exercises;
    }
    
    public List<Exercise> findAll() throws SQLException {
        List<Exercise> exercises = new ArrayList<>();
        String sql = "SELECT * FROM exercises";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                exercises.add(mapResultSetToExercise(rs));
            }
        }
        return exercises;
    }
    
    public void update(Exercise exercise) throws SQLException {
        String sql = "UPDATE exercises SET title = ?, description = ?, instructions = ?, " +
                    "category = ?, duration = ?, difficulty = ?, icon = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, exercise.getTitle());
            pstmt.setString(2, exercise.getDescription());
            pstmt.setString(3, exercise.getInstructions());
            pstmt.setString(4, exercise.getCategory());
            pstmt.setInt(5, exercise.getDuration());
            pstmt.setString(6, exercise.getDifficulty());
            pstmt.setString(7, exercise.getIcon());
            pstmt.setInt(8, exercise.getId());
            pstmt.executeUpdate();
        }
    }
    
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM exercises WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
    
    private Exercise mapResultSetToExercise(ResultSet rs) throws SQLException {
        return new Exercise(
            rs.getInt("id"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getString("instructions"),
            rs.getString("category"),
            rs.getInt("duration"),
            rs.getString("difficulty"),
            rs.getString("icon")
        );
    }
}
