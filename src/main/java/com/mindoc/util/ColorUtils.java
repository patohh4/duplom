package com.mindoc.util;

import javafx.scene.paint.Color;

/**
 * Utility class for color operations
 */
public class ColorUtils {
    
    public static Color hexToColor(String hex) {
        try {
            return Color.web(hex);
        } catch (IllegalArgumentException e) {
            return Color.BLACK;
        }
    }
    
    public static String colorToHex(Color color) {
        return String.format("#%02X%02X%02X",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255));
    }
    
    public static Color getMoodColor(int moodLevel) {
        return switch (moodLevel) {
            case 8, 9, 10 -> Color.web("#4caf50"); // Green - Excellent
            case 6, 7 -> Color.web("#8bc34a");     // Light Green - Good
            case 5 -> Color.web("#ffc107");        // Amber - Neutral
            case 3, 4 -> Color.web("#ff9800");     // Orange - Bad
            default -> Color.web("#f44336");       // Red - Terrible
        };
    }
    
    public static Color getSeverityColor(int severity) {
        if (severity <= 2) return Color.web("#4caf50");  // Green
        if (severity <= 4) return Color.web("#ff9800");  // Orange
        if (severity <= 7) return Color.web("#ff5722");  // Deep Orange
        return Color.web("#f44336");                      // Red
    }
    
    public static Color invertColor(Color color) {
        return new Color(
            1 - color.getRed(),
            1 - color.getGreen(),
            1 - color.getBlue(),
            color.getOpacity()
        );
    }
}
