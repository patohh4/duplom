package com.mindoc.repository;

import com.mindoc.model.CBTCourse;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for managing CBT courses in the database
 */
public class CBTCourseRepository {
    private Connection connection;
    
    public CBTCourseRepository(Connection connection) {
        this.connection = connection;
    }
    
    public void create(CBTCourse course) throws SQLException {
        String sql = "INSERT INTO cbt_courses (title, description, category, duration, difficulty, content, icon) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, course.getTitle());
            pstmt.setString(2, course.getDescription());
            pstmt.setString(3, course.getCategory());
            pstmt.setInt(4, course.getDuration());
            pstmt.setInt(5, course.getDifficulty());
            pstmt.setString(6, course.getContent());
            pstmt.setString(7, course.getIcon());
            pstmt.executeUpdate();
        }
    }
    
    public CBTCourse findById(int id) throws SQLException {
        String sql = "SELECT * FROM cbt_courses WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCourse(rs);
                }
            }
        }
        return null;
    }
    
    public List<CBTCourse> findByCategory(String category) throws SQLException {
        List<CBTCourse> courses = new ArrayList<>();
        String sql = "SELECT * FROM cbt_courses WHERE category = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, category);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    courses.add(mapResultSetToCourse(rs));
                }
            }
        }
        return courses;
    }
    
    public List<CBTCourse> findAll() throws SQLException {
        List<CBTCourse> courses = new ArrayList<>();
        String sql = "SELECT * FROM cbt_courses";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                courses.add(mapResultSetToCourse(rs));
            }
        }
        return courses;
    }
    
    public void update(CBTCourse course) throws SQLException {
        String sql = "UPDATE cbt_courses SET title = ?, description = ?, category = ?, " +
                    "duration = ?, difficulty = ?, content = ?, icon = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, course.getTitle());
            pstmt.setString(2, course.getDescription());
            pstmt.setString(3, course.getCategory());
            pstmt.setInt(4, course.getDuration());
            pstmt.setInt(5, course.getDifficulty());
            pstmt.setString(6, course.getContent());
            pstmt.setString(7, course.getIcon());
            pstmt.setInt(8, course.getId());
            pstmt.executeUpdate();
        }
    }
    
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM cbt_courses WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
    
    private CBTCourse mapResultSetToCourse(ResultSet rs) throws SQLException {
        return new CBTCourse(
            rs.getInt("id"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getString("category"),
            rs.getInt("duration"),
            rs.getInt("difficulty"),
            rs.getString("content"),
            rs.getString("icon")
        );
    }
}
