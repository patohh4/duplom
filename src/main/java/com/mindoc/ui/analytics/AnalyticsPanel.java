package com.mindoc.ui.analytics;

import com.mindoc.database.DatabaseManager;
import com.mindoc.model.MoodEntry;
import com.mindoc.repository.MoodEntryRepository;
import com.mindoc.ui.common.BasePanel;
import com.mindoc.ui.theme.MindDocTheme;
import com.mindoc.util.I18n;
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
    private Label titleLabel;
    
    public AnalyticsPanel(int userId, DatabaseManager databaseManager) {
        this.currentUserId = userId;
        this.databaseManager = databaseManager;
        this.moodEntryRepository = new MoodEntryRepository(databaseManager.getConnection());
        
        initializeUI();
        refresh();
    }
    
    private void initializeUI() {
        setStyle("-fx-background-color: " + MindDocTheme.BACKGROUND + ";");
        setPadding(new Insets(28));
        setSpacing(16);

        // Header banner
        HBox header = createHeaderBanner();
        titleLabel = null;
        getChildren().add(header);

        // Title label (hidden, kept for applyLanguage)
        titleLabel = new Label("📊 Analytics & Statistics");
        titleLabel.setVisible(false);
        titleLabel.setManaged(false);
        getChildren().add(titleLabel);

        // Stats cards
        HBox statsContainer = createStatsCards();
        getChildren().add(statsContainer);

        // Chart section
        VBox chartSection = createChartSection();
        getChildren().add(chartSection);
        VBox.setVgrow(chartSection, javafx.scene.layout.Priority.ALWAYS);
    }

    private HBox createHeaderBanner() {
        HBox section = new HBox();
        section.setStyle(
            "-fx-background-color: linear-gradient(from 0% 0% to 100% 0%, " +
                MindDocTheme.PRIMARY + ", " + MindDocTheme.SECONDARY + "); " +
            "-fx-background-radius: 16; " +
            "-fx-padding: 24 32; " +
            "-fx-effect: dropshadow(three-pass-box, #00000020, 10, 0, 0, 4);"
        );
        section.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        VBox textBox = new VBox(4);
        HBox.setHgrow(textBox, javafx.scene.layout.Priority.ALWAYS);

        Label title = new Label("📊 Analytics & Statistics");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(javafx.scene.paint.Color.WHITE);

        Label subtitle = new Label("Insights from your mood and wellness data");
        subtitle.setFont(Font.font("Segoe UI", 13));
        subtitle.setTextFill(javafx.scene.paint.Color.web("#d1fae5"));

        textBox.getChildren().addAll(title, subtitle);

        Label deco = new Label("📈");
        deco.setFont(Font.font("System", 48));
        deco.setOpacity(0.7);

        section.getChildren().addAll(textBox, deco);
        return section;
    }

    private HBox createStatsCards() {
        HBox container = new HBox(16);
        container.setPadding(new Insets(0));

        Label[] labels = new Label[4];

        VBox card1 = createStatCard("😊 " + I18n.t("average_mood", "Average Mood"), MindDocTheme.PRIMARY, labels, 0);
        averageMoodLabel = labels[0];
        container.getChildren().add(card1);
        HBox.setHgrow(card1, javafx.scene.layout.Priority.ALWAYS);

        VBox card2 = createStatCard(I18n.t("best_mood", "🏆 Best Mood"), MindDocTheme.SUCCESS, labels, 1);
        highestMoodLabel = labels[1];
        container.getChildren().add(card2);
        HBox.setHgrow(card2, javafx.scene.layout.Priority.ALWAYS);

        VBox card3 = createStatCard(I18n.t("tough_day", "📉 Tough Day"), MindDocTheme.WARNING, labels, 2);
        lowestMoodLabel = labels[2];
        container.getChildren().add(card3);
        HBox.setHgrow(card3, javafx.scene.layout.Priority.ALWAYS);

        VBox card4 = createStatCard(I18n.t("total_entries", "📝 Total Entries"), MindDocTheme.INFO, labels, 3);
        entriesCountLabel = labels[3];
        container.getChildren().add(card4);
        HBox.setHgrow(card4, javafx.scene.layout.Priority.ALWAYS);

        return container;
    }

    private VBox createStatCard(String title, String accentColor, Label[] out, int idx) {
        // Accent bar at top
        javafx.scene.layout.Region bar = new javafx.scene.layout.Region();
        bar.setPrefHeight(4);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setStyle("-fx-background-color: " + accentColor + "; -fx-background-radius: 14 14 0 0;");

        VBox inner = new VBox(6);
        inner.setPadding(new Insets(14, 18, 18, 18));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        titleLabel.setTextFill(javafx.scene.paint.Color.web(MindDocTheme.TEXT_SECONDARY));

        Label valueLabel = new Label("--");
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        valueLabel.setTextFill(javafx.scene.paint.Color.web(accentColor));
        out[idx] = valueLabel;

        inner.getChildren().addAll(titleLabel, valueLabel);

        VBox card = new VBox(0);
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 14; " +
            "-fx-border-radius: 14; " +
            "-fx-effect: dropshadow(three-pass-box, #00000014, 8, 0, 0, 2);"
        );
        card.getChildren().addAll(bar, inner);
        return card;
    }

    private VBox createChartSection() {
        VBox section = new VBox(15);
        section.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 14; " +
            "-fx-border-radius: 14; " +
            "-fx-padding: 24; " +
            "-fx-effect: dropshadow(three-pass-box, #00000014, 8, 0, 0, 2);"
        );
        
        Label chartTitle = new Label(I18n.t("analytics_trend", "Mood Trend (Last 30 Days)"));
        chartTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        section.getChildren().add(chartTitle);
        
        // Chart canvas
        moodChartCanvas = new Canvas(800, 250);
        section.getChildren().add(moodChartCanvas);
        VBox.setVgrow(moodChartCanvas, javafx.scene.layout.Priority.ALWAYS);
        
        // Stats canvas
        Label statsTitle = new Label(I18n.t("weekly_summary", "Weekly Summary"));
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
                gc.fillText(I18n.t("no_mood_data", "No mood data available yet"), width / 2 - 60, height / 2);
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
            Map<String, List<Double>> dailyValues = new LinkedHashMap<>();
            String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
            
            for (String day : days) {
                dailyValues.put(day, new ArrayList<>());
            }
            
            // Calculate daily averages properly
            LocalDate today = LocalDate.now();
            for (MoodEntry entry : entries) {
                try {
                    LocalDate entryDate = entry.getEntryDate();
                    int dayOfWeek = entryDate.getDayOfWeek().getValue() - 1;
                    if (dayOfWeek >= 0 && dayOfWeek < 7) {
                        dailyValues.get(days[dayOfWeek]).add((double) entry.getMoodLevel());
                    }
                } catch (Exception e) {
                    logger.warn("Skipping invalid entry date");
                }
            }
            
            GraphicsContext gc = statsCanvas.getGraphicsContext2D();
            double width = statsCanvas.getWidth();
            double height = statsCanvas.getHeight();
            
            // Clear canvas
            gc.setFill(Color.WHITE);
            gc.fillRect(0, 0, width, height);
            
            // Draw bar chart
            double barWidth = (width - 60) / 7;
            int index = 0;
            
            for (Map.Entry<String, List<Double>> dayEntry : dailyValues.entrySet()) {
                double average = dayEntry.getValue().isEmpty() ? 0 : 
                    dayEntry.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0);
                double barHeight = (average * (height - 60)) / 10;
                double x = 40 + index * barWidth;
                double y = height - 40 - barHeight;
                
                // Draw bar
                gc.setFill(Color.web(MindDocTheme.PRIMARY));
                gc.fillRect(x + 2, y, barWidth - 5, barHeight);
                
                // Draw label
                gc.setFill(Color.web("#666"));
                gc.setFont(Font.font("System", 10));
                gc.fillText(dayEntry.getKey(), x + 8, height - 20);
                
                // Draw value on top of bar if exists
                if (average > 0) {
                    gc.setFill(Color.web(MindDocTheme.PRIMARY));
                    gc.setFont(Font.font("System", 10));
                    gc.fillText(String.format("%.1f", average), x + 5, y - 5);
                }
                
                index++;
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
            applyLanguage(languageFromI18n());
            
        } catch (SQLException e) {
            logger.error("Error refreshing analytics", e);
        }
    }

    public void applyLanguage(String language) {
        if (titleLabel != null) {
            titleLabel.setText(I18n.t("analytics", "📊 Analytics & Statistics"));
        }
    }

    private String languageFromI18n() {
        return I18n.isUkrainian() ? "Українська" : "English";
    }
}
