package com.mindoc.ui.symptomtracker;

import com.mindoc.database.DatabaseManager;
import com.mindoc.model.Symptom;
import com.mindoc.model.SymptomLog;
import com.mindoc.repository.SymptomRepository;
import com.mindoc.repository.SymptomLogRepository;
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
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymptomTrackerPanel extends BasePanel {
    private static final Logger logger = LoggerFactory.getLogger(SymptomTrackerPanel.class);

    private int currentUserId;
    private DatabaseManager databaseManager;
    private SymptomRepository symptomRepository;
    private SymptomLogRepository symptomLogRepository;

    // form
    private ComboBox<Symptom> symptomCombo;
    private Slider severitySlider;
    private Label severityValueLabel;
    private TextArea notesArea;
    private Button saveButton;

    // history
    private VBox historyItemsBox;
    private ComboBox<String> filterCombo;
    private final List<SymptomLog> cachedLogs = new ArrayList<>();

    // i18n labels
    private Label titleLabel;
    private Label logSectionTitle;
    private Label symptomLabel;
    private Label severityLabel;
    private Label notesLabel;
    private Label historySectionTitle;
    private Label filterLabel;

    private String currentLanguage = "English";
    // kept for details dialog click
    private final Map<Integer, Symptom> symptomCache = new HashMap<>();

    public SymptomTrackerPanel(int userId, DatabaseManager databaseManager) {
        this.currentUserId = userId;
        this.databaseManager = databaseManager;
        this.symptomRepository = new SymptomRepository(databaseManager.getConnection());
        this.symptomLogRepository = new SymptomLogRepository(databaseManager.getConnection());
        initializeUI();
        refresh();
    }

    // ── Layout ───────────────────────────────────────────────────────────────

    private void initializeUI() {
        setStyle("-fx-background-color: " + MindDocTheme.BACKGROUND + ";");
        setPadding(new Insets(0));
        setSpacing(0);
        setFillWidth(true);

        // Header flush to top
        getChildren().add(buildHeaderBanner());

        // hidden label for applyLanguage
        titleLabel = new Label("🩺 Symptom Tracker");
        titleLabel.setVisible(false);
        titleLabel.setManaged(false);
        getChildren().add(titleLabel);

        // Two-column body
        HBox body = new HBox(20);
        body.setPadding(new Insets(24));
        body.setFillHeight(true);
        VBox.setVgrow(body, Priority.ALWAYS);

        VBox log  = buildLogSection();
        VBox hist = buildHistorySection();
        HBox.setHgrow(log,  Priority.ALWAYS);
        HBox.setHgrow(hist, Priority.ALWAYS);
        body.getChildren().addAll(log, hist);
        getChildren().add(body);
    }

    // ── Header ───────────────────────────────────────────────────────────────

    private HBox buildHeaderBanner() {
        HBox banner = new HBox();
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.setPadding(new Insets(24, 36, 24, 36));
        banner.setStyle(
            "-fx-background-color: linear-gradient(from 0% 0% to 100% 0%, " +
                MindDocTheme.PRIMARY + ", " + MindDocTheme.SECONDARY + ");"
        );

        VBox text = new VBox(4);
        HBox.setHgrow(text, Priority.ALWAYS);

        Label h1 = new Label("🩺 Symptom Tracker");
        h1.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        h1.setTextFill(Color.WHITE);

        Label h2 = new Label("Log and monitor your symptoms over time");
        h2.setFont(Font.font("Segoe UI", 13));
        h2.setTextFill(Color.web("#d1fae5"));

        text.getChildren().addAll(h1, h2);

        Label deco = new Label("🌡");
        deco.setFont(Font.font("System", 56));
        deco.setOpacity(0.18);

        banner.getChildren().addAll(text, deco);
        return banner;
    }

    // ── Log Section ──────────────────────────────────────────────────────────

    private VBox buildLogSection() {
        // accent bar
        Region bar = accentBar(MindDocTheme.PRIMARY);

        VBox inner = new VBox(16);
        inner.setPadding(new Insets(20, 22, 24, 22));

        logSectionTitle = new Label("✏️  Log New Symptom");
        logSectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        logSectionTitle.setTextFill(Color.web(MindDocTheme.TEXT_PRIMARY));

        // ── Symptom picker ──────────────────────────────────────────────────
        symptomLabel = new Label("Select Symptom:");
        symptomLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        symptomLabel.setTextFill(Color.web(MindDocTheme.TEXT_SECONDARY));

        symptomCombo = new ComboBox<>();
        symptomCombo.setPrefWidth(Double.MAX_VALUE);
        symptomCombo.setPromptText("Choose a symptom…");
        symptomCombo.setConverter(new StringConverter<>() {
            @Override public String toString(Symptom s) {
                return s == null ? "" : s.getIcon() + "  " + translateSymptomName(s.getName());
            }
            @Override public Symptom fromString(String str) { return null; }
        });
        loadSymptoms(symptomCombo);

        // ── Severity ────────────────────────────────────────────────────────
        severityLabel = new Label("Severity Level:");
        severityLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        severityLabel.setTextFill(Color.web(MindDocTheme.TEXT_SECONDARY));

        severitySlider = new Slider(1, 10, 5);
        severitySlider.setShowTickLabels(true);
        severitySlider.setShowTickMarks(true);
        severitySlider.setMajorTickUnit(1);
        severitySlider.setMinorTickCount(0);
        severitySlider.setSnapToTicks(true);
        HBox.setHgrow(severitySlider, Priority.ALWAYS);

        severityValueLabel = new Label();
        severityValueLabel.setMinWidth(38);
        severityValueLabel.setAlignment(Pos.CENTER);
        severityValueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        updateSeverityBadge(5);

        severitySlider.valueProperty().addListener((obs, ov, nv) -> updateSeverityBadge(nv.intValue()));

        HBox severityRow = new HBox(10);
        severityRow.setAlignment(Pos.CENTER_LEFT);
        severityRow.getChildren().addAll(severitySlider, severityValueLabel);

        VBox severityBox = new VBox(6);
        severityBox.getChildren().addAll(severityLabel, severityRow);

        // ── Notes ────────────────────────────────────────────────────────────
        notesLabel = new Label("Notes (optional):");
        notesLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        notesLabel.setTextFill(Color.web(MindDocTheme.TEXT_SECONDARY));

        notesArea = new TextArea();
        notesArea.setWrapText(true);
        notesArea.setPrefRowCount(3);
        notesArea.setPromptText("Describe how you feel, triggers, context…");

        // ── Save button ──────────────────────────────────────────────────────
        saveButton = new Button("Log Symptom");
        saveButton.setPrefWidth(Double.MAX_VALUE);
        saveButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        saveButton.setStyle(
            "-fx-background-color: " + MindDocTheme.PRIMARY + "; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 10; " +
            "-fx-padding: 13 0; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(three-pass-box, #00000026, 6, 0, 0, 3);"
        );
        saveButton.setOnAction(e -> handleSave());

        inner.getChildren().addAll(
            logSectionTitle,
            symptomLabel, symptomCombo,
            severityBox,
            notesLabel, notesArea,
            saveButton
        );

        VBox card = new VBox(0);
        card.setMinWidth(320);
        card.setPrefWidth(420);
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 16; " +
            "-fx-border-radius: 16; " +
            "-fx-effect: dropshadow(three-pass-box, #00000014, 10, 0, 0, 3);"
        );
        card.getChildren().addAll(bar, inner);
        return card;
    }

    /** Update the severity badge color + text */
    private void updateSeverityBadge(int v) {
        String color = severityColor(v);
        severityValueLabel.setText(String.valueOf(v));
        severityValueLabel.setStyle(
            "-fx-background-color: " + color + "22; " +
            "-fx-text-fill: " + color + "; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 4 8;"
        );
    }

    // ── History Section ──────────────────────────────────────────────────────

    private VBox buildHistorySection() {
        Region bar = accentBar(MindDocTheme.INFO);

        VBox inner = new VBox(14);
        inner.setPadding(new Insets(20, 22, 24, 22));
        VBox.setVgrow(inner, Priority.ALWAYS);

        // header row
        HBox headerRow = new HBox(10);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        historySectionTitle = new Label("📋  Recent Symptoms");
        historySectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        historySectionTitle.setTextFill(Color.web(MindDocTheme.TEXT_PRIMARY));
        HBox.setHgrow(historySectionTitle, Priority.ALWAYS);

        filterLabel = new Label("Show last:");
        filterLabel.setFont(Font.font("Segoe UI", 12));
        filterLabel.setTextFill(Color.web(MindDocTheme.TEXT_SECONDARY));

        filterCombo = new ComboBox<>();
        filterCombo.getItems().addAll("3 days", "1 week", "2 weeks", "1 month", "All");
        filterCombo.setValue("1 week");
        filterCombo.setPrefWidth(120);
        filterCombo.setOnAction(e -> refresh());

        headerRow.getChildren().addAll(historySectionTitle, filterLabel, filterCombo);

        // scrollable items container
        historyItemsBox = new VBox(10);
        historyItemsBox.setStyle("-fx-background-color: transparent;");

        ScrollPane sp = new ScrollPane(historyItemsBox);
        sp.setFitToWidth(true);
        sp.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-background: transparent; " +
            "-fx-padding: 0;"
        );
        VBox.setVgrow(sp, Priority.ALWAYS);

        inner.getChildren().addAll(headerRow, sp);

        VBox card = new VBox(0);
        card.setMinWidth(360);
        card.setPrefWidth(560);
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 16; " +
            "-fx-border-radius: 16; " +
            "-fx-effect: dropshadow(three-pass-box, #00000014, 10, 0, 0, 3);"
        );
        VBox.setVgrow(card, Priority.ALWAYS);
        card.getChildren().addAll(bar, inner);
        return card;
    }

    /** Build one symptom history row-card */
    private HBox buildHistoryCard(Symptom symptom, SymptomLog log) {
        int sev  = log.getSeverity();
        String c = severityColor(sev);

        // Severity badge (circle with number)
        StackPane badge = new StackPane();
        badge.setStyle(
            "-fx-background-color: " + c + "; " +
            "-fx-background-radius: 24; " +
            "-fx-min-width: 48; -fx-min-height: 48; " +
            "-fx-max-width: 48; -fx-max-height: 48;"
        );
        VBox badgeText = new VBox(0);
        badgeText.setAlignment(Pos.CENTER);
        Label sevNum = new Label(String.valueOf(sev));
        sevNum.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        sevNum.setTextFill(Color.WHITE);
        Label sevOf = new Label("/10");
        sevOf.setFont(Font.font("Segoe UI", 9));
        sevOf.setTextFill(Color.web("#ffffff99"));
        badgeText.getChildren().addAll(sevNum, sevOf);
        badge.getChildren().add(badgeText);

        // Content
        VBox content = new VBox(3);
        HBox.setHgrow(content, Priority.ALWAYS);

        // Name row
        HBox nameRow = new HBox(8);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        Label icon = new Label(symptom.getIcon());
        icon.setFont(Font.font("System", 18));
        Label name = new Label(translateSymptomName(symptom.getName()));
        name.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        name.setTextFill(Color.web(MindDocTheme.TEXT_PRIMARY));
        nameRow.getChildren().addAll(icon, name);

        // Date + notes indicator
        HBox metaRow = new HBox(10);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        Label date = new Label("📅 " + log.getDate());
        date.setFont(Font.font("Segoe UI", 11));
        date.setTextFill(Color.web(MindDocTheme.TEXT_MUTED));
        metaRow.getChildren().add(date);

        if (log.getNotes() != null && !log.getNotes().isBlank()) {
            Label notesChip = new Label("💬 notes");
            notesChip.setFont(Font.font("Segoe UI", 10));
            notesChip.setStyle(
                "-fx-background-color: " + MindDocTheme.INFO + "22; " +
                "-fx-text-fill: " + MindDocTheme.INFO + "; " +
                "-fx-padding: 2 7; " +
                "-fx-background-radius: 10;"
            );
            metaRow.getChildren().add(notesChip);
        }

        content.getChildren().addAll(nameRow, metaRow);

        // Severity level bar (thin, at bottom)
        Region levelBar = new Region();
        levelBar.setPrefHeight(3);
        double pct = sev / 10.0;
        levelBar.setPrefWidth(pct * 200);
        levelBar.setMaxWidth(Double.MAX_VALUE);
        levelBar.setStyle("-fx-background-color: " + c + "; -fx-background-radius: 2;");

        VBox contentWrapper = new VBox(6);
        HBox.setHgrow(contentWrapper, Priority.ALWAYS);
        contentWrapper.getChildren().addAll(content, levelBar);

        // Arrow hint
        Label arrow = new Label("›");
        arrow.setFont(Font.font("Segoe UI", 20));
        arrow.setTextFill(Color.web(MindDocTheme.TEXT_MUTED));

        HBox card = new HBox(14);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(12, 14, 12, 14));
        card.setStyle(
            "-fx-background-color: " + MindDocTheme.BACKGROUND + "; " +
            "-fx-background-radius: 12; " +
            "-fx-cursor: hand;"
        );
        card.getChildren().addAll(badge, contentWrapper, arrow);

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(
            "-fx-background-color: " + c + "11; " +
            "-fx-background-radius: 12; " +
            "-fx-cursor: hand;"
        ));
        card.setOnMouseExited(e -> card.setStyle(
            "-fx-background-color: " + MindDocTheme.BACKGROUND + "; " +
            "-fx-background-radius: 12; " +
            "-fx-cursor: hand;"
        ));

        // Click → detail dialog
        card.setOnMouseClicked(e -> showDetailsDialog(symptom, log));

        return card;
    }

    // ── Data / Actions ────────────────────────────────────────────────────────

    private void handleSave() {
        if (symptomCombo.getValue() == null) {
            showAlert(isUkrainian() ? "Оберіть симптом" : "Please select a symptom",
                Alert.AlertType.WARNING);
            return;
        }
        try {
            Symptom symptom = symptomCombo.getValue();
            int sev = (int) severitySlider.getValue();
            String notes = notesArea.getText().trim().isEmpty() ? null : notesArea.getText();

            SymptomLog log = new SymptomLog(0, currentUserId, symptom.getId(),
                sev, LocalDate.now().toString(), notes);
            symptomLogRepository.create(log);
            logger.info("Symptom logged: {} severity {}", symptom.getName(), sev);

            symptomCombo.setValue(null);
            severitySlider.setValue(5);
            notesArea.clear();
            refresh();
            showAlert(isUkrainian() ? "Симптом збережено!" : "Symptom logged successfully!",
                Alert.AlertType.INFORMATION);
        } catch (SQLException ex) {
            logger.error("Error logging symptom", ex);
            showAlert((isUkrainian() ? "Помилка: " : "Error: ") + ex.getMessage(),
                Alert.AlertType.ERROR);
        }
    }

    @Override
    public void refresh() {
        try {
            List<SymptomLog> logs = symptomLogRepository.findByUserId(currentUserId);
            cachedLogs.clear();
            historyItemsBox.getChildren().clear();

            LocalDate minDate = resolveMinDate();
            int shown = 0;

            for (SymptomLog log : logs) {
                try {
                    LocalDate ld = LocalDate.parse(log.getDate());
                    if (minDate != null && ld.isBefore(minDate)) continue;

                    Symptom symptom = symptomCache.computeIfAbsent(log.getSymptomId(), id -> {
                        try { return symptomRepository.findById(id); }
                        catch (SQLException e) { return null; }
                    });
                    if (symptom == null) continue;

                    cachedLogs.add(log);
                    historyItemsBox.getChildren().add(buildHistoryCard(symptom, log));
                    shown++;
                } catch (Exception ex) {
                    logger.warn("Skipping log entry: {}", ex.getMessage());
                }
            }

            if (shown == 0) {
                Label empty = new Label(isUkrainian()
                    ? "Немає записів за вибраний період"
                    : "No symptom logs for the selected period yet.");
                empty.setFont(Font.font("Segoe UI", 13));
                empty.setTextFill(Color.web(MindDocTheme.TEXT_MUTED));
                empty.setPadding(new Insets(24));
                empty.setAlignment(Pos.CENTER);
                empty.setMaxWidth(Double.MAX_VALUE);
                historyItemsBox.getChildren().add(empty);
            }
        } catch (SQLException e) {
            logger.error("Error refreshing symptom history", e);
        }
    }

    private LocalDate resolveMinDate() {
        if (filterCombo == null || filterCombo.getValue() == null) return null;
        LocalDate today = LocalDate.now();
        return switch (filterCombo.getValue()) {
            case "3 days",  "3 дні"       -> today.minusDays(3);
            case "1 week",  "1 тиждень"   -> today.minusWeeks(1);
            case "2 weeks", "2 тижні"     -> today.minusWeeks(2);
            case "1 month", "1 місяць"    -> today.minusMonths(1);
            default -> null;
        };
    }

    private void loadSymptoms(ComboBox<Symptom> combo) {
        try {
            List<Symptom> list = symptomRepository.findAll();
            combo.getItems().clear();
            combo.getItems().addAll(list);
            list.forEach(s -> symptomCache.put(s.getId(), s));
        } catch (SQLException e) {
            logger.error("Error loading symptoms", e);
        }
    }

    // ── Detail dialog ─────────────────────────────────────────────────────────

    private void showDetailsDialog(Symptom symptom, SymptomLog log) {
        String c = severityColor(log.getSeverity());

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(isUkrainian() ? "Деталі симптому" : "Symptom Details");
        dialog.setHeaderText(null);

        VBox root = new VBox(0);

        // colored header
        HBox dHeader = new HBox(14);
        dHeader.setAlignment(Pos.CENTER_LEFT);
        dHeader.setPadding(new Insets(20, 24, 20, 24));
        dHeader.setStyle("-fx-background-color: " + c + ";");

        StackPane iconCircle = new StackPane();
        iconCircle.setStyle(
            "-fx-background-color: white; -fx-background-radius: 24; " +
            "-fx-min-width: 48; -fx-min-height: 48; -fx-max-width: 48; -fx-max-height: 48;"
        );
        Label ico = new Label(symptom.getIcon());
        ico.setFont(Font.font("System", 24));
        iconCircle.getChildren().add(ico);

        VBox dTitle = new VBox(2);
        Label dn = new Label(translateSymptomName(symptom.getName()));
        dn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        dn.setTextFill(Color.WHITE);
        Label dSev = new Label("Severity: " + log.getSeverity() + "/10  ·  " + log.getDate());
        dSev.setFont(Font.font("Segoe UI", 12));
        dSev.setTextFill(Color.web("#ffffff99"));
        dTitle.getChildren().addAll(dn, dSev);

        dHeader.getChildren().addAll(iconCircle, dTitle);

        // body
        VBox body = new VBox(12);
        body.setPadding(new Insets(20, 24, 24, 24));

        if (log.getNotes() != null && !log.getNotes().isBlank()) {
            Label notesTitle = new Label(isUkrainian() ? "Нотатки:" : "Notes:");
            notesTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            notesTitle.setTextFill(Color.web(MindDocTheme.TEXT_SECONDARY));

            TextArea ta = new TextArea(log.getNotes());
            ta.setWrapText(true);
            ta.setEditable(false);
            ta.setPrefRowCount(5);

            body.getChildren().addAll(notesTitle, ta);
        } else {
            Label noNotes = new Label(isUkrainian() ? "Нотаток немає" : "No notes added");
            noNotes.setTextFill(Color.web(MindDocTheme.TEXT_MUTED));
            body.getChildren().add(noNotes);
        }

        root.getChildren().addAll(dHeader, body);
        dialog.getDialogPane().setContent(root);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.showAndWait();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Color by severity level */
    private String severityColor(int sev) {
        if (sev <= 3) return MindDocTheme.SUCCESS;   // green
        if (sev <= 5) return MindDocTheme.WARNING;   // yellow
        if (sev <= 7) return MindDocTheme.MOOD_BAD;  // orange
        return MindDocTheme.DANGER;                   // red
    }

    /** Thin colored accent bar at top of card */
    private Region accentBar(String color) {
        Region r = new Region();
        r.setPrefHeight(4);
        r.setMaxWidth(Double.MAX_VALUE);
        r.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 16 16 0 0;");
        return r;
    }

    private void showAlert(String msg, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setTitle(isUkrainian() ? "Трекер симптомів" : "Symptom Tracker");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private boolean isUkrainian() {
        return "Українська".equalsIgnoreCase(currentLanguage);
    }

    private String translateSymptomName(String name) {
        if (!isUkrainian() || name == null) return name;
        return switch (name) {
            case "Persistent sadness"    -> "Постійний смуток";
            case "Loss of interest"      -> "Втрата інтересу";
            case "Fatigue"               -> "Втома";
            case "Sleep problems"        -> "Проблеми зі сном";
            case "Excessive worry"       -> "Надмірне хвилювання";
            case "Racing thoughts"       -> "Нав'язливі думки";
            case "Tension"               -> "Напруження";
            case "Panic"                 -> "Паніка";
            case "Overwhelmed"           -> "Перевантаження";
            case "Irritability"          -> "Дратівливість";
            case "Concentration issues"  -> "Проблеми з концентрацією";
            case "Physical tension"      -> "Фізичне напруження";
            default -> name;
        };
    }

    // ── i18n ─────────────────────────────────────────────────────────────────

    public void applyLanguage(String language) {
        this.currentLanguage = language == null ? "English" : language;
        Symptom sel = symptomCombo != null ? symptomCombo.getValue() : null;

        if (logSectionTitle    != null) logSectionTitle.setText(isUkrainian() ? "✏️  Додати симптом" : "✏️  Log New Symptom");
        if (symptomLabel       != null) symptomLabel.setText(isUkrainian() ? "Оберіть симптом:" : "Select Symptom:");
        if (severityLabel      != null) severityLabel.setText(isUkrainian() ? "Рівень важкості:" : "Severity Level:");
        if (notesLabel         != null) notesLabel.setText(isUkrainian() ? "Нотатки (необов'язково):" : "Notes (optional):");
        if (saveButton         != null) saveButton.setText(isUkrainian() ? "Зберегти симптом" : "Log Symptom");
        if (historySectionTitle != null) historySectionTitle.setText(isUkrainian() ? "📋  Останні симптоми" : "📋  Recent Symptoms");
        if (filterLabel        != null) filterLabel.setText(isUkrainian() ? "Показати за:" : "Show last:");
        if (symptomCombo       != null) symptomCombo.setPromptText(isUkrainian() ? "Оберіть симптом…" : "Choose a symptom…");

        if (filterCombo != null) {
            String cur = filterCombo.getValue();
            filterCombo.getItems().setAll(
                isUkrainian() ? "3 дні"      : "3 days",
                isUkrainian() ? "1 тиждень"  : "1 week",
                isUkrainian() ? "2 тижні"    : "2 weeks",
                isUkrainian() ? "1 місяць"   : "1 month",
                "All"
            );
            filterCombo.setValue(mapFilter(cur));
        }
        if (symptomCombo != null) {
            loadSymptoms(symptomCombo);
            symptomCombo.setValue(sel);
        }
        refresh();
    }

    private String mapFilter(String v) {
        if (v == null) return isUkrainian() ? "1 тиждень" : "1 week";
        return switch (v) {
            case "3 days",  "3 дні"     -> isUkrainian() ? "3 дні"     : "3 days";
            case "1 week",  "1 тиждень" -> isUkrainian() ? "1 тиждень" : "1 week";
            case "2 weeks", "2 тижні"   -> isUkrainian() ? "2 тижні"   : "2 weeks";
            case "1 month", "1 місяць"  -> isUkrainian() ? "1 місяць"  : "1 month";
            default -> "All";
        };
    }
}
