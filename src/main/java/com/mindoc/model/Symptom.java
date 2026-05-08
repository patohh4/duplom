package com.mindoc.model;

/**
 * Model class representing a symptom of mental health condition
 */
public class Symptom {
    private int id;
    private String name;
    private String description;
    private String category; // depression, anxiety, sleep, eating_disorder
    private int severity; // 1-10 scale
    private String icon; // emoji or icon code
    
    public Symptom(String name, String description, String category) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.severity = 5;
    }
    
    public Symptom(int id, String name, String description, String category, int severity, String icon) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.severity = severity;
        this.icon = icon;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public int getSeverity() {
        return severity;
    }
    
    public void setSeverity(int severity) {
        this.severity = severity;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    @Override
    public String toString() {
        return name;
    }
}
