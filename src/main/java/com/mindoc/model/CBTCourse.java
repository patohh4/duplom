package com.mindoc.model;

/**
 * Model class representing a CBT (Cognitive Behavioral Therapy) Course
 */
public class CBTCourse {
    private int id;
    private String title;
    private String description;
    private String category; // depression, anxiety, sleep, etc.
    private int duration; // in minutes
    private int difficulty; // 1-5
    private String content; // HTML or plain text content
    private String icon;
    private boolean completed;
    private int progress; // 0-100
    
    public CBTCourse(String title, String description, String category) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.difficulty = 3;
        this.duration = 30;
        this.progress = 0;
    }
    
    public CBTCourse(int id, String title, String description, String category, 
                    int duration, int difficulty, String content, String icon) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.duration = duration;
        this.difficulty = difficulty;
        this.content = content;
        this.icon = icon;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
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
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public int getDuration() {
        return duration;
    }
    
    public void setDuration(int duration) {
        this.duration = duration;
    }
    
    public int getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    public boolean isCompleted() {
        return completed;
    }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
    
    public int getProgress() {
        return progress;
    }
    
    public void setProgress(int progress) {
        this.progress = progress;
    }
    
    @Override
    public String toString() {
        return title;
    }
}
