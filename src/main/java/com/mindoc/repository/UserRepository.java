package com.mindoc.repository;

import com.mindoc.model.User;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for managing users in the database
 */
public class UserRepository {
    private Connection connection;
    
    public UserRepository(Connection connection) {
        this.connection = connection;
    }
    
    public void create(User user) throws SQLException {
        String sql = "INSERT INTO users (username, email, password, first_name, last_name, date_of_birth, gender, registration_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPassword());
            pstmt.setString(4, user.getFirstName());
            pstmt.setString(5, user.getLastName());
            pstmt.setString(6, user.getDateOfBirth());
            pstmt.setString(7, user.getGender());
            pstmt.setString(8, user.getRegistrationDate() != null ? user.getRegistrationDate().toString() : LocalDate.now().toString());
            pstmt.executeUpdate();
        }
    }
    
    public User findById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }
    
    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }
    
    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        return users;
    }
    
    public void update(User user) throws SQLException {
        String sql = "UPDATE users SET username = ?, email = ?, first_name = ?, last_name = ?, " +
                    "date_of_birth = ?, gender = ?, last_login_date = ?, notifications_enabled = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getFirstName());
            pstmt.setString(4, user.getLastName());
            pstmt.setString(5, user.getDateOfBirth());
            pstmt.setString(6, user.getGender());
            pstmt.setString(7, user.getLastLoginDate());
            pstmt.setInt(8, user.isNotificationsEnabled() ? 1 : 0);
            pstmt.setInt(9, user.getId());
            pstmt.executeUpdate();
        }
    }
    
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
    
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        LocalDate regDate = null;
        try {
            String dateStr = rs.getString("registration_date");
            if (dateStr != null) {
                regDate = LocalDate.parse(dateStr);
            }
        } catch (Exception e) {
            regDate = LocalDate.now();
        }
        
        User user = new User(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getString("date_of_birth"),
            regDate,
            rs.getInt("notifications_enabled") == 1
        );
        return user;
    }
}
