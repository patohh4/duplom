package com.mindoc.ui.courses;

import com.mindoc.database.DatabaseManager;
import com.mindoc.model.CBTCourse;
import com.mindoc.repository.CBTCourseRepository;
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
 * Panel for CBT courses and learning content
 */
public class CoursesPanel extends BasePanel {
    private static final Logger logger = LoggerFactory.getLogger(CoursesPanel.class);
    
    private int currentUserId;
    private DatabaseManager databaseManager;
    private CBTCourseRepository courseRepository;
    
    private ListView<CBTCourse> courseListView;
    private VBox courseDetailsPanel;
    
    public CoursesPanel(int userId, DatabaseManager databaseManager) {
        this.currentUserId = userId;
        this.databaseManager = databaseManager;
        this.courseRepository = new CBTCourseRepository(databaseManager.getConnection());
        
        initializeUI();
        refresh();
    }
    
    private void initializeUI() {
        setStyle("-fx-background-color: " + MindDocTheme.BACKGROUND + ";");
        setPadding(new Insets(20));
        setSpacing(15);
        
        // Title
        Label titleLabel = new Label("📚 CBT Learning Courses");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setTextFill(javafx.scene.paint.Color.web(MindDocTheme.PRIMARY));
        getChildren().add(titleLabel);
        
        // Main container with two columns
        HBox mainContainer = new HBox(20);
        mainContainer.setPadding(new Insets(0));
        
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
    
    private VBox createListSection() {
        VBox section = new VBox(10);
        section.setStyle(
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-radius: 8;" +
            "-fx-padding: 20;" +
            "-fx-background-color: white;"
        );
        
        Label sectionTitle = new Label("Available Courses");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        section.getChildren().add(sectionTitle);
        
        // Filter
        HBox filterBox = new HBox(10);
        Label filterLabel = new Label("Category:");
        ComboBox<String> categoryCombo = new ComboBox<>();
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
        courseListView.setStyle("-fx-control-inner-background: #f8fafc;");
        
        courseListView.setCellFactory(lv -> new ListCell<CBTCourse>() {
            @Override
            protected void updateItem(CBTCourse course, boolean empty) {
                super.updateItem(course, empty);
                if (empty || course == null) {
                    setText(null);
                } else {
                    setText(String.format(
                        "%s\n⏱ %d min | 📊 Difficulty: %d/5",
                        course.getTitle(),
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
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-radius: 8;" +
            "-fx-padding: 20;" +
            "-fx-background-color: white;"
        );
        
        Label detailsTitle = new Label("Course Details");
        detailsTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        panel.getChildren().add(detailsTitle);
        
        Label placeholder = new Label("Select a course to view details");
        placeholder.setStyle("-fx-text-fill: #999; -fx-font-size: 14px;");
        placeholder.setAlignment(Pos.CENTER);
        
        panel.getChildren().add(placeholder);
        VBox.setVgrow(placeholder, javafx.scene.layout.Priority.ALWAYS);
        
        return panel;
    }
    
    private void showCourseDetails(CBTCourse course) {
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
        Label categoryLabel = new Label("📁 Category: " + course.getCategory());
        Label durationLabel = new Label("⏱ Duration: " + course.getDuration() + " min");
        Label difficultyLabel = new Label("📊 Difficulty: " + course.getDifficulty() + "/5");
        metaBox.getChildren().addAll(categoryLabel, durationLabel, difficultyLabel);
        content.getChildren().add(metaBox);
        
        // Description
        Label descLabel = new Label("Description:");
        descLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 12));
        content.getChildren().add(descLabel);
        
        TextArea descArea = new TextArea();
        descArea.setText(course.getDescription());
        descArea.setWrapText(true);
        descArea.setEditable(false);
        descArea.setPrefRowCount(4);
        descArea.setStyle(getTextAreaStyle());
        content.getChildren().add(descArea);
        
        // Content
        Label contentLabel = new Label("Course Content:");
        contentLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 12));
        content.getChildren().add(contentLabel);
        
        TextArea contentArea = new TextArea();
        contentArea.setText(course.getContent());
        contentArea.setWrapText(true);
        contentArea.setEditable(false);
        contentArea.setPrefRowCount(6);
        contentArea.setStyle(getTextAreaStyle());
        content.getChildren().add(contentArea);
        
        // Start button
        Button startButton = new Button("Start Course");
        startButton.setPrefWidth(Double.MAX_VALUE);
        startButton.setStyle(
            "-fx-padding: 12px;" +
            "-fx-font-size: 14px;" +
            "-fx-background-color: " + MindDocTheme.PRIMARY + ";" +
            "-fx-text-fill: white;" +
            "-fx-cursor: hand;"
        );
        startButton.setOnAction(e -> {
            logger.info("Starting course: {}", course.getTitle());
            showAlert("Course started! Follow the instructions above.", Alert.AlertType.INFORMATION);
        });
        content.getChildren().add(startButton);
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-padding: 0;");
        
        courseDetailsPanel.getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS);
    }
    
    @Override
    public void refresh() {
        try {
            List<CBTCourse> courses = courseRepository.findAll();
            courseListView.getItems().clear();
            for (CBTCourse course : courses) {
                courseListView.getItems().add(course);
            }
        } catch (SQLException e) {
            logger.error("Error refreshing courses", e);
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
    
    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Courses");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
