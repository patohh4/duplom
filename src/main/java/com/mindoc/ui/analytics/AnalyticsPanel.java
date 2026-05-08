package com.mindoc.ui.analytics;

import com.mindoc.database.DatabaseManager;
import com.mindoc.model.MoodEntry;
import com.mindoc.repository.MoodEntryRepository;
import com.mindoc.ui.common.BasePanel;
import com.mindoc.ui.theme.MindDocTheme;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

/**
 * Panel for analytics and mood statistics
 */
public class AnalyticsPanel extends BasePanel {
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsPanel.class);
    
    private int currentUserId;
    private DatabaseManager databaseManager;
    private MoodEntryRepository moodEntryRepository;
    
    private Canvas moodChartCanvas;
    private Canvas statsCanvas;
    private Label averageMoodLabel;
    private Label highestMoodLabel;
    private Label lowestMoodLabel;
    private Label entriesCountLabel;
    
    public AnalyticsPanel(int userId, DatabaseManager databaseManager) {
        this.currentUserId = userId;
        this.databaseManager = databaseManager;
        this.moodEntryRepository = new MoodEntryRepository(databaseManager.getConnection());
        
        initializeUI();
        refresh();
    }
    
    private void initializeUI() {
        setStyle("-fx-background-color: " + MindDocTheme.BACKGROUND + ";");
        setPadding(new Insets(20));
        setSpacing(15);
        
        // Title
        Label titleLabel = new Label("📊 Analytics & Statistics");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setTextFill(javafx.scene.paint.Color.web(MindDocTheme.PRIMARY));
        getChildren().add(titleLabel);
        
        // Stats cards
        HBox statsContainer = createStatsCards();
        getChildren().add(statsContainer);
        
        // Chart section
        VBox chartSection = createChartSection();
        getChildren().add(chartSection);
        VBox.setVgrow(chartSection, javafx.scene.layout.Priority.ALWAYS);
    }
    
    private HBox createStatsCards() {
        HBox container = new HBox(15);
        container.setPadding(new Insets(0));
        
        // Average Mood Card
        VBox card1 = createStatCard("😊 Average Mood", "", "7.5");
        averageMoodLabel = (Label) ((VBox) card1.getChildren().get(1)).getChildren().get(0);
        container.getChildren().add(card1);
        HBox.setHgrow(card1, javafx.scene.layout.Priority.ALWAYS);
        
        // Highest Mood Card
        VBox card2 = createStatCard("🏆 Best Mood", "", "10");
        highestMoodLabel = (Label) ((VBox) card2.getChildren().get(1)).getChildren().get(0);
        container.getChildren().add(card2);
        HBox.setHgrow(card2, javafx.scene.layout.Priority.ALWAYS);
        
        // Lowest Mood Card
        VBox card3 = createStatCard("📉 Tough Day", "", "3");
        lowestMoodLabel = (Label) ((VBox) card3.getChildren().get(1)).getChildren().get(0);
        container.getChildren().add(card3);
        HBox.setHgrow(card3, javafx.scene.layout.Priority.ALWAYS);
        
        // Total Entries Card
        VBox card4 = createStatCard("📝 Total Entries", "", "24");
        entriesCountLabel = (Label) ((VBox) card4.getChildren().get(1)).getChildren().get(0);
        container.getChildren().add(card4);
        HBox.setHgrow(card4, javafx.scene.layout.Priority.ALWAYS);
        
        return container;
    }
    
    private VBox createStatCard(String title, String unit, String value) {
        VBox card = new VBox(10);
        card.setStyle(
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-radius: 8;" +
            "-fx-padding: 20;" +
            "-fx-background-color: white;"
        );
        card.setPadding(new Insets(15));
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 12));
        titleLabel.setTextFill(javafx.scene.paint.Color.web("#666"));
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        valueLabel.setTextFill(javafx.scene.paint.Color.web(MindDocTheme.PRIMARY));
        
        VBox valueContainer = new VBox();
        valueContainer.getChildren().add(valueLabel);
        
        card.getChildren().addAll(titleLabel, valueContainer);
        
        return card;
    }
    
    private VBox createChartSection() {
        VBox section = new VBox(15);
        section.setStyle(
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-radius: 8;" +
            "-fx-padding: 20;" +
            "-fx-background-color: white;"
        );
        
        Label chartTitle = new Label("Mood Trend (Last 30 Days)");
        chartTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        section.getChildren().add(chartTitle);
        
        // Chart canvas
        moodChartCanvas = new Canvas(800, 250);
        section.getChildren().add(moodChartCanvas);
        VBox.setVgrow(moodChartCanvas, javafx.scene.layout.Priority.ALWAYS);
        
        // Stats canvas
        Label statsTitle = new Label("Weekly Summary");
        statsTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        statsTitle.setPadding(new Insets(15, 0, 0, 0));
        section.getChildren().add(statsTitle);
        
        statsCanvas = new Canvas(800, 200);
        section.getChildren().add(statsCanvas);
        
        return section;
    }
    
    private void drawMoodChart() {
        try {
            List<MoodEntry> entries = moodEntryRepository.findByUserId(currentUserId);
            
            GraphicsContext gc = moodChartCanvas.getGraphicsContext2D();
            double width = moodChartCanvas.getWidth();
            double height = moodChartCanvas.getHeight();
            
            // Clear canvas
            gc.setFill(Color.WHITE);
            gc.fillRect(0, 0, width, height);
            
            if (entries.isEmpty()) {
                gc.setFont(Font.font("System", 14));
                gc.setFill(Color.web("#999"));
                gc.fillText("No mood data available yet", width / 2 - 60, height / 2);
                return;
            }
            
            // Draw axes
            gc.setStroke(Color.web("#ddd"));
            gc.setLineWidth(1);
            
            // Y-axis
            gc.strokeLine(40, 10, 40, height - 40);
            
            // X-axis
            gc.strokeLine(40, height - 40, width - 20, height - 40);
            
            // Draw grid lines
            for (int i = 1; i <= 10; i++) {
                double y = height - 40 - (i * (height - 50) / 10);
                gc.strokeLine(35, y, width - 20, y);
                
                gc.setFill(Color.web("#999"));
                gc.setFont(Font.font("System", 10));
                gc.fillText(String.valueOf(i), 20, y + 5);
            }
            
            // Draw mood plots
            gc.setStroke(Color.web(MindDocTheme.PRIMARY));
            gc.setLineWidth(2);
            
            double pointSpacing = (width - 60) / Math.max(entries.size() - 1, 1);
            
            for (int i = 0; i < entries.size(); i++) {
                double x = 40 + i * pointSpacing;
                double y = height - 40 - (entries.get(i).getMoodLevel() * (height - 50) / 10);
                
                // Draw point
                gc.setFill(Color.web(MindDocTheme.PRIMARY));
                gc.fillOval(x - 3, y - 3, 6, 6);
                
                // Draw line to next point
                if (i < entries.size() - 1) {
                    double nextX = 40 + (i + 1) * pointSpacing;
                    double nextY = height - 40 - (entries.get(i + 1).getMoodLevel() * (height - 50) / 10);
                    gc.strokeLine(x, y, nextX, nextY);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error drawing mood chart", e);
        }
    }
    
    private void drawWeeklyStats() {
        try {
            List<MoodEntry> entries = moodEntryRepository.findByUserId(currentUserId);
            
            // Group by day of week
            Map<String, Double> dailyAverages = new LinkedHashMap<>();
            String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
            
            for (String day : days) {
                dailyAverages.put(day, 0.0);
            }
            
            // Calculate daily averages (simplified)
            for (MoodEntry entry : entries) {
                dailyAverages.put(days[0], 
                    (dailyAverages.get(days[0]) + entry.getMoodLevel()) / 2);
            }
            
            GraphicsContext gc = statsCanvas.getGraphicsContext2D();
            double width = statsCanvas.getWidth();
            double height = statsCanvas.getHeight();
            
            // Clear canvas
            gc.setFill(Color.WHITE);
            gc.fillRect(0, 0, width, height);
            
            // Draw bar chart
            double barWidth = width / 10;
            int index = 0;
            
            for (Map.Entry<String, Double> dayEntry : dailyAverages.entrySet()) {
                double barHeight = (dayEntry.getValue() * height) / 10;
                double x = 40 + index * barWidth;
                double y = height - barHeight;
                
                // Draw bar
                gc.setFill(Color.web(MindDocTheme.PRIMARY));
                gc.fillRect(x, y, barWidth - 5, barHeight);
                
                // Draw label
                gc.setFill(Color.web("#666"));
                gc.setFont(Font.font("System", 10));
                gc.fillText(dayEntry.getKey(), x + 5, height - 5);
            }
            
        } catch (SQLException e) {
            logger.error("Error drawing weekly stats", e);
        }
    }
    
    @Override
    public void refresh() {
        try {
            List<MoodEntry> entries = moodEntryRepository.findByUserId(currentUserId);
            
            if (entries.isEmpty()) {
                averageMoodLabel.setText("N/A");
                highestMoodLabel.setText("N/A");
                lowestMoodLabel.setText("N/A");
                entriesCountLabel.setText("0");
                return;
            }
            
            // Calculate statistics
            double average = entries.stream()
                .mapToInt(MoodEntry::getMoodLevel)
                .average()
                .orElse(0);
            
            int highest = entries.stream()
                .mapToInt(MoodEntry::getMoodLevel)
                .max()
                .orElse(0);
            
            int lowest = entries.stream()
                .mapToInt(MoodEntry::getMoodLevel)
                .min()
                .orElse(0);
            
            averageMoodLabel.setText(String.format("%.1f", average));
            highestMoodLabel.setText(String.valueOf(highest));
            lowestMoodLabel.setText(String.valueOf(lowest));
            entriesCountLabel.setText(String.valueOf(entries.size()));
            
            // Draw charts
            drawMoodChart();
            drawWeeklyStats();
            
        } catch (SQLException e) {
            logger.error("Error refreshing analytics", e);
        }
    }
}
