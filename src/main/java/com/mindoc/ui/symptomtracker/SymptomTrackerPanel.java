package com.mindoc.ui.symptomtracker;

import com.mindoc.database.DatabaseManager;
import com.mindoc.model.Symptom;
import com.mindoc.model.SymptomLog;
import com.mindoc.repository.SymptomRepository;
import com.mindoc.repository.SymptomLogRepository;
import com.mindoc.ui.common.BasePanel;
import com.mindoc.ui.theme.MindDocTheme;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private ComboBox<Symptom> symptomCombo;
    private ComboBox<String> filterCombo;
    private Label titleLabel;
    private Label logSectionTitle;
    private Label symptomLabel;
    private Label severityLabel;
    private Label notesLabel;
    private Button saveButton;
    private Label historySectionTitle;
    private Label filterLabel;
    private String currentLanguage = "English";
    private Map<String, SymptomLog> symptomLogMap = new HashMap<>();  // Map to store logs with display text as key
    
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
        setPadding(new Insets(28));
        setSpacing(16);
        setFillWidth(true);

        // Header banner
        HBox header = createHeaderBanner();
        getChildren().add(header);

        // Title (hidden — header replaces it, kept for applyLanguage)
        titleLabel = new Label("🩺 Symptom Tracker");
        titleLabel.setVisible(false);
        titleLabel.setManaged(false);
        
        // Main container with two columns
        HBox mainContainer = new HBox(20);
        mainContainer.setPadding(new Insets(0));
        mainContainer.setFillHeight(true);
        mainContainer.setMaxWidth(Double.MAX_VALUE);
        
        // Left side - Log new symptom
        VBox logSection = createLogSection();
        mainContainer.getChildren().add(logSection);
        HBox.setHgrow(logSection, javafx.scene.layout.Priority.ALWAYS);
        
        // Right side - History
        VBox historySection = createHistorySection();
        mainContainer.getChildren().add(historySection);
        HBox.setHgrow(historySection, javafx.scene.layout.Priority.ALWAYS);
        
        getChildren().add(mainContainer);
        VBox.setVgrow(mainContainer, javafx.scene.layout.Priority.ALWAYS);
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

        Label titleLbl = new Label("🩺 Symptom Tracker");
        titleLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLbl.setTextFill(javafx.scene.paint.Color.WHITE);

        Label subtitleLbl = new Label("Log and monitor your symptoms over time");
        subtitleLbl.setFont(Font.font("Segoe UI", 13));
        subtitleLbl.setTextFill(javafx.scene.paint.Color.web("#d1fae5"));

        textBox.getChildren().addAll(titleLbl, subtitleLbl);

        Label deco = new Label("📋");
        deco.setFont(Font.font("System", 48));
        deco.setOpacity(0.7);

        section.getChildren().addAll(textBox, deco);
        return section;
    }

    private VBox createLogSection() {
        VBox section = new VBox(14);
        section.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 14; " +
            "-fx-border-radius: 14; " +
            "-fx-padding: 20; " +
            "-fx-effect: dropshadow(three-pass-box, #00000014, 8, 0, 0, 2);"
        );
        section.setMinWidth(360);
        section.setPrefWidth(460);
        section.setMaxWidth(Double.MAX_VALUE);
        
        logSectionTitle = new Label("Log New Symptom");
        logSectionTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        section.getChildren().add(logSectionTitle);
        
        // Symptom selection
        symptomLabel = new Label("Select Symptom:");
        symptomCombo = new ComboBox<>();
        symptomCombo.setStyle(getComboBoxStyle());
        symptomCombo.setPrefWidth(Double.MAX_VALUE);
        symptomCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Symptom symptom) {
                if (symptom == null) {
                    return "";
                }
                return translateSymptomName(symptom.getName()) + " (" + symptom.getIcon() + ")";
            }

            @Override
            public Symptom fromString(String string) {
                return null;
            }
        });
        
        loadSymptoms(symptomCombo);
        
        // Severity slider
        severityLabel = new Label("Severity Level: ");
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
        notesLabel = new Label("Notes (optional):");
        TextArea notesArea = new TextArea();
        notesArea.setWrapText(true);
        notesArea.setPrefRowCount(4);
        notesArea.setStyle(getTextAreaStyle());
        
        // Save button
        saveButton = new Button("Log Symptom");
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
                showAlert(isUkrainian() ? "Оберіть симптом" : "Please select a symptom", Alert.AlertType.WARNING);
                return;
            }
            
            try {
                Symptom symptom = symptomCombo.getValue();
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
                    showAlert(isUkrainian() ? "Симптом успішно збережено!" : "Symptom logged successfully!", Alert.AlertType.INFORMATION);
                }
            } catch (SQLException ex) {
                logger.error("Error logging symptom", ex);
                showAlert((isUkrainian() ? "Помилка: " : "Error: ") + ex.getMessage(), Alert.AlertType.ERROR);
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
        VBox section = new VBox(12);
        section.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 14; " +
            "-fx-border-radius: 14; " +
            "-fx-padding: 20; " +
            "-fx-effect: dropshadow(three-pass-box, #00000014, 8, 0, 0, 2);"
        );
        section.setMinWidth(380);
        section.setPrefWidth(620);
        section.setMaxWidth(Double.MAX_VALUE);
        
        historySectionTitle = new Label("Recent Symptoms");
        historySectionTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        section.getChildren().add(historySectionTitle);
        
        // Filter by time period
        HBox filterBox = new HBox(10);
        filterLabel = new Label("Show last:");
        filterCombo = new ComboBox<>();
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
        
        // Add click handler to show notes
        historyListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                String selectedEntry = historyListView.getSelectionModel().getSelectedItem();
                if (selectedEntry != null && symptomLogMap.containsKey(selectedEntry)) {
                    SymptomLog log = symptomLogMap.get(selectedEntry);
                    showSymptomDetailsDialog(log);
                }
            }
        });
        
        section.getChildren().add(historyListView);
        VBox.setVgrow(historyListView, javafx.scene.layout.Priority.ALWAYS);
        
        return section;
    }
    
    private void loadSymptoms(ComboBox<Symptom> combo) {
        try {
            List<Symptom> symptoms = symptomRepository.findAll();
            combo.getItems().clear();
            combo.getItems().addAll(symptoms);
        } catch (SQLException e) {
            logger.error("Error loading symptoms", e);
        }
    }
    
    @Override
    public void refresh() {
        try {
            List<SymptomLog> logs = symptomLogRepository.findByUserId(currentUserId);
            
            historyListView.getItems().clear();
            symptomLogMap.clear();

            LocalDate minDate = resolveMinDateFromFilter();
            
            for (SymptomLog log : logs) {
                try {
                    LocalDate logDate = LocalDate.parse(log.getDate());
                    if (minDate != null && logDate.isBefore(minDate)) {
                        continue;
                    }
                    Symptom symptom = symptomRepository.findById(log.getSymptomId());
                    if (symptom != null) {
                        String entry = String.format(
                            "📌 %s | %s: %d/10 | %s",
                            symptom.getIcon() + " " + translateSymptomName(symptom.getName()),
                            isUkrainian() ? "Рівень" : "Severity",
                            log.getSeverity(),
                            log.getDate()
                        );
                        historyListView.getItems().add(entry);
                        symptomLogMap.put(entry, log);
                    }
                } catch (SQLException e) {
                    logger.error("Error loading symptom details", e);
                } catch (Exception e) {
                    logger.warn("Skipping invalid symptom log date: {}", log.getDate());
                }
            }

            if (historyListView.getItems().isEmpty()) {
                historyListView.getItems().add(
                    isUkrainian()
                        ? "Ще немає записів симптомів за вибраний період."
                        : "No symptom logs for the selected period yet."
                );
            }
        } catch (SQLException e) {
            logger.error("Error refreshing symptom history", e);
        }
    }

    private LocalDate resolveMinDateFromFilter() {
        if (filterCombo == null || filterCombo.getValue() == null || "All".equals(filterCombo.getValue())) {
            return null;
        }

        LocalDate today = LocalDate.now();
        return switch (filterCombo.getValue()) {
            case "3 days", "3 дні" -> today.minusDays(3);
            case "1 week", "1 тиждень" -> today.minusWeeks(1);
            case "2 weeks", "2 тижні" -> today.minusWeeks(2);
            case "1 month", "1 місяць" -> today.minusMonths(1);
            default -> null;
        };
    }
    
    private String getComboBoxStyle() {
        return "-fx-padding: 8; " +
               "-fx-font-size: 13px; " +
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
        alert.setTitle(isUkrainian() ? "Трекер симптомів" : "Symptom Tracker");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void applyLanguage(String language) {
        this.currentLanguage = language == null ? "English" : language;
        Symptom selectedSymptom = symptomCombo != null ? symptomCombo.getValue() : null;
        if (titleLabel != null) titleLabel.setText(isUkrainian() ? "🩺 Трекер симптомів" : "🩺 Symptom Tracker");
        if (logSectionTitle != null) logSectionTitle.setText(isUkrainian() ? "Додати симптом" : "Log New Symptom");
        if (symptomLabel != null) symptomLabel.setText(isUkrainian() ? "Оберіть симптом:" : "Select Symptom:");
        if (severityLabel != null) severityLabel.setText(isUkrainian() ? "Рівень важкості: " : "Severity Level: ");
        if (notesLabel != null) notesLabel.setText(isUkrainian() ? "Нотатки (необов'язково):" : "Notes (optional):");
        if (saveButton != null) saveButton.setText(isUkrainian() ? "Зберегти симптом" : "Log Symptom");
        if (historySectionTitle != null) historySectionTitle.setText(isUkrainian() ? "Останні симптоми" : "Recent Symptoms");
        if (filterLabel != null) filterLabel.setText(isUkrainian() ? "Показати за:" : "Show last:");
        if (symptomCombo != null) symptomCombo.setPromptText(isUkrainian() ? "Оберіть симптом..." : "Select symptom...");
        if (filterCombo != null) {
            String selectedFilter = filterCombo.getValue();
            filterCombo.getItems().setAll(
                isUkrainian() ? "3 дні" : "3 days",
                isUkrainian() ? "1 тиждень" : "1 week",
                isUkrainian() ? "2 тижні" : "2 weeks",
                isUkrainian() ? "1 місяць" : "1 month",
                "All"
            );
            if (selectedFilter == null) {
                filterCombo.setValue(isUkrainian() ? "1 тиждень" : "1 week");
            } else {
                filterCombo.setValue(mapFilterValue(selectedFilter));
            }
        }
        if (symptomCombo != null) {
            loadSymptoms(symptomCombo);
            symptomCombo.setValue(selectedSymptom);
        }
        refresh();
    }

    private boolean isUkrainian() {
        return "Українська".equalsIgnoreCase(currentLanguage);
    }

    private String mapFilterValue(String value) {
        Map<String, String> map = new HashMap<>();
        map.put("3 days", isUkrainian() ? "3 дні" : "3 days");
        map.put("1 week", isUkrainian() ? "1 тиждень" : "1 week");
        map.put("2 weeks", isUkrainian() ? "2 тижні" : "2 weeks");
        map.put("1 month", isUkrainian() ? "1 місяць" : "1 month");
        map.put("3 дні", isUkrainian() ? "3 дні" : "3 days");
        map.put("1 тиждень", isUkrainian() ? "1 тиждень" : "1 week");
        map.put("2 тижні", isUkrainian() ? "2 тижні" : "2 weeks");
        map.put("1 місяць", isUkrainian() ? "1 місяць" : "1 month");
        map.put("All", "All");
        return map.getOrDefault(value, isUkrainian() ? "1 тиждень" : "1 week");
    }

    private void showSymptomDetailsDialog(SymptomLog log) {
        try {
            Symptom symptom = symptomRepository.findById(log.getSymptomId());
            if (symptom == null) return;

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle(isUkrainian() ? "Деталі симптому" : "Symptom Details");
            dialog.setHeaderText(null);

            VBox content = new VBox(15);
            content.setPadding(new Insets(20));

            Label titleLabel = new Label(symptom.getIcon() + " " + translateSymptomName(symptom.getName()));
            titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
            content.getChildren().add(titleLabel);

            HBox infoBox = new HBox(30);
            
            VBox dateBox = new VBox(5);
            Label dateLabel = new Label(isUkrainian() ? "Дата:" : "Date:");
            dateLabel.setStyle("-fx-font-weight: bold;");
            Label dateValue = new Label(log.getDate());
            dateBox.getChildren().addAll(dateLabel, dateValue);
            infoBox.getChildren().add(dateBox);

            VBox severityBox = new VBox(5);
            Label severityLabel = new Label(isUkrainian() ? "Рівень важкості:" : "Severity:");
            severityLabel.setStyle("-fx-font-weight: bold;");
            Label severityValue = new Label(log.getSeverity() + "/10");
            severityBox.getChildren().addAll(severityLabel, severityValue);
            infoBox.getChildren().add(severityBox);

            content.getChildren().add(infoBox);

            // Notes section
            if (log.getNotes() != null && !log.getNotes().trim().isEmpty()) {
                Separator separator = new Separator();
                content.getChildren().add(separator);

                Label notesTitle = new Label(isUkrainian() ? "Нотатки:" : "Notes:");
                notesTitle.setStyle("-fx-font-weight: bold;");
                content.getChildren().add(notesTitle);

                TextArea notesArea = new TextArea(log.getNotes());
                notesArea.setWrapText(true);
                notesArea.setEditable(false);
                notesArea.setPrefRowCount(6);
                notesArea.setStyle("-fx-control-inner-background: #f8fafc; -fx-padding: 10px; -fx-font-size: 12px;");
                content.getChildren().add(notesArea);
            } else {
                Label noNotesLabel = new Label(isUkrainian() ? "Немає нотаток" : "No notes");
                noNotesLabel.setStyle("-fx-text-fill: #718096; -fx-font-size: 12px;");
                content.getChildren().add(noNotesLabel);
            }

            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
            dialog.showAndWait();
        } catch (SQLException e) {
            logger.error("Error loading symptom details", e);
            showAlert((isUkrainian() ? "Помилка: " : "Error: ") + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private String translateSymptomName(String name) {
        if (!isUkrainian() || name == null) {
            return name;
        }
        return switch (name) {
            case "Persistent sadness" -> "Постійний смуток";
            case "Loss of interest" -> "Втрата інтересу";
            case "Fatigue" -> "Втома";
            case "Sleep problems" -> "Проблеми зі сном";
            case "Excessive worry" -> "Надмірне хвилювання";
            case "Racing thoughts" -> "Нав'язливі думки";
            case "Tension" -> "Напруження";
            case "Panic" -> "Паніка";
            case "Overwhelmed" -> "Перевантаження";
            case "Irritability" -> "Дратівливість";
            case "Concentration issues" -> "Проблеми з концентрацією";
            case "Physical tension" -> "Фізичне напруження";
            default -> name;
        };
    }
}
