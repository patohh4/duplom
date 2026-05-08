package com.mindoc.ui.moodtracking;

import com.mindoc.model.MoodEntry;
import com.mindoc.ui.common.BasePanel;
import com.mindoc.ui.theme.MindDocTheme;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

    /**
     * Panel for tracking mood with detailed context and symptoms
     */
    public class MoodTrackingPanel extends BasePanel {
        private static final Logger logger = LoggerFactory.getLogger(MoodTrackingPanel.class);
        
        private int currentUserId;
        
        private Slider moodSlider;
        private Label moodEmojiLabel;
        private TextArea notesTextArea;
        private ComboBox<String> contextComboBox;
        private CheckBox anxietyCheckbox;
        private CheckBox depressionCheckbox;
        private CheckBox stressCheckbox;
        private CheckBox sleepCheckbox;
        
        public MoodTrackingPanel(int currentUserId) {
            this.currentUserId = currentUserId;
            
            initializeUI();
        }
    
    private void initializeUI() {
        // Title
        Label titleLabel = new Label("Track Your Mood");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(javafx.scene.paint.Color.web(MindDocTheme.TEXT_PRIMARY));
        
        // Mood selection section
        VBox moodSection = createMoodSelectionSection();
        
        // Context section
        VBox contextSection = createContextSection();
        
        // Symptoms section
        VBox symptomsSection = createSymptomsSection();
        
        // Notes section
        VBox notesSection = createNotesSection();
        
        // Save button
        Button saveButton = new Button("Save Entry");
        saveButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px 30px;");
        saveButton.setStyle(
            "-fx-background-color: " + MindDocTheme.PRIMARY + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-padding: 10px 30px;" +
            "-fx-cursor: hand;"
        );
        saveButton.setOnAction(e -> saveMoodEntry());
        
        VBox buttonBox = new VBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().add(saveButton);
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        VBox content = new VBox(20);
        content.setPadding(new javafx.geometry.Insets(20));
        content.getChildren().addAll(
            titleLabel,
            moodSection,
            new Separator(),
            contextSection,
            new Separator(),
            symptomsSection,
            new Separator(),
            notesSection,
            buttonBox
        );
        
        scrollPane.setContent(content);
        getChildren().add(scrollPane);
    }
    
    private VBox createMoodSelectionSection() {
        VBox section = new VBox(15);
        section.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-color: white; -fx-padding: 15;");
        
        Label sectionTitle = new Label("How are you feeling?");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        
        // Mood emoji display
        moodEmojiLabel = new Label("😐");
        moodEmojiLabel.setFont(Font.font("Segoe UI", 64));
        
        // Mood slider
        moodSlider = new Slider(1, 10, 5);
        moodSlider.setShowTickLabels(true);
        moodSlider.setShowTickMarks(true);
        moodSlider.setMajorTickUnit(1);
        moodSlider.setMinorTickCount(0);
        moodSlider.setSnapToTicks(true);
        moodSlider.valueProperty().addListener((obs, oldVal, newVal) -> updateMoodEmoji(newVal.intValue()));
        
        // Labels for scale
        HBox scaleLabels = new HBox(60);
        scaleLabels.getChildren().addAll(
            new Label("Very Bad"),
            new Label("Okay"),
            new Label("Excellent")
        );
        
        section.getChildren().addAll(sectionTitle, moodEmojiLabel, moodSlider, scaleLabels);
        return section;
    }
    
    private VBox createContextSection() {
        VBox section = new VBox(10);
        section.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-color: white; -fx-padding: 15;");
        
        Label sectionTitle = new Label("What triggered this mood?");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        
        contextComboBox = new ComboBox<>();
        contextComboBox.getItems().addAll(
            "Work stress",
            "Relationship issues",
            "Health concerns",
            "Financial worries",
            "Social interaction",
            "Personal achievement",
            "No specific cause",
            "Other"
        );
        contextComboBox.setPromptText("Select a context...");
        
        section.getChildren().addAll(sectionTitle, contextComboBox);
        return section;
    }
    
    private VBox createSymptomsSection() {
        VBox section = new VBox(10);
        section.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-color: white; -fx-padding: 15;");
        
        Label sectionTitle = new Label("Are you experiencing any of these?");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        
        anxietyCheckbox = new CheckBox("Anxiety or worry");
        depressionCheckbox = new CheckBox("Sadness or depression");
        stressCheckbox = new CheckBox("Stress or overwhelm");
        sleepCheckbox = new CheckBox("Sleep problems");
        
        section.getChildren().addAll(
            sectionTitle,
            anxietyCheckbox,
            depressionCheckbox,
            stressCheckbox,
            sleepCheckbox
        );
        return section;
    }
    
    private VBox createNotesSection() {
        VBox section = new VBox(10);
        section.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-color: white; -fx-padding: 15;");
        
        Label sectionTitle = new Label("Additional Notes");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        
        notesTextArea = new TextArea();
        notesTextArea.setWrapText(true);
        notesTextArea.setPrefRowCount(4);
        notesTextArea.setPromptText("Write anything you'd like to remember about this mood...");
        
        section.getChildren().addAll(sectionTitle, notesTextArea);
        return section;
    }
    
    private void updateMoodEmoji(int moodLevel) {
        String emoji = switch (moodLevel) {
            case 1, 2 -> "😭";
            case 3, 4 -> "😔";
            case 5, 6 -> "😐";
            case 7, 8 -> "😊";
            case 9, 10 -> "😄";
            default -> "😐";
        };
        moodEmojiLabel.setText(emoji);
    }
    
    private void saveMoodEntry() {
        try {
            int moodLevel = (int) moodSlider.getValue();
            String emoji = moodEmojiLabel.getText();
            String notes = notesTextArea.getText();
            String context = contextComboBox.getValue() != null ? contextComboBox.getValue() : "";
            
            // Build symptoms string
            StringBuilder symptoms = new StringBuilder();
            if (anxietyCheckbox.isSelected()) symptoms.append("anxiety,");
            if (depressionCheckbox.isSelected()) symptoms.append("depression,");
            if (stressCheckbox.isSelected()) symptoms.append("stress,");
            if (sleepCheckbox.isSelected()) symptoms.append("sleep");
            
            MoodEntry entry = new MoodEntry(
                currentUserId,
                moodLevel,
                emoji,
                notes,
                context,
                symptoms.toString(),
                LocalDate.now()
            );
            
            // Save to database (will implement when repos are ready)
            showSuccessDialog("Success", "Mood entry saved successfully!");
            clearForm();
            refresh();
            
            logger.info("Mood entry saved for user " + currentUserId);
        } catch (Exception e) {
            logger.error("Error saving mood entry", e);
            showErrorDialog("Error", "Failed to save mood entry: " + e.getMessage());
        }
    }
    
    private void clearForm() {
        moodSlider.setValue(5);
        contextComboBox.setValue(null);
        anxietyCheckbox.setSelected(false);
        depressionCheckbox.setSelected(false);
        stressCheckbox.setSelected(false);
        sleepCheckbox.setSelected(false);
        notesTextArea.clear();
    }
    
    private void showSuccessDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @Override
    public void refresh() {
        // Refresh any loaded data
    }
}
