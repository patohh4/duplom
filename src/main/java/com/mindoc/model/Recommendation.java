package com.mindoc.model;

/**
 * Model class representing a personalized recommendation for the user
 */
public class Recommendation {
    private int id;
    private int userId;
    private String title;
    private String description;
    private String type; // course, exercise, assessment
    private int targetId; // ID of the related course/exercise
    private String reason; // Why this recommendation was made
    private int priority; // 1-5 scale
    private String date;
    
    public Recommendation(int userId, String title, String description, String type) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.type = type;
        this.priority = 3;
    }
    
    public Recommendation(int id, int userId, String title, String description, 
                         String type, int targetId, String reason, int priority, String date) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.type = type;
        this.targetId = targetId;
        this.reason = reason;
        this.priority = priority;
        this.date = date;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public int getTargetId() {
        return targetId;
    }
    
    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public void setPriority(int priority) {
        this.priority = priority;
    }
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    @Override
    public String toString() {
        return title;
    }
}
