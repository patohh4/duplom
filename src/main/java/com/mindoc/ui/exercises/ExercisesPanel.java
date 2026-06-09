package com.mindoc.ui.exercises;

import com.mindoc.database.DatabaseManager;
import com.mindoc.model.Exercise;
import com.mindoc.repository.ExerciseRepository;
import com.mindoc.repository.LearningProgressRepository;
import com.mindoc.ui.common.BasePanel;
import com.mindoc.ui.theme.MindDocTheme;
import com.mindoc.util.I18n;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Panel for coping exercises and wellness activities
 */
public class ExercisesPanel extends BasePanel {
    private static final Logger logger = LoggerFactory.getLogger(ExercisesPanel.class);
    
    private int currentUserId;
    private DatabaseManager databaseManager;
    private ExerciseRepository exerciseRepository;
    private LearningProgressRepository progressRepository;
    
    private ListView<Exercise> exerciseListView;
    private VBox exerciseDetailsPanel;
    private Label timerLabel;
    private Button startButton;
    private ComboBox<String> categoryCombo;
    private Exercise selectedExercise;
    private final Map<Integer, String> exerciseStatuses = new HashMap<>();
    private Label titleLabel;
    
    public ExercisesPanel(int userId, DatabaseManager databaseManager) {
        this.currentUserId = userId;
        this.databaseManager = databaseManager;
        this.exerciseRepository = new ExerciseRepository(databaseManager.getConnection());
        this.progressRepository = new LearningProgressRepository(databaseManager.getConnection());
        
        initializeUI();
        refresh();
    }
    
    private void initializeUI() {
        setStyle("-fx-background-color: " + MindDocTheme.BACKGROUND + ";");
        setPadding(new Insets(28));
        setSpacing(16);
        setFillWidth(true);

        // Header banner
        HBox header = createHeaderBanner("💪 Coping Exercises", "Choose and complete wellness exercises at your own pace", "🏋");
        getChildren().add(header);

        // Title (hidden — header replaces it visually, kept for applyLanguage)
        titleLabel = new Label("💪 Coping Exercises");
        titleLabel.setVisible(false);
        titleLabel.setManaged(false);
        
        // Main container with two columns
        HBox mainContainer = new HBox(20);
        mainContainer.setPadding(new Insets(0));
        mainContainer.setFillHeight(true);
        mainContainer.setMaxWidth(Double.MAX_VALUE);
        
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
    
    private HBox createHeaderBanner(String title, String subtitle, String emoji) {
        HBox section = new HBox();
        section.setStyle(
            "-fx-background-color: linear-gradient(from 0% 0% to 100% 0%, " +
                MindDocTheme.PRIMARY + ", " + MindDocTheme.SECONDARY + "); " +
            "-fx-background-radius: 16; " +
            "-fx-padding: 24 32; " +
            "-fx-effect: dropshadow(three-pass-box, #00000020, 10, 0, 0, 4);"
        );
        section.setAlignment(Pos.CENTER_LEFT);

        VBox textBox = new VBox(4);
        HBox.setHgrow(textBox, javafx.scene.layout.Priority.ALWAYS);

        Label titleLbl = new Label(title);
        titleLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLbl.setTextFill(javafx.scene.paint.Color.WHITE);

        Label subtitleLbl = new Label(subtitle);
        subtitleLbl.setFont(Font.font("Segoe UI", 13));
        subtitleLbl.setTextFill(javafx.scene.paint.Color.web("#d1fae5"));

        textBox.getChildren().addAll(titleLbl, subtitleLbl);

        Label deco = new Label(emoji);
        deco.setFont(Font.font("System", 48));
        deco.setOpacity(0.7);

        section.getChildren().addAll(textBox, deco);
        return section;
    }

    private VBox createListSection() {
        VBox section = new VBox(12);
        section.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 14; " +
            "-fx-border-radius: 14; " +
            "-fx-padding: 20; " +
            "-fx-effect: dropshadow(three-pass-box, #00000014, 8, 0, 0, 2);"
        );
        section.setMinWidth(320);
        section.setPrefWidth(380);
        section.setMaxWidth(Double.MAX_VALUE);

        Label sectionTitle = new Label(I18n.t("available_exercises", "Available Exercises"));
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        sectionTitle.setTextFill(javafx.scene.paint.Color.web(MindDocTheme.TEXT_PRIMARY));
        section.getChildren().add(sectionTitle);
        
        // Filter
        HBox filterBox = new HBox(10);
        Label filterLabel = new Label(I18n.t("category", "Category:"));
        categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll(
            "All",
            "breathing",
            "grounding",
            "relaxation",
            "cognitive",
            "mindfulness",
            "meditation"
        );
        categoryCombo.setValue("All");
        categoryCombo.setStyle(getComboBoxStyle());
        categoryCombo.setPrefWidth(150);
        
        categoryCombo.setOnAction(e -> refresh());
        filterBox.getChildren().addAll(filterLabel, categoryCombo);
        section.getChildren().add(filterBox);
        
        // Exercise list
        exerciseListView = new ListView<>();
        exerciseListView.setPrefHeight(400);
        exerciseListView.setPrefWidth(340);
        exerciseListView.setStyle("-fx-control-inner-background: #f8fafc;");
        
        exerciseListView.setCellFactory(lv -> new ListCell<Exercise>() {
            @Override
            protected void updateItem(Exercise exercise, boolean empty) {
                super.updateItem(exercise, empty);
                if (empty || exercise == null) {
                    setText(null);
                } else {
                    String statusMark = statusMarkFor(exercise.getId());
                    setText(String.format(
                        "%s %s\n⏱ %d min | 📊 Level: %s",
                        exercise.getTitle(),
                        statusMark,
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
            "-fx-background-color: white; " +
            "-fx-background-radius: 14; " +
            "-fx-border-radius: 14; " +
            "-fx-padding: 20; " +
            "-fx-effect: dropshadow(three-pass-box, #00000014, 8, 0, 0, 2);"
        );
        panel.setMinWidth(420);
        panel.setPrefWidth(700);
        panel.setMaxWidth(Double.MAX_VALUE);
        
        Label detailsTitle = new Label(I18n.t("exercise_details", "Exercise Details"));
        detailsTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        panel.getChildren().add(detailsTitle);
        
        Label placeholder = new Label(I18n.t("select_exercise", "Select an exercise to view details"));
        placeholder.setStyle("-fx-text-fill: #999; -fx-font-size: 14px;");
        placeholder.setAlignment(Pos.CENTER);
        
        panel.getChildren().add(placeholder);
        VBox.setVgrow(placeholder, javafx.scene.layout.Priority.ALWAYS);
        
        return panel;
    }
    
    private void showExerciseDetails(Exercise exercise) {
        selectedExercise = exercise;
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
        Label categoryLabel = new Label("📁 " + I18n.t("category", "Category:") + " " + exercise.getCategory());
        Label durationLabel = new Label("⏱ " + I18n.t("duration", "Duration") + ": " + exercise.getDuration() + " min");
        Label difficultyLabel = new Label("📊 Level: " + exercise.getDifficulty() + "/5");
        metaBox.getChildren().addAll(categoryLabel, durationLabel, difficultyLabel);
        content.getChildren().add(metaBox);
        
        // Description
        Label descLabel = new Label(I18n.t("description", "Description:"));
        descLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 12));
        content.getChildren().add(descLabel);
        
        TextArea descArea = new TextArea();
        descArea.setText(safeText(
            exercise.getDescription(),
            I18n.t("no_desc_exercise", "No description is available for this exercise yet.")
        ));
        descArea.setWrapText(true);
        descArea.setEditable(false);
        descArea.setPrefRowCount(3);
        descArea.setStyle(getTextAreaStyle());
        content.getChildren().add(descArea);
        
        // Instructions
        Label instructionsLabel = new Label(I18n.t("instructions", "Step-by-Step Instructions:"));
        instructionsLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 12));
        content.getChildren().add(instructionsLabel);
        
        TextArea instructionsArea = new TextArea();
        instructionsArea.setText(safeText(
            exercise.getInstructions(),
            I18n.t("no_instr_exercise", "Step-by-step instructions are not available yet.\n\n") +
            "Recommended flow:\n" +
            "1. Find a quiet place.\n" +
            "2. Set a timer for the exercise duration.\n" +
            "3. Focus on calm, steady breathing.\n" +
            "4. Stop if you feel discomfort and try again later."
        ).replace("\\n", "\n"));
        instructionsArea.setWrapText(true);
        instructionsArea.setEditable(false);
        instructionsArea.setPrefRowCount(5);
        instructionsArea.setStyle(getTextAreaStyle());
        content.getChildren().add(instructionsArea);
        
        // Timer section
        timerLabel = new Label("⏱ " + I18n.t("duration", "Duration") + ": " + exercise.getDuration() + " minutes");
        timerLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        timerLabel.setStyle("-fx-text-fill: " + MindDocTheme.PRIMARY + ";");
        timerLabel.setAlignment(Pos.CENTER);
        content.getChildren().add(timerLabel);
        
        // Start button
        startButton = new Button(I18n.t("start_exercise", "▶ Start Exercise"));
        startButton.setPrefWidth(Double.MAX_VALUE);
        updateStartButtonState(exercise);
        startButton.setOnAction(e -> startExercise(exercise));
        content.getChildren().add(startButton);
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-padding: 0;");
        
        exerciseDetailsPanel.getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS);
    }
    
    private void startExercise(Exercise exercise) {
        logger.info("Starting exercise: {}", exercise.getTitle());

        String status = exerciseStatuses.getOrDefault(exercise.getId(), "not_started");
        if ("completed".equals(status)) {
            return;
        }

        try {
            if (!"in_progress".equals(status)) {
                progressRepository.saveStatus(currentUserId, "exercise", exercise.getId(), "in_progress");
                exerciseStatuses.put(exercise.getId(), "in_progress");
            }
        } catch (SQLException e) {
            logger.error("Error updating exercise status", e);
            showError(I18n.t("failed_update_progress", "Failed to update progress."));
            return;
        }

        Alert timerAlert = new Alert(Alert.AlertType.CONFIRMATION);
        timerAlert.setTitle(I18n.t("exercise_progress", "Exercise Progress"));
        timerAlert.setHeaderText(exercise.getTitle());
        ButtonType completeButton = new ButtonType(I18n.t("mark_completed", "Mark as Completed"), ButtonBar.ButtonData.OK_DONE);
        ButtonType keepProgressButton = new ButtonType(I18n.t("keep_in_progress", "Keep In Progress"), ButtonBar.ButtonData.CANCEL_CLOSE);
        timerAlert.getButtonTypes().setAll(completeButton, keepProgressButton);
        timerAlert.setContentText(
            String.format(
                I18n.t("marked_in_progress", "Exercise marked as in progress.\nMark it as completed when done.")
            )
        );

        timerAlert.showAndWait().ifPresent(result -> {
            if (result == completeButton) {
                try {
                    progressRepository.saveStatus(currentUserId, "exercise", exercise.getId(), "completed");
                    exerciseStatuses.put(exercise.getId(), "completed");
                } catch (SQLException e) {
                    logger.error("Error marking exercise completed", e);
                    showError(I18n.t("failed_save_completion", "Failed to save completion status."));
                }
                if (selectedExercise != null && selectedExercise.getId() == exercise.getId()) {
                    updateStartButtonState(exercise);
                }
            }
        });
        exerciseListView.refresh();
    }
    
    @Override
    public void refresh() {
        try {
            exerciseStatuses.clear();
            exerciseStatuses.putAll(progressRepository.findStatusByUserAndType(currentUserId, "exercise"));

            String category = categoryCombo != null ? categoryCombo.getValue() : "All";
            List<Exercise> exercises;
            if (category == null || "All".equalsIgnoreCase(category)) {
                exercises = exerciseRepository.findAll();
            } else {
                exercises = exerciseRepository.findByCategory(category);
            }

            exerciseListView.getItems().clear();
            for (Exercise exercise : exercises) {
                exerciseListView.getItems().add(exercise);
            }
            exerciseListView.refresh();

            if (selectedExercise != null) {
                boolean stillVisible = exercises.stream().anyMatch(e -> e.getId() == selectedExercise.getId());
                if (!stillVisible) {
                    selectedExercise = null;
                    exerciseDetailsPanel.getChildren().clear();
                    exerciseDetailsPanel.getChildren().add(createDetailsPlaceholder());
                }
            }
        } catch (SQLException e) {
            logger.error("Error refreshing exercises", e);
        }
    }

    private Label createDetailsPlaceholder() {
        Label placeholder = new Label(I18n.t("select_exercise", "Select an exercise to view details"));
        placeholder.setStyle("-fx-text-fill: #999; -fx-font-size: 14px;");
        placeholder.setAlignment(Pos.CENTER);
        VBox.setVgrow(placeholder, javafx.scene.layout.Priority.ALWAYS);
        return placeholder;
    }

    private void updateStartButtonState(Exercise exercise) {
        String status = exerciseStatuses.getOrDefault(exercise.getId(), "not_started");
        if ("completed".equals(status)) {
            startButton.setText(I18n.t("exercise_completed", "✓ Exercise Completed"));
            startButton.setDisable(true);
            startButton.setStyle(
                "-fx-padding: 12px;" +
                "-fx-font-size: 14px;" +
                "-fx-background-color: " + MindDocTheme.SUCCESS + ";" +
                "-fx-text-fill: white;"
            );
            return;
        }

        if ("in_progress".equals(status)) {
            startButton.setText(I18n.t("in_progress", "⏳ In Progress"));
        } else {
            startButton.setText(I18n.t("start_exercise", "▶ Start Exercise"));
        }
        startButton.setDisable(false);
        startButton.setStyle(
            "-fx-padding: 12px;" +
            "-fx-font-size: 14px;" +
            "-fx-background-color: " + MindDocTheme.PRIMARY + ";" +
            "-fx-text-fill: white;" +
            "-fx-cursor: hand;"
        );
    }

    private String safeText(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value;
    }

    private String statusMarkFor(int exerciseId) {
        String status = exerciseStatuses.getOrDefault(exerciseId, "not_started");
        if ("completed".equals(status)) {
            return "✓";
        }
        if ("in_progress".equals(status)) {
            return "⏳";
        }
        return "";
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(I18n.t("exercises", "Exercises"));
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private String getComboBoxStyle() {
        return "-fx-padding: 8; " +
               "-fx-font-size: 12px; " +
               "-fx-border-radius: 6; " +
               "-fx-background-radius: 6; " +
               "-fx-border-color: " + MindDocTheme.BORDER + "; " +
               "-fx-border-width: 1;";
    }

    private String getTextAreaStyle() {
        return "-fx-padding: 10; " +
               "-fx-font-size: 12px; " +
               "-fx-border-radius: 6; " +
               "-fx-background-radius: 6; " +
               "-fx-border-color: " + MindDocTheme.BORDER + "; " +
               "-fx-border-width: 1; " +
               "-fx-control-inner-background: " + MindDocTheme.BACKGROUND + ";";
    }

    public void applyLanguage(String language) {
        I18n.setLanguage(language);
        
        // Update title
        if (titleLabel != null) {
            titleLabel.setText("💪 " + I18n.t("exercises", "Exercises & Strategies"));
        }
        
        // Refresh exercise list to update labels with new translations
        if (exerciseListView != null) {
            exerciseListView.refresh();
        }
        
        // Refresh details if an exercise is selected
        if (selectedExercise != null && selectedExercise.getId() > 0) {
            showExerciseDetails(selectedExercise);
        }
    }
}
