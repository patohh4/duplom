package com.mindoc.model;

import java.time.LocalDate;

/**
 * Model class representing a user profile
 */
public class User {
    private int id;
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String dateOfBirth;
    private String gender;
    private LocalDate registrationDate;
    private String lastLoginDate;
    private boolean notificationsEnabled;
    
    public User(String username, String email) {
        this.username = username;
        this.email = email;
        this.password = "";
        this.notificationsEnabled = true;
    }
    
    public User(int id, String username, String email, String password, String firstName, String lastName,
               String dateOfBirth, LocalDate registrationDate, boolean notificationsEnabled) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.registrationDate = registrationDate;
        this.notificationsEnabled = notificationsEnabled;
    }
    
    public User(int id, String username, String email, String firstName, String lastName,
               String dateOfBirth, String gender, String registrationDate) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = "";
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.notificationsEnabled = true;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getDateOfBirth() {
        return dateOfBirth;
    }
    
    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public LocalDate getRegistrationDate() {
        return registrationDate;
    }
    
    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }
    
    public String getLastLoginDate() {
        return lastLoginDate;
    }
    
    public void setLastLoginDate(String lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }
    
    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }
    
    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }
    
    @Override
    public String toString() {
        return firstName + " " + lastName;
    }
}
