package com.mindoc.ui.dashboard;

import com.mindoc.ui.common.BasePanel;
import com.mindoc.ui.theme.MindDocTheme;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dashboard panel showing today's summary and quick stats
 */
public class DashboardPanel extends BasePanel {
    private static final Logger logger = LoggerFactory.getLogger(DashboardPanel.class);
    
    private Label todayMoodLabel;
    private Label todayNotesLabel;
    private Label weekMoodAverage;
    private Label streakLabel;
    
    public DashboardPanel(int currentUserId) {
        initializeUI();
        refresh();
    }
    
    private void initializeUI() {
        // Title
        Label titleLabel = new Label("Dashboard");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(javafx.scene.paint.Color.web(MindDocTheme.TEXT_PRIMARY));
        
        // Today's mood section
        VBox todaySection = createTodaySection();
        
        // Stats section
        VBox statsSection = createStatsSection();
        
        // Quick actions
        VBox quickActionsSection = createQuickActionsSection();
        
        getChildren().addAll(titleLabel, todaySection, statsSection, quickActionsSection);
    }
    
    private VBox createTodaySection() {
        VBox section = new VBox(10);
        section.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-color: white; -fx-padding: 15;");
        
        Label sectionTitle = new Label("Today's Mood");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        
        todayMoodLabel = new Label("No entry yet");
        todayMoodLabel.setFont(Font.font("Segoe UI", 36));
        
        todayNotesLabel = new Label("");
        todayNotesLabel.setFont(Font.font("Segoe UI", 13));
        todayNotesLabel.setTextFill(javafx.scene.paint.Color.web(MindDocTheme.TEXT_SECONDARY));
        todayNotesLabel.setWrapText(true);
        
        section.getChildren().addAll(sectionTitle, todayMoodLabel, todayNotesLabel);
        return section;
    }
    
    private VBox createStatsSection() {
        VBox section = new VBox(10);
        section.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-color: white; -fx-padding: 15;");
        
        Label sectionTitle = new Label("This Week");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        
        HBox statsBox = new HBox(20);
        
        VBox averageBox = new VBox(5);
        Label avgLabel = new Label("Average Mood");
        avgLabel.setFont(Font.font("Segoe UI", 12));
        averageBox.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-padding: 10;");
        weekMoodAverage = new Label("--");
        weekMoodAverage.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        averageBox.getChildren().addAll(avgLabel, weekMoodAverage);
        
        VBox streakBox = new VBox(5);
        Label streakLabelTitle = new Label("Current Streak");
        streakLabelTitle.setFont(Font.font("Segoe UI", 12));
        streakBox.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-padding: 10;");
        streakLabel = new Label("0 days");
        streakLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        streakBox.getChildren().addAll(streakLabelTitle, streakLabel);
        
        statsBox.getChildren().addAll(averageBox, streakBox);
        section.getChildren().addAll(sectionTitle, statsBox);
        return section;
    }
    
    private VBox createQuickActionsSection() {
        VBox section = new VBox(10);
        section.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-color: white; -fx-padding: 15;");
        
        Label sectionTitle = new Label("Quick Tips");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        
        Label tip1 = new Label("💡 Track your mood regularly to identify patterns");
        Label tip2 = new Label("🎓 Try a new course or exercise today");
        Label tip3 = new Label("🧘 Practice daily mindfulness for better wellbeing");
        
        section.getChildren().addAll(sectionTitle, tip1, tip2, tip3);
        return section;
    }
    
    @Override
    public void refresh() {
        try {
            // Update statistics
            updateStats();
            
            logger.info("Dashboard refreshed");
        } catch (Exception e) {
            logger.error("Error refreshing dashboard", e);
        }
    }
    
    private void updateStats() {
        // This will be implemented when we have repository access
        weekMoodAverage.setText("7.2/10");
        streakLabel.setText("5 days");
    }
}
