package com.mindoc.ui.theme;

/**
 * MindDoc application theme with color palette and styles
 */
public class MindDocTheme {
    // Primary Colors
    public static final String PRIMARY = "#667eea";
    public static final String PRIMARY_DARK = "#764ba2";
    public static final String ACCENT = "#f093fb";
    
    // Secondary Colors
    public static final String SUCCESS = "#4caf50";
    public static final String WARNING = "#ff9800";
    public static final String DANGER = "#f44336";
    public static final String INFO = "#2196f3";
    
    // Neutral Colors
    public static final String BACKGROUND = "#f7fafc";
    public static final String SURFACE = "#ffffff";
    public static final String TEXT_PRIMARY = "#2d3748";
    public static final String TEXT_SECONDARY = "#718096";
    public static final String BORDER = "#e2e8f0";
    
    // Mood Colors
    public static final String MOOD_EXCELLENT = "#4caf50"; // Green
    public static final String MOOD_GOOD = "#8bc34a";      // Light Green
    public static final String MOOD_NEUTRAL = "#ffc107";   // Amber
    public static final String MOOD_BAD = "#ff9800";        // Orange
    public static final String MOOD_TERRIBLE = "#f44336";   // Red
    
    // CSS Stylesheet
    public static String getStylesheet() {
        return """
            * {
                -fx-font-family: 'Segoe UI', 'Helvetica Neue', sans-serif;
            }
            
            .root {
                -fx-background-color: #f7fafc;
                -fx-text-fill: #2d3748;
            }
            
            .button {
                -fx-padding: 8px 16px;
                -fx-font-size: 14px;
                -fx-cursor: hand;
                -fx-border-radius: 4px;
                -fx-background-radius: 4px;
            }
            
            .button:default {
                -fx-background-color: #667eea;
                -fx-text-fill: white;
            }
            
            .button:default:hover {
                -fx-background-color: #764ba2;
            }
            
            .label {
                -fx-font-size: 14px;
                -fx-text-fill: #2d3748;
            }
            
            .label:header {
                -fx-font-size: 18px;
                -fx-font-weight: bold;
            }
            
            .tab-pane .tab {
                -fx-padding: 10px 20px;
                -fx-font-size: 13px;
            }
            
            .tab-pane .tab-header-background {
                -fx-background-color: #ffffff;
            }
            
            .tab-pane:focused > .tab-header-area > .headers-region > .tab:selected {
                -fx-border-color: #667eea;
                -fx-border-width: 0 0 2 0;
            }
            
            .scroll-pane {
                -fx-background-color: #f7fafc;
            }
            
            .text-field {
                -fx-padding: 8px 10px;
                -fx-border-color: #e2e8f0;
                -fx-border-width: 1;
                -fx-border-radius: 4px;
            }
            
            .text-area {
                -fx-padding: 8px 10px;
                -fx-border-color: #e2e8f0;
                -fx-border-width: 1;
                -fx-border-radius: 4px;
            }
            
            .combo-box {
                -fx-border-color: #e2e8f0;
                -fx-border-width: 1;
                -fx-border-radius: 4px;
            }
            
            .progress-bar {
                -fx-accent: #667eea;
            }
            
            .menu-bar {
                -fx-background-color: #2c3e50;
            }
            
            .menu-bar .menu .label {
                -fx-text-fill: white;
            }
            
            .menu-item {
                -fx-padding: 8px 20px;
            }
            
            .menu-item:focused {
                -fx-background-color: #667eea;
            }
            """;
    }
    
    // Helper method to get mood color based on level
    public static String getMoodColor(int moodLevel) {
        return switch (moodLevel) {
            case 8, 9, 10 -> MOOD_EXCELLENT;
            case 6, 7 -> MOOD_GOOD;
            case 5 -> MOOD_NEUTRAL;
            case 3, 4 -> MOOD_BAD;
            default -> MOOD_TERRIBLE;
        };
    }
    
    // Helper method to get severity color
    public static String getSeverityColor(int severity) {
        if (severity <= 2) return SUCCESS;
        if (severity <= 4) return WARNING;
        if (severity <= 7) return DANGER;
        return DANGER;
    }
}
