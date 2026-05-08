package com.mindoc.ui.exercises;

import com.mindoc.database.DatabaseManager;
import com.mindoc.model.Exercise;
import com.mindoc.repository.ExerciseRepository;
import com.mindoc.ui.common.BasePanel;
import com.mindoc.ui.theme.MindDocTheme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

/**
 * Panel for coping exercises and wellness activities
 */
public class ExercisesPanel extends BasePanel {
    private static final Logger logger = LoggerFactory.getLogger(ExercisesPanel.class);
    
    private int currentUserId;
    private DatabaseManager databaseManager;
    private ExerciseRepository exerciseRepository;
    
    private ListView<Exercise> exerciseListView;
    private VBox exerciseDetailsPanel;
    private Label timerLabel;
    private Button startButton;
    
    public ExercisesPanel(int userId, DatabaseManager databaseManager) {
        this.currentUserId = userId;
        this.databaseManager = databaseManager;
        this.exerciseRepository = new ExerciseRepository(databaseManager.getConnection());
        
        initializeUI();
        refresh();
    }
    
    private void initializeUI() {
        setStyle("-fx-background-color: " + MindDocTheme.BACKGROUND + ";");
        setPadding(new Insets(20));
        setSpacing(15);
        
        // Title
        Label titleLabel = new Label("💪 Coping Exercises");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setTextFill(javafx.scene.paint.Color.web(MindDocTheme.PRIMARY));
        getChildren().add(titleLabel);
        
        // Main container with two columns
        HBox mainContainer = new HBox(20);
        mainContainer.setPadding(new Insets(0));
        
        // Left side - Exercise list
        VBox listSection = createListSection();
        mainContainer.getChildren().add(listSection);
        HBox.setHgrow(listSection, javafx.scene.layout.Priority.ALWAYS);
        
        // Right side - Exercise details
        exerciseDetailsPanel = createDetailsPanel();
        mainContainer.getChildren().add(exerciseDetailsPanel);
        HBox.setHgrow(exerciseDetailsPanel, javafx.scene.layout.Priority.ALWAYS);
        
        getChildren().add(mainContainer);
        VBox.setVgrow(mainContainer, javafx.scene.layout.Priority.ALWAYS);
    }
    
    private VBox createListSection() {
        VBox section = new VBox(10);
        section.setStyle(
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-radius: 8;" +
            "-fx-padding: 20;" +
            "-fx-background-color: white;"
        );
        
        Label sectionTitle = new Label("Available Exercises");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        section.getChildren().add(sectionTitle);
        
        // Filter
        HBox filterBox = new HBox(10);
        Label filterLabel = new Label("Category:");
        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll("All", "breathing", "meditation", "grounding", "movement");
        categoryCombo.setValue("All");
        categoryCombo.setStyle(getComboBoxStyle());
        categoryCombo.setPrefWidth(150);
        
        categoryCombo.setOnAction(e -> refresh());
        filterBox.getChildren().addAll(filterLabel, categoryCombo);
        section.getChildren().add(filterBox);
        
        // Exercise list
        exerciseListView = new ListView<>();
        exerciseListView.setPrefHeight(400);
        exerciseListView.setStyle("-fx-control-inner-background: #f8fafc;");
        
        exerciseListView.setCellFactory(lv -> new ListCell<Exercise>() {
            @Override
            protected void updateItem(Exercise exercise, boolean empty) {
                super.updateItem(exercise, empty);
                if (empty || exercise == null) {
                    setText(null);
                } else {
                    setText(String.format(
                        "%s\n⏱ %d min | 📊 Level: %s",
                        exercise.getTitle(),
                        exercise.getDuration(),
                        exercise.getDifficulty()
                    ));
                    setStyle("-fx-padding: 10px; -fx-cursor: hand;");
                }
            }
        });
        
        exerciseListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showExerciseDetails(newVal);
            }
        });
        
        section.getChildren().add(exerciseListView);
        VBox.setVgrow(exerciseListView, javafx.scene.layout.Priority.ALWAYS);
        
        return section;
    }
    
    private VBox createDetailsPanel() {
        VBox panel = new VBox(15);
        panel.setStyle(
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-radius: 8;" +
            "-fx-padding: 20;" +
            "-fx-background-color: white;"
        );
        
        Label detailsTitle = new Label("Exercise Details");
        detailsTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        panel.getChildren().add(detailsTitle);
        
        Label placeholder = new Label("Select an exercise to view details");
        placeholder.setStyle("-fx-text-fill: #999; -fx-font-size: 14px;");
        placeholder.setAlignment(Pos.CENTER);
        
        panel.getChildren().add(placeholder);
        VBox.setVgrow(placeholder, javafx.scene.layout.Priority.ALWAYS);
        
        return panel;
    }
    
    private void showExerciseDetails(Exercise exercise) {
        exerciseDetailsPanel.getChildren().clear();
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(0));
        
        // Title
        Label titleLabel = new Label(exercise.getTitle());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setTextFill(javafx.scene.paint.Color.web(MindDocTheme.PRIMARY));
        content.getChildren().add(titleLabel);
        
        // Metadata
        HBox metaBox = new HBox(20);
        Label categoryLabel = new Label("📁 Category: " + exercise.getCategory());
        Label durationLabel = new Label("⏱ Duration: " + exercise.getDuration() + " min");
        Label difficultyLabel = new Label("📊 Level: " + exercise.getDifficulty() + "/5");
        metaBox.getChildren().addAll(categoryLabel, durationLabel, difficultyLabel);
        content.getChildren().add(metaBox);
        
        // Description
        Label descLabel = new Label("Description:");
        descLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 12));
        content.getChildren().add(descLabel);
        
        TextArea descArea = new TextArea();
        descArea.setText(exercise.getDescription());
        descArea.setWrapText(true);
        descArea.setEditable(false);
        descArea.setPrefRowCount(3);
        descArea.setStyle(getTextAreaStyle());
        content.getChildren().add(descArea);
        
        // Instructions
        Label instructionsLabel = new Label("Step-by-Step Instructions:");
        instructionsLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 12));
        content.getChildren().add(instructionsLabel);
        
        TextArea instructionsArea = new TextArea();
        instructionsArea.setText(exercise.getInstructions());
        instructionsArea.setWrapText(true);
        instructionsArea.setEditable(false);
        instructionsArea.setPrefRowCount(5);
        instructionsArea.setStyle(getTextAreaStyle());
        content.getChildren().add(instructionsArea);
        
        // Timer section
        timerLabel = new Label("⏱ Duration: " + exercise.getDuration() + " minutes");
        timerLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        timerLabel.setStyle("-fx-text-fill: " + MindDocTheme.PRIMARY + ";");
        timerLabel.setAlignment(Pos.CENTER);
        content.getChildren().add(timerLabel);
        
        // Start button
        startButton = new Button("▶ Start Exercise");
        startButton.setPrefWidth(Double.MAX_VALUE);
        startButton.setStyle(
            "-fx-padding: 12px;" +
            "-fx-font-size: 14px;" +
            "-fx-background-color: " + MindDocTheme.PRIMARY + ";" +
            "-fx-text-fill: white;" +
            "-fx-cursor: hand;"
        );
        startButton.setOnAction(e -> startExercise(exercise));
        content.getChildren().add(startButton);
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-padding: 0;");
        
        exerciseDetailsPanel.getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS);
    }
    
    private void startExercise(Exercise exercise) {
        logger.info("Starting exercise: {}", exercise.getTitle());
        
        // Show timer dialog
        Alert timerAlert = new Alert(Alert.AlertType.INFORMATION);
        timerAlert.setTitle("Exercise in Progress");
        timerAlert.setHeaderText(exercise.getTitle());
        timerAlert.setContentText(
            String.format(
                "🎯 Exercise Duration: %d minutes\n\n" +
                "Follow the instructions above and complete this exercise.\n" +
                "You'll be notified when time is up!\n\n" +
                "🏆 Great job for taking care of your mental health!",
                exercise.getDuration()
            )
        );
        
        startButton.setText("✓ Exercise Completed");
        startButton.setStyle(
            "-fx-padding: 12px;" +
            "-fx-font-size: 14px;" +
            "-fx-background-color: " + MindDocTheme.SUCCESS + ";" +
            "-fx-text-fill: white;" +
            "-fx-cursor: hand;"
        );
        
        timerAlert.showAndWait();
    }
    
    @Override
    public void refresh() {
        try {
            List<Exercise> exercises = exerciseRepository.findAll();
            exerciseListView.getItems().clear();
            for (Exercise exercise : exercises) {
                exerciseListView.getItems().add(exercise);
            }
        } catch (SQLException e) {
            logger.error("Error refreshing exercises", e);
        }
    }
    
    private String getComboBoxStyle() {
        return "-fx-padding: 8px;" +
               "-fx-font-size: 12px;" +
               "-fx-border-radius: 5;" +
               "-fx-border-color: #e0e0e0;";
    }
    
    private String getTextAreaStyle() {
        return "-fx-padding: 10px;" +
               "-fx-font-size: 12px;" +
               "-fx-border-radius: 5;" +
               "-fx-border-color: #e0e0e0;" +
               "-fx-control-inner-background: #f8fafc;";
    }
}
