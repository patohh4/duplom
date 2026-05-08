package com.mindoc.ui.symptomtracker;

import com.mindoc.database.DatabaseManager;
import com.mindoc.model.Symptom;
import com.mindoc.model.SymptomLog;
import com.mindoc.repository.SymptomRepository;
import com.mindoc.repository.SymptomLogRepository;
import com.mindoc.ui.common.BasePanel;
import com.mindoc.ui.theme.MindDocTheme;
import com.mindoc.util.DateUtils;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Panel for tracking and logging symptoms
 */
public class SymptomTrackerPanel extends BasePanel {
    private static final Logger logger = LoggerFactory.getLogger(SymptomTrackerPanel.class);
    
    private int currentUserId;
    private DatabaseManager databaseManager;
    private SymptomRepository symptomRepository;
    private SymptomLogRepository symptomLogRepository;
    
    private ListView<String> historyListView;
    
    public SymptomTrackerPanel(int userId, DatabaseManager databaseManager) {
        this.currentUserId = userId;
        this.databaseManager = databaseManager;
        this.symptomRepository = new SymptomRepository(databaseManager.getConnection());
        this.symptomLogRepository = new SymptomLogRepository(databaseManager.getConnection());
        
        initializeUI();
        refresh();
    }
    
    private void initializeUI() {
        setStyle("-fx-background-color: " + MindDocTheme.BACKGROUND + ";");
        setPadding(new Insets(20));
        setSpacing(15);
        
        // Title
        Label titleLabel = new Label("🩺 Symptom Tracker");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setTextFill(javafx.scene.paint.Color.web(MindDocTheme.PRIMARY));
        getChildren().add(titleLabel);
        
        // Main container with two columns
        HBox mainContainer = new HBox(20);
        mainContainer.setPadding(new Insets(0));
        
        // Left side - Log new symptom
        VBox logSection = createLogSection();
        mainContainer.getChildren().add(logSection);
        HBox.setHgrow(logSection, javafx.scene.layout.Priority.ALWAYS);
        
        // Right side - History
        VBox historySection = createHistorySection();
        mainContainer.getChildren().add(historySection);
        HBox.setHgrow(historySection, javafx.scene.layout.Priority.ALWAYS);
        
        getChildren().add(mainContainer);
    }
    
    private VBox createLogSection() {
        VBox section = new VBox(15);
        section.setStyle(
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-radius: 8;" +
            "-fx-padding: 20;" +
            "-fx-background-color: white;"
        );
        
        Label sectionTitle = new Label("Log New Symptom");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        section.getChildren().add(sectionTitle);
        
        // Symptom selection
        Label symptomLabel = new Label("Select Symptom:");
        ComboBox<String> symptomCombo = new ComboBox<>();
        symptomCombo.setStyle(getComboBoxStyle());
        symptomCombo.setPrefWidth(Double.MAX_VALUE);
        
        loadSymptoms(symptomCombo);
        
        // Severity slider
        Label severityLabel = new Label("Severity Level: ");
        Slider severitySlider = new Slider(1, 10, 5);
        severitySlider.setShowTickLabels(true);
        severitySlider.setShowTickMarks(true);
        severitySlider.setMajorTickUnit(1);
        severitySlider.setStyle("-fx-control-inner-background: " + MindDocTheme.PRIMARY + ";");
        
        Label severityValueLabel = new Label("5");
        severityValueLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " + MindDocTheme.PRIMARY + ";");
        severitySlider.valueProperty().addListener((obs, oldVal, newVal) -> 
            severityValueLabel.setText(String.valueOf(newVal.intValue()))
        );
        
        HBox severityBox = new HBox(10);
        severityBox.getChildren().addAll(severityLabel, severitySlider, severityValueLabel);
        
        // Notes
        Label notesLabel = new Label("Notes (optional):");
        TextArea notesArea = new TextArea();
        notesArea.setWrapText(true);
        notesArea.setPrefRowCount(4);
        notesArea.setStyle(getTextAreaStyle());
        
        // Save button
        Button saveButton = new Button("Log Symptom");
        saveButton.setPrefWidth(Double.MAX_VALUE);
        saveButton.setStyle(
            "-fx-padding: 12px;" +
            "-fx-font-size: 14px;" +
            "-fx-background-color: " + MindDocTheme.SUCCESS + ";" +
            "-fx-text-fill: white;" +
            "-fx-cursor: hand;"
        );
        
        saveButton.setOnAction(e -> {
            if (symptomCombo.getValue() == null) {
                showAlert("Please select a symptom", Alert.AlertType.WARNING);
                return;
            }
            
            try {
                Symptom symptom = symptomRepository.findByName(symptomCombo.getValue());
                if (symptom != null) {
                    SymptomLog log = new SymptomLog(
                        0,
                        currentUserId,
                        symptom.getId(),
                        (int) severitySlider.getValue(),
                        LocalDate.now().toString(),
                        notesArea.getText().trim().isEmpty() ? null : notesArea.getText()
                    );
                    
                    symptomLogRepository.create(log);
                    logger.info("Symptom logged: {} with severity {}", symptom.getName(), (int) severitySlider.getValue());
                    
                    // Reset form
                    symptomCombo.setValue(null);
                    severitySlider.setValue(5);
                    notesArea.clear();
                    
                    refresh();
                    showAlert("Symptom logged successfully!", Alert.AlertType.INFORMATION);
                }
            } catch (SQLException ex) {
                logger.error("Error logging symptom", ex);
                showAlert("Error: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        });
        
        section.getChildren().addAll(
            symptomLabel, symptomCombo,
            severityBox,
            notesLabel, notesArea,
            saveButton
        );
        
        return section;
    }
    
    private VBox createHistorySection() {
        VBox section = new VBox(10);
        section.setStyle(
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-radius: 8;" +
            "-fx-padding: 20;" +
            "-fx-background-color: white;"
        );
        
        Label sectionTitle = new Label("Recent Symptoms");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        section.getChildren().add(sectionTitle);
        
        // Filter by time period
        HBox filterBox = new HBox(10);
        Label filterLabel = new Label("Show last:");
        ComboBox<String> filterCombo = new ComboBox<>();
        filterCombo.getItems().addAll("3 days", "1 week", "2 weeks", "1 month", "All");
        filterCombo.setValue("1 week");
        filterCombo.setStyle(getComboBoxStyle());
        filterCombo.setPrefWidth(150);
        
        filterCombo.setOnAction(e -> refresh());
        
        filterBox.getChildren().addAll(filterLabel, filterCombo);
        section.getChildren().add(filterBox);
        
        // History list
        historyListView = new ListView<>();
        historyListView.setPrefHeight(300);
        historyListView.setStyle("-fx-control-inner-background: #f8fafc;");
        
        section.getChildren().add(historyListView);
        VBox.setVgrow(historyListView, javafx.scene.layout.Priority.ALWAYS);
        
        return section;
    }
    
    private void loadSymptoms(ComboBox<String> combo) {
        try {
            List<Symptom> symptoms = symptomRepository.findAll();
            for (Symptom symptom : symptoms) {
                combo.getItems().add(symptom.getIcon() + " " + symptom.getName());
            }
        } catch (SQLException e) {
            logger.error("Error loading symptoms", e);
        }
    }
    
    @Override
    public void refresh() {
        try {
            List<SymptomLog> logs = symptomLogRepository.findByUserId(currentUserId);
            
            historyListView.getItems().clear();
            
            for (SymptomLog log : logs) {
                try {
                    Symptom symptom = symptomRepository.findById(log.getSymptomId());
                    if (symptom != null) {
                        String entry = String.format(
                            "📌 %s | Severity: %d/10 | %s",
                            symptom.getIcon() + " " + symptom.getName(),
                            log.getSeverity(),
                            log.getDate()
                        );
                        historyListView.getItems().add(entry);
                    }
                } catch (SQLException e) {
                    logger.error("Error loading symptom details", e);
                }
            }
        } catch (SQLException e) {
            logger.error("Error refreshing symptom history", e);
        }
    }
    
    private String getComboBoxStyle() {
        return "-fx-padding: 10px;" +
               "-fx-font-size: 13px;" +
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
        alert.setTitle("Symptom Tracker");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
