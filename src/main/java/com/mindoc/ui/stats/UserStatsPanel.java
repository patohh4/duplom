package com.mindoc.ui.stats;

import com.mindoc.ui.common.BasePanel;
import com.mindoc.ui.theme.MindDocTheme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User Statistics and Activity Panel
 */
public class UserStatsPanel extends BasePanel {
    private static final Logger logger = LoggerFactory.getLogger(UserStatsPanel.class);
    
    private int currentUserId;
    private Label moodEntriesCount;
    private Label symptomLogsCount;
    private Label exercisesCompletedCount;
    private Label coursesStartedCount;
    private Label currentStreakLabel;
    private Label averageMoodLabel;
    
    public UserStatsPanel(int userId) {
        this.currentUserId = userId;
        initializeUI();
        refresh();
    }
    
    private void initializeUI() {
        setSpacing(20);
        setPadding(new Insets(20));
        
        // Title
        Label titleLabel = new Label("📊 Your Statistics");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web(MindDocTheme.PRIMARY));
        getChildren().add(titleLabel);
        
        // Stats grid
        HBox statsGrid = createStatsGrid();
        getChildren().add(statsGrid);
        
        // Achievements section
        VBox achievementsSection = createAchievementsSection();
        getChildren().add(achievementsSection);
        
        // Activity summary
        VBox activitySection = createActivitySection();
        getChildren().add(activitySection);
    }
    
    private HBox createStatsGrid() {
        HBox gridBox = new HBox(15);
        gridBox.setPadding(new Insets(15));
        gridBox.setSpacing(15);
        
        // Mood Entries
        VBox moodCard = createStatCard(
            "😊 Mood Entries",
            "0",
            "You have logged",
            MindDocTheme.PRIMARY
        );
        moodEntriesCount = (Label) moodCard.lookup("*");
        
        // Symptom Logs
        VBox symptomCard = createStatCard(
            "🩺 Symptoms",
            "0",
            "Total tracked",
            "#ff9800"
        );
        symptomLogsCount = (Label) symptomCard.lookup("*");
        
        // Exercises
        VBox exerciseCard = createStatCard(
            "💪 Exercises",
            "0",
            "Completed",
            MindDocTheme.SUCCESS
        );
        exercisesCompletedCount = (Label) exerciseCard.lookup("*");
        
        // Courses
        VBox courseCard = createStatCard(
            "📚 Courses",
            "0",
            "Started",
            "#9c27b0"
        );
        coursesStartedCount = (Label) courseCard.lookup("*");
        
        gridBox.getChildren().addAll(moodCard, symptomCard, exerciseCard, courseCard);
        return gridBox;
    }
    
    private VBox createStatCard(String title, String value, String subtitle, String color) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setStyle(
            "-fx-border-radius: 10;" +
            "-fx-background-color: white;" +
            "-fx-border-color: " + color + ";" +
            "-fx-border-width: 2;"
        );
        card.setPrefWidth(160);
        card.setAlignment(Pos.CENTER);
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        titleLabel.setTextFill(Color.web("#333"));
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        valueLabel.setTextFill(Color.web(color));
        
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setFont(Font.font("System", 11));
        subtitleLabel.setTextFill(Color.web("#999"));
        
        card.getChildren().addAll(titleLabel, valueLabel, subtitleLabel);
        return card;
    }
    
    private VBox createAchievementsSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));
        section.setStyle(
            "-fx-border-radius: 10;" +
            "-fx-border-color: #e0e0e0;" +
            "-fx-border-width: 1;" +
            "-fx-background-color: #f9f9f9;"
        );
        
        Label sectionTitle = new Label("🏆 Achievements");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        sectionTitle.setTextFill(Color.web(MindDocTheme.PRIMARY));
        section.getChildren().add(sectionTitle);
        
        // Streak achievement
        HBox streakBox = new HBox(15);
        streakBox.setAlignment(Pos.CENTER_LEFT);
        streakBox.setPadding(new Insets(10));
        
        Circle streakCircle = new Circle(25);
        streakCircle.setFill(Color.web(MindDocTheme.PRIMARY));
        Label streakEmoji = new Label("🔥");
        streakEmoji.setFont(Font.font("System", 24));
        
        VBox streakInfo = new VBox(5);
        Label streakTitle = new Label("Mood Logging Streak");
        streakTitle.setFont(Font.font("System", FontWeight.BOLD, 13));
        currentStreakLabel = new Label("0 days");
        currentStreakLabel.setFont(Font.font("System", 12));
        currentStreakLabel.setTextFill(Color.web("#666"));
        streakInfo.getChildren().addAll(streakTitle, currentStreakLabel);
        
        streakBox.getChildren().addAll(streakCircle, streakInfo);
        section.getChildren().add(streakBox);
        
        // Average mood achievement
        HBox averageBox = new HBox(15);
        averageBox.setAlignment(Pos.CENTER_LEFT);
        averageBox.setPadding(new Insets(10));
        
        Circle averageCircle = new Circle(25);
        averageCircle.setFill(Color.web("#4caf50"));
        Label averageEmoji = new Label("😊");
        averageEmoji.setFont(Font.font("System", 24));
        
        VBox averageInfo = new VBox(5);
        Label averageTitle = new Label("Average Mood Level");
        averageTitle.setFont(Font.font("System", FontWeight.BOLD, 13));
        averageMoodLabel = new Label("Not available");
        averageMoodLabel.setFont(Font.font("System", 12));
        averageMoodLabel.setTextFill(Color.web("#666"));
        averageInfo.getChildren().addAll(averageTitle, averageMoodLabel);
        
        averageBox.getChildren().addAll(averageCircle, averageInfo);
        section.getChildren().add(averageBox);
        
        return section;
    }
    
    private VBox createActivitySection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));
        section.setStyle(
            "-fx-border-radius: 10;" +
            "-fx-border-color: #e0e0e0;" +
            "-fx-border-width: 1;" +
            "-fx-background-color: #f9f9f9;"
        );
        
        Label sectionTitle = new Label("📈 Activity Summary");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        sectionTitle.setTextFill(Color.web(MindDocTheme.PRIMARY));
        section.getChildren().add(sectionTitle);
        
        // Activity stats
        VBox activityStats = new VBox(10);
        activityStats.setPadding(new Insets(10));
        
        Label thisWeekLabel = new Label("📅 This Week: 5 mood entries logged");
        thisWeekLabel.setFont(Font.font("System", 12));
        
        Label thisMonthLabel = new Label("📊 This Month: 18 mood entries logged");
        thisMonthLabel.setFont(Font.font("System", 12));
        
        Label totalLabel = new Label("⭐ All Time: 45 mood entries logged");
        totalLabel.setFont(Font.font("System", 12));
        
        activityStats.getChildren().addAll(thisWeekLabel, thisMonthLabel, totalLabel);
        section.getChildren().add(activityStats);
        
        return section;
    }
    
    public void refresh() {
        logger.info("Refreshing statistics for user: {}", currentUserId);
        // TODO: Load actual statistics from database
        
        // Placeholder data
        moodEntriesCount.setText("0");
        symptomLogsCount.setText("0");
        exercisesCompletedCount.setText("0");
        coursesStartedCount.setText("0");
        currentStreakLabel.setText("0 days");
        averageMoodLabel.setText("Not available");
    }
}
