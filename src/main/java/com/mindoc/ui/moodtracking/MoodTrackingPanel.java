package com.mindoc.ui.moodtracking;

import com.mindoc.database.DatabaseManager;
import com.mindoc.model.MoodEntry;
import com.mindoc.repository.MoodEntryRepository;
import com.mindoc.ui.common.BasePanel;
import com.mindoc.ui.theme.MindDocTheme;
import com.mindoc.util.I18n;
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
        private final MoodEntryRepository moodEntryRepository;
        private final Runnable onMoodSaved;
        
        private Slider moodSlider;
        private Label moodEmojiLabel;
        private Label titleLabel;
        private Label moodSectionTitle;
        private Label veryBadLabel;
        private Label okayLabel;
        private Label excellentLabel;
        private Label contextSectionTitle;
        private Label symptomsSectionTitle;
        private Label notesSectionTitle;
        private Button saveButton;
        private TextArea notesTextArea;
        private ComboBox<String> contextComboBox;
        private CheckBox anxietyCheckbox;
        private CheckBox depressionCheckbox;
        private CheckBox stressCheckbox;
        private CheckBox sleepCheckbox;
        
        public MoodTrackingPanel(int currentUserId, DatabaseManager databaseManager, Runnable onMoodSaved) {
            this.currentUserId = currentUserId;
            this.moodEntryRepository = new MoodEntryRepository(databaseManager.getConnection());
            this.onMoodSaved = onMoodSaved;
            
            initializeUI();
        }
    
    private void initializeUI() {
        setStyle("-fx-background-color: " + MindDocTheme.BACKGROUND + ";");
        setPadding(new javafx.geometry.Insets(0));

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: " + MindDocTheme.BACKGROUND + "; -fx-background: " + MindDocTheme.BACKGROUND + ";");

        VBox content = new VBox(20);
        content.setStyle("-fx-background-color: " + MindDocTheme.BACKGROUND + ";");
        content.setPadding(new javafx.geometry.Insets(28));

        // Header
        HBox header = createHeaderSection();

        // Title
        titleLabel = new Label("Track Your Mood");
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
        saveButton = new Button("Save Entry");
        saveButton.setPrefWidth(Double.MAX_VALUE);
        saveButton.setStyle(
            "-fx-background-color: " + MindDocTheme.PRIMARY + "; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 14 0; " +
            "-fx-background-radius: 10; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(three-pass-box, #00000026, 6, 0, 0, 3);"
        );
        saveButton.setOnAction(e -> saveMoodEntry());

        content.getChildren().addAll(
            header,
            moodSection,
            contextSection,
            symptomsSection,
            notesSection,
            saveButton
        );

        scrollPane.setContent(content);
        getChildren().add(scrollPane);
    }

    private HBox createHeaderSection() {
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
        javafx.scene.layout.HBox.setHgrow(textBox, javafx.scene.layout.Priority.ALWAYS);

        Label title = new Label("🎯 Track Your Mood");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(javafx.scene.paint.Color.WHITE);

        Label subtitle = new Label("Record how you feel today and add context");
        subtitle.setFont(Font.font("Segoe UI", 13));
        subtitle.setTextFill(javafx.scene.paint.Color.web("#d1fae5"));

        textBox.getChildren().addAll(title, subtitle);

        Label deco = new Label("😊");
        deco.setFont(Font.font("System", 48));
        deco.setOpacity(0.7);

        section.getChildren().addAll(textBox, deco);
        return section;
    }

    private VBox createMoodSelectionSection() {
        VBox section = new VBox(16);
        section.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 14; " +
            "-fx-border-radius: 14; " +
            "-fx-padding: 22 24; " +
            "-fx-effect: dropshadow(three-pass-box, #00000014, 8, 0, 0, 2);"
        );

        moodSectionTitle = new Label("How are you feeling?");
        moodSectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        moodSectionTitle.setTextFill(javafx.scene.paint.Color.web(MindDocTheme.TEXT_PRIMARY));

        // Mood emoji display centered
        moodEmojiLabel = new Label("😐");
        moodEmojiLabel.setFont(Font.font("System", 72));
        moodEmojiLabel.setAlignment(Pos.CENTER);

        // Mood slider
        moodSlider = new Slider(1, 10, 5);
        moodSlider.setShowTickLabels(true);
        moodSlider.setShowTickMarks(true);
        moodSlider.setMajorTickUnit(1);
        moodSlider.setMinorTickCount(0);
        moodSlider.setSnapToTicks(true);
        moodSlider.setStyle("-fx-accent: " + MindDocTheme.PRIMARY + ";");
        moodSlider.valueProperty().addListener((obs, oldVal, newVal) -> updateMoodEmoji(newVal.intValue()));

        // Labels for scale
        HBox scaleLabels = new HBox();
        veryBadLabel = new Label("😞 Very Bad");
        okayLabel = new Label("😐 Okay");
        excellentLabel = new Label("😄 Excellent");
        for (Label l : new Label[]{veryBadLabel, okayLabel, excellentLabel}) {
            l.setFont(Font.font("Segoe UI", 11));
            l.setTextFill(javafx.scene.paint.Color.web(MindDocTheme.TEXT_SECONDARY));
        }
        javafx.scene.layout.HBox.setHgrow(okayLabel, javafx.scene.layout.Priority.ALWAYS);
        okayLabel.setAlignment(Pos.CENTER);
        scaleLabels.getChildren().addAll(veryBadLabel, okayLabel, excellentLabel);

        VBox emojiBox = new VBox();
        emojiBox.setAlignment(Pos.CENTER);
        emojiBox.getChildren().add(moodEmojiLabel);

        section.getChildren().addAll(moodSectionTitle, emojiBox, moodSlider, scaleLabels);
        return section;
    }

    private VBox createContextSection() {
        VBox section = new VBox(12);
        section.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 14; " +
            "-fx-border-radius: 14; " +
            "-fx-padding: 22 24; " +
            "-fx-effect: dropshadow(three-pass-box, #00000014, 8, 0, 0, 2);"
        );

        contextSectionTitle = new Label("🧩 What triggered this mood?");
        contextSectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        contextSectionTitle.setTextFill(javafx.scene.paint.Color.web(MindDocTheme.TEXT_PRIMARY));

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
        contextComboBox.setPrefWidth(Double.MAX_VALUE);

        section.getChildren().addAll(contextSectionTitle, contextComboBox);
        return section;
    }

    private VBox createSymptomsSection() {
        VBox section = new VBox(12);
        section.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 14; " +
            "-fx-border-radius: 14; " +
            "-fx-padding: 22 24; " +
            "-fx-effect: dropshadow(three-pass-box, #00000014, 8, 0, 0, 2);"
        );

        symptomsSectionTitle = new Label("⚡ Are you experiencing any of these?");
        symptomsSectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        symptomsSectionTitle.setTextFill(javafx.scene.paint.Color.web(MindDocTheme.TEXT_PRIMARY));

        anxietyCheckbox = new CheckBox("Anxiety or worry");
        depressionCheckbox = new CheckBox("Sadness or depression");
        stressCheckbox = new CheckBox("Stress or overwhelm");
        sleepCheckbox = new CheckBox("Sleep problems");

        HBox row1 = new HBox(20);
        HBox row2 = new HBox(20);
        row1.getChildren().addAll(anxietyCheckbox, depressionCheckbox);
        row2.getChildren().addAll(stressCheckbox, sleepCheckbox);

        section.getChildren().addAll(symptomsSectionTitle, row1, row2);
        return section;
    }

    private VBox createNotesSection() {
        VBox section = new VBox(12);
        section.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 14; " +
            "-fx-border-radius: 14; " +
            "-fx-padding: 22 24; " +
            "-fx-effect: dropshadow(three-pass-box, #00000014, 8, 0, 0, 2);"
        );

        notesSectionTitle = new Label("📝 Additional Notes");
        notesSectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        notesSectionTitle.setTextFill(javafx.scene.paint.Color.web(MindDocTheme.TEXT_PRIMARY));

        notesTextArea = new TextArea();
        notesTextArea.setWrapText(true);
        notesTextArea.setPrefRowCount(4);
        notesTextArea.setPromptText("Write anything you'd like to remember about this mood...");

        section.getChildren().addAll(notesSectionTitle, notesTextArea);
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
            String notes = notesTextArea.getText() == null ? "" : notesTextArea.getText().trim();
            String context = contextComboBox.getValue() != null ? contextComboBox.getValue() : "";

            boolean anySymptomSelected = anxietyCheckbox.isSelected()
                || depressionCheckbox.isSelected()
                || stressCheckbox.isSelected()
                || sleepCheckbox.isSelected();
            boolean hasNotes = !notes.isEmpty();
            boolean hasContext = contextComboBox.getValue() != null && !context.trim().isEmpty();
            boolean moodChanged = moodLevel != 5;

            // Prevent saving a default/empty entry (user didn't choose anything meaningful)
            if (!moodChanged && !hasContext && !hasNotes && !anySymptomSelected) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle(I18n.t("warning", "Warning"));
                alert.setHeaderText(null);
                alert.setContentText(I18n.t(
                    "empty_mood_entry",
                    "Please change the mood level or add context/notes/symptoms before saving."
                ));
                alert.showAndWait();
                return;
            }

            // Build symptoms string
            StringBuilder symptoms = new StringBuilder();
            if (anxietyCheckbox.isSelected()) symptoms.append("anxiety,");
            if (depressionCheckbox.isSelected()) symptoms.append("depression,");
            if (stressCheckbox.isSelected()) symptoms.append("stress,");
            if (sleepCheckbox.isSelected()) symptoms.append("sleep,");
            if (!symptoms.isEmpty()) {
                symptoms.deleteCharAt(symptoms.length() - 1);
            }
            
            MoodEntry entry = new MoodEntry(
                currentUserId,
                moodLevel,
                emoji,
                notes,
                context,
                symptoms.toString(),
                LocalDate.now()
            );
            
            MoodEntry existing = moodEntryRepository.findByUserIdAndDate(currentUserId, LocalDate.now());
            if (existing != null) {
                existing.setMoodLevel(moodLevel);
                existing.setMoodEmoji(emoji);
                existing.setNote(notes);
                existing.setContext(context);
                existing.setSymptoms(symptoms.toString());
                moodEntryRepository.update(existing);
            } else {
                moodEntryRepository.create(entry);
            }

            showSuccessDialog(I18n.t("success", "Success"), I18n.t("saved_success", "Mood entry saved successfully!"));
            clearForm();
            refresh();
            if (onMoodSaved != null) {
                onMoodSaved.run();
            }
            
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
        applyLanguage();
    }

    public void applyLanguage() {
        if (titleLabel != null) titleLabel.setText(I18n.t("track_mood", "Track Your Mood"));
        if (saveButton != null) saveButton.setText(I18n.t("save_entry", "Save Entry"));
        if (moodSectionTitle != null) moodSectionTitle.setText(I18n.t("how_feeling", "How are you feeling?"));
        if (veryBadLabel != null) veryBadLabel.setText(I18n.t("very_bad", "Very Bad"));
        if (okayLabel != null) okayLabel.setText(I18n.t("okay", "Okay"));
        if (excellentLabel != null) excellentLabel.setText(I18n.t("excellent", "Excellent"));
        if (contextSectionTitle != null) contextSectionTitle.setText(I18n.t("trigger_mood", "What triggered this mood?"));
        if (symptomsSectionTitle != null) symptomsSectionTitle.setText(I18n.t("symptoms_q", "Are you experiencing any of these?"));
        if (notesSectionTitle != null) notesSectionTitle.setText(I18n.t("additional_notes", "Additional Notes"));
    }
}
