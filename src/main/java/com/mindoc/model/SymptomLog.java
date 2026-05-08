package com.mindoc.model;

/**
 * Model class representing a symptom log entry
 */
public class SymptomLog {
    private int id;
    private int userId;
    private int symptomId;
    private int severity; // 1-10 scale
    private String date;
    private String notes;
    
    public SymptomLog(int userId, int symptomId, int severity, String date) {
        this.userId = userId;
        this.symptomId = symptomId;
        this.severity = severity;
        this.date = date;
    }
    
    public SymptomLog(int id, int userId, int symptomId, int severity, String date, String notes) {
        this.id = id;
        this.userId = userId;
        this.symptomId = symptomId;
        this.severity = severity;
        this.date = date;
        this.notes = notes;
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
    
    public int getSymptomId() {
        return symptomId;
    }
    
    public void setSymptomId(int symptomId) {
        this.symptomId = symptomId;
    }
    
    public int getSeverity() {
        return severity;
    }
    
    public void setSeverity(int severity) {
        this.severity = severity;
    }
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}
