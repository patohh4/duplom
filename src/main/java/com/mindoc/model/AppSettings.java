package com.mindoc.model;

/**
 * Model for per-user application settings.
 */
public class AppSettings {
    private int userId;
    private String theme;
    private String language;
    private int textSize;
    private boolean notificationsEnabled;

    public AppSettings(int userId, String theme, String language, int textSize, boolean notificationsEnabled) {
        this.userId = userId;
        this.theme = theme;
        this.language = language;
        this.textSize = textSize;
        this.notificationsEnabled = notificationsEnabled;
    }

    public int getUserId() {
        return userId;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }
}
