package com.mindoc.model;

import java.time.LocalDate;

/**
 * Model class representing an assessment/evaluation of emotional health
 */
public class Assessment {
    private int id;
    private int userId;
    private LocalDate assessmentDate;
    private int moodScore; // 1-10
    private int anxietyScore; // 1-10
    private int depressionScore; // 1-10
    private int stressScore; // 1-10
    private int overallWellbeing; // 1-10
    private String summary;
    private String recommendation;
    
    public Assessment(int userId, LocalDate assessmentDate) {
        this.userId = userId;
        this.assessmentDate = assessmentDate;
        this.moodScore = 5;
        this.anxietyScore = 5;
        this.depressionScore = 5;
        this.stressScore = 5;
        this.overallWellbeing = 5;
    }
    
    public Assessment(int id, int userId, LocalDate assessmentDate, 
                     int moodScore, int anxietyScore, int depressionScore,
                     int stressScore, int overallWellbeing, String summary, String recommendation) {
        this.id = id;
        this.userId = userId;
        this.assessmentDate = assessmentDate;
        this.moodScore = moodScore;
        this.anxietyScore = anxietyScore;
        this.depressionScore = depressionScore;
        this.stressScore = stressScore;
        this.overallWellbeing = overallWellbeing;
        this.summary = summary;
        this.recommendation = recommendation;
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
    
    public LocalDate getAssessmentDate() {
        return assessmentDate;
    }
    
    public void setAssessmentDate(LocalDate assessmentDate) {
        this.assessmentDate = assessmentDate;
    }
    
    public int getMoodScore() {
        return moodScore;
    }
    
    public void setMoodScore(int moodScore) {
        this.moodScore = moodScore;
    }
    
    public int getAnxietyScore() {
        return anxietyScore;
    }
    
    public void setAnxietyScore(int anxietyScore) {
        this.anxietyScore = anxietyScore;
    }
    
    public int getDepressionScore() {
        return depressionScore;
    }
    
    public void setDepressionScore(int depressionScore) {
        this.depressionScore = depressionScore;
    }
    
    public int getStressScore() {
        return stressScore;
    }
    
    public void setStressScore(int stressScore) {
        this.stressScore = stressScore;
    }
    
    public int getOverallWellbeing() {
        return overallWellbeing;
    }
    
    public void setOverallWellbeing(int overallWellbeing) {
        this.overallWellbeing = overallWellbeing;
    }
    
    public String getSummary() {
        return summary;
    }
    
    public void setSummary(String summary) {
        this.summary = summary;
    }
    
    public String getRecommendation() {
        return recommendation;
    }
    
    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }
}
