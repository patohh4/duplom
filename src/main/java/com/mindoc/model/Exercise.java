package com.mindoc.model;

/**
 * Model class representing an exercise or coping strategy
 */
public class Exercise {
    private int id;
    private String title;
    private String description;
    private String instructions; // Step-by-step instructions
    private String category; // breathing, meditation, grounding, etc.
    private int duration; // in minutes
    private String difficulty; // beginner, intermediate, advanced
    private String icon;
    private int completionCount;
    
    public Exercise(String title, String description, String category) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.difficulty = "beginner";
        this.duration = 10;
        this.completionCount = 0;
    }
    
    public Exercise(int id, String title, String description, String instructions,
                   String category, int duration, String difficulty, String icon) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.instructions = instructions;
        this.category = category;
        this.duration = duration;
        this.difficulty = difficulty;
        this.icon = icon;
        this.completionCount = 0;
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
    
    public String getInstructions() {
        return instructions;
    }
    
    public void setInstructions(String instructions) {
        this.instructions = instructions;
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
    
    public String getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    public int getCompletionCount() {
        return completionCount;
    }
    
    public void setCompletionCount(int completionCount) {
        this.completionCount = completionCount;
    }
    
    @Override
    public String toString() {
        return title;
    }
}
