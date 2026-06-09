package com.mindoc.ui.courses;

import com.mindoc.database.DatabaseManager;
import com.mindoc.model.CBTCourse;
import com.mindoc.repository.CBTCourseRepository;
import com.mindoc.repository.LearningProgressRepository;
import com.mindoc.ui.common.BasePanel;
import com.mindoc.ui.theme.MindDocTheme;
import com.mindoc.util.I18n;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Panel for CBT courses and learning content
 */
public class CoursesPanel extends BasePanel {
    private static final Logger logger = LoggerFactory.getLogger(CoursesPanel.class);
    
    private int currentUserId;
    private DatabaseManager databaseManager;
    private CBTCourseRepository courseRepository;
    private LearningProgressRepository progressRepository;
    
    private ListView<CBTCourse> courseListView;
    private VBox courseDetailsPanel;
    private ComboBox<String> categoryCombo;
    private CBTCourse selectedCourse;
    private Button startButton;
    private final Map<Integer, String> courseStatuses = new HashMap<>();
    private Label titleLabel;
    
    public CoursesPanel(int userId, DatabaseManager databaseManager) {
        this.currentUserId = userId;
        this.databaseManager = databaseManager;
        this.courseRepository = new CBTCourseRepository(databaseManager.getConnection());
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
        HBox header = createHeaderBanner("📚 CBT Learning Courses", "Explore evidence-based courses to support your mental wellbeing", "🎓");
        getChildren().add(header);

        // Title (hidden — header replaces it visually, kept for applyLanguage)
        titleLabel = new Label("📚 CBT Learning Courses");
        titleLabel.setVisible(false);
        titleLabel.setManaged(false);
        
        // Main container with two columns
        HBox mainContainer = new HBox(20);
        mainContainer.setPadding(new Insets(0));
        mainContainer.setFillHeight(true);
        mainContainer.setMaxWidth(Double.MAX_VALUE);
        
        // Left side - Course list
        VBox listSection = createListSection();
        mainContainer.getChildren().add(listSection);
        HBox.setHgrow(listSection, javafx.scene.layout.Priority.ALWAYS);
        
        // Right side - Course details
        courseDetailsPanel = createDetailsPanel();
        mainContainer.getChildren().add(courseDetailsPanel);
        HBox.setHgrow(courseDetailsPanel, javafx.scene.layout.Priority.ALWAYS);
        
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

        Label sectionTitle = new Label(I18n.t("available_courses", "Available Courses"));
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        sectionTitle.setTextFill(javafx.scene.paint.Color.web(MindDocTheme.TEXT_PRIMARY));
        section.getChildren().add(sectionTitle);
        
        // Filter
        HBox filterBox = new HBox(10);
        Label filterLabel = new Label(I18n.t("category", "Category:"));
        categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll("All", "depression", "anxiety", "stress", "sleep", "general");
        categoryCombo.setValue("All");
        categoryCombo.setStyle(getComboBoxStyle());
        categoryCombo.setPrefWidth(150);
        
        categoryCombo.setOnAction(e -> refresh());
        
        filterBox.getChildren().addAll(filterLabel, categoryCombo);
        section.getChildren().add(filterBox);
        
        // Course list
        courseListView = new ListView<>();
        courseListView.setPrefHeight(400);
        courseListView.setPrefWidth(340);
        courseListView.setStyle("-fx-control-inner-background: #f8fafc;");
        
        courseListView.setCellFactory(lv -> new ListCell<CBTCourse>() {
            @Override
            protected void updateItem(CBTCourse course, boolean empty) {
                super.updateItem(course, empty);
                if (empty || course == null) {
                    setText(null);
                } else {
                    String statusMark = statusMarkFor(course.getId());
                    setText(String.format(
                        "%s %s\n⏱ %d min | 📊 Difficulty: %d/5",
                        course.getTitle(),
                        statusMark,
                        course.getDuration(),
                        course.getDifficulty()
                    ));
                    setStyle("-fx-padding: 10px; -fx-cursor: hand;");
                }
            }
        });
        
        courseListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showCourseDetails(newVal);
            }
        });
        
        section.getChildren().add(courseListView);
        VBox.setVgrow(courseListView, javafx.scene.layout.Priority.ALWAYS);
        
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
        
        Label detailsTitle = new Label(I18n.t("course_details", "Course Details"));
        detailsTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        panel.getChildren().add(detailsTitle);
        
        Label placeholder = new Label(I18n.t("select_course", "Select a course to view details"));
        placeholder.setStyle("-fx-text-fill: #999; -fx-font-size: 14px;");
        placeholder.setAlignment(Pos.CENTER);
        
        panel.getChildren().add(placeholder);
        VBox.setVgrow(placeholder, javafx.scene.layout.Priority.ALWAYS);
        
        return panel;
    }
    
    private void showCourseDetails(CBTCourse course) {
        selectedCourse = course;
        courseDetailsPanel.getChildren().clear();
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(0));
        
        // Title
        Label titleLabel = new Label(course.getTitle());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setTextFill(javafx.scene.paint.Color.web(MindDocTheme.PRIMARY));
        content.getChildren().add(titleLabel);
        
        // Category and duration
        HBox metaBox = new HBox(20);
        Label categoryLabel = new Label("📁 " + I18n.t("category", "Category:") + " " + course.getCategory());
        Label durationLabel = new Label("⏱ " + I18n.t("duration", "Duration") + ": " + course.getDuration() + " min");
        Label difficultyLabel = new Label("📊 Difficulty: " + course.getDifficulty() + "/5");
        metaBox.getChildren().addAll(categoryLabel, durationLabel, difficultyLabel);
        content.getChildren().add(metaBox);
        
        // Description
        Label descLabel = new Label(I18n.t("description", "Description:"));
        descLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 12));
        content.getChildren().add(descLabel);
        
        TextArea descArea = new TextArea();
        descArea.setText(safeText(
            course.getDescription(),
            I18n.t("no_desc_course", "No description is available for this course yet.")
        ));
        descArea.setWrapText(true);
        descArea.setEditable(false);
        descArea.setPrefRowCount(4);
        descArea.setStyle(getTextAreaStyle());
        content.getChildren().add(descArea);
        
        // Content
        Label contentLabel = new Label(I18n.t("course_content", "Course Content:"));
        contentLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 12));
        content.getChildren().add(contentLabel);
        
        TextArea contentArea = new TextArea();
        contentArea.setText(safeText(
            course.getContent(),
            I18n.t("no_content_course", "Course content is not available yet. Please use the description and consult your therapist or trusted learning resources.")
        ).replace("\\n", "\n"));
        contentArea.setWrapText(true);
        contentArea.setEditable(false);
        contentArea.setPrefRowCount(6);
        contentArea.setStyle(getTextAreaStyle());
        content.getChildren().add(contentArea);
        
        // Start button
        startButton = new Button(I18n.t("start_course", "▶ Start Course"));
        startButton.setPrefWidth(Double.MAX_VALUE);
        updateStartButtonState(course);
        startButton.setOnAction(e -> {
            startCourse(course);
        });
        content.getChildren().add(startButton);
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-padding: 0;");
        
        courseDetailsPanel.getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS);
    }
    
    @Override
    public void refresh() {
        try {
            courseStatuses.clear();
            courseStatuses.putAll(progressRepository.findStatusByUserAndType(currentUserId, "course"));

            String category = categoryCombo != null ? categoryCombo.getValue() : "All";
            List<CBTCourse> courses;
            if (category == null || "All".equalsIgnoreCase(category)) {
                courses = courseRepository.findAll();
            } else {
                courses = courseRepository.findByCategory(category);
            }
            courseListView.getItems().clear();
            for (CBTCourse course : courses) {
                courseListView.getItems().add(course);
            }
            courseListView.refresh();

            if (selectedCourse != null && startButton != null) {
                updateStartButtonState(selectedCourse);
            }
        } catch (SQLException e) {
            logger.error("Error refreshing courses", e);
        }
    }

    private String safeText(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value;
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
    
    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(I18n.t("courses_title", "Courses"));
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void startCourse(CBTCourse course) {
        String status = courseStatuses.getOrDefault(course.getId(), "not_started");
        if ("completed".equals(status)) {
            return;
        }

        try {
            if (!"in_progress".equals(status)) {
                progressRepository.saveStatus(currentUserId, "course", course.getId(), "in_progress");
                courseStatuses.put(course.getId(), "in_progress");
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(I18n.t("course_progress", "Course Progress"));
            alert.setHeaderText(course.getTitle());
            alert.setContentText(I18n.t("marked_in_progress", "Course marked as in progress.\nMark it as completed when done."));
            ButtonType completedButton = new ButtonType(I18n.t("mark_completed", "Mark Completed"), ButtonBar.ButtonData.OK_DONE);
            ButtonType keepProgressButton = new ButtonType(I18n.t("keep_in_progress", "Keep In Progress"), ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(completedButton, keepProgressButton);
            alert.showAndWait().ifPresent(result -> {
                if (result == completedButton) {
                    try {
                        progressRepository.saveStatus(currentUserId, "course", course.getId(), "completed");
                        courseStatuses.put(course.getId(), "completed");
                    } catch (SQLException e) {
                        logger.error("Error updating course status", e);
                    }
                }
            });

            updateStartButtonState(course);
            courseListView.refresh();
        } catch (SQLException e) {
            logger.error("Error starting course", e);
            showAlert(I18n.t("failed_update_progress", "Failed to update progress."), Alert.AlertType.ERROR);
        }
    }

    private void updateStartButtonState(CBTCourse course) {
        if (startButton == null) {
            return;
        }

        String status = courseStatuses.getOrDefault(course.getId(), "not_started");
        if ("completed".equals(status)) {
            startButton.setText(I18n.t("course_completed", "✓ Course Completed"));
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
            startButton.setText(I18n.t("start_course", "▶ Start Course"));
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

    private String statusMarkFor(int courseId) {
        String status = courseStatuses.getOrDefault(courseId, "not_started");
        if ("completed".equals(status)) {
            return "✓";
        }
        if ("in_progress".equals(status)) {
            return "⏳";
        }
        return "";
    }

    public void applyLanguage(String language) {
        I18n.setLanguage(language);
        
        // Update title
        if (titleLabel != null) {
            titleLabel.setText("📚 " + I18n.t("learn_courses", "CBT Learning Courses"));
        }
        
        // Refresh course list to update labels with new translations
        if (courseListView != null) {
            courseListView.refresh();
        }
        
        // Refresh details if a course is selected
        if (selectedCourse != null && selectedCourse.getId() > 0) {
            showCourseDetails(selectedCourse);
        }
    }
}
