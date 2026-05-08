package com.mindoc.model;

import java.time.LocalDate;

/**
 * Model class representing a mood entry
 */
public class MoodEntry {
    private int id;
    private int userId;
    private int moodLevel; // 1-10
    private String moodEmoji;
    private String note;
    private String context; // What triggered the mood
    private String symptoms; // Comma-separated symptom IDs
    private LocalDate entryDate;
    
    public MoodEntry(int userId, int moodLevel, String moodEmoji, String note, LocalDate entryDate) {
        this.userId = userId;
        this.moodLevel = moodLevel;
        this.moodEmoji = moodEmoji;
        this.note = note;
        this.entryDate = entryDate;
    }
    
    public MoodEntry(int userId, int moodLevel, String moodEmoji, String note, 
                    String context, String symptoms, LocalDate entryDate) {
        this.userId = userId;
        this.moodLevel = moodLevel;
        this.moodEmoji = moodEmoji;
        this.note = note;
        this.context = context;
        this.symptoms = symptoms != null ? symptoms : "";
        this.entryDate = entryDate;
    }
    
    public MoodEntry(int id, int userId, int moodLevel, String moodEmoji, String note, 
                    String context, String symptoms, LocalDate entryDate) {
        this.id = id;
        this.userId = userId;
        this.moodLevel = moodLevel;
        this.moodEmoji = moodEmoji;
        this.note = note;
        this.context = context;
        this.symptoms = symptoms != null ? symptoms : "";
        this.entryDate = entryDate;
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
    
    public int getMoodLevel() {
        return moodLevel;
    }
    
    public void setMoodLevel(int moodLevel) {
        this.moodLevel = moodLevel;
    }
    
    public String getMoodEmoji() {
        return moodEmoji;
    }
    
    public void setMoodEmoji(String moodEmoji) {
        this.moodEmoji = moodEmoji;
    }
    
    public String getNote() {
        return note;
    }
    
    public void setNote(String note) {
        this.note = note;
    }
    
    public String getContext() {
        return context;
    }
    
    public void setContext(String context) {
        this.context = context;
    }
    
    public String getSymptoms() {
        return symptoms;
    }
    
    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms != null ? symptoms : "";
    }
    
    public LocalDate getEntryDate() {
        return entryDate;
    }
    
    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }
    
    @Override
    public String toString() {
        return moodEmoji + " " + moodLevel + "/10 - " + entryDate;
    }
}
