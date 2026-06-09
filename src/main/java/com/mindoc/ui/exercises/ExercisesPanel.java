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
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;

public class ExercisesPanel extends BasePanel {
    private static final Logger logger = LoggerFactory.getLogger(ExercisesPanel.class);

    private int currentUserId;
    private DatabaseManager databaseManager;
    private ExerciseRepository exerciseRepository;
    private LearningProgressRepository progressRepository;

    private Exercise selectedExercise;
    private Button startButton;
    private final Map<Integer, String> exerciseStatuses = new HashMap<>();

    // Left panel
    private VBox exerciseCardsBox;
    private Label progressCountLabel;
    private ProgressBar progressBar;
    private String activeFilter = "All";
    private HBox filterChipsBox;
    private List<Exercise> allExercises = new ArrayList<>();

    // Right panel
    private VBox exerciseDetailsPanel;

    // i18n
    private Label titleLabel;

    public ExercisesPanel(int userId, DatabaseManager databaseManager) {
        this.currentUserId = userId;
        this.databaseManager = databaseManager;
        this.exerciseRepository = new ExerciseRepository(databaseManager.getConnection());
        this.progressRepository = new LearningProgressRepository(databaseManager.getConnection());
        initializeUI();
        refresh();
    }

    // ── Category meta ─────────────────────────────────────────────────────────

    private static final Map<String, String[]> CAT_META = new LinkedHashMap<>();
    static {
        CAT_META.put("All",          new String[]{"#10B981", "💪"});
        CAT_META.put("breathing",    new String[]{"#06b6d4", "🌬"});
        CAT_META.put("grounding",    new String[]{"#10B981", "🌿"});
        CAT_META.put("relaxation",   new String[]{"#8b5cf6", "😌"});
        CAT_META.put("cognitive",    new String[]{"#f59e0b", "🧠"});
        CAT_META.put("mindfulness",  new String[]{"#3b82f6", "🧘"});
        CAT_META.put("meditation",   new String[]{"#ec4899", "🕯"});
    }

    private String catColor(String cat) {
        if (cat == null) return "#10B981";
        String[] m = CAT_META.get(cat.toLowerCase());
        return m != null ? m[0] : "#10B981";
    }

    private String catEmoji(String cat) {
        if (cat == null) return "🏃";
        String[] m = CAT_META.get(cat.toLowerCase());
        return m != null ? m[1] : "🏃";
    }

    // ── Layout ────────────────────────────────────────────────────────────────

    private void initializeUI() {
        setStyle("-fx-background-color: " + MindDocTheme.BACKGROUND + ";");
        setPadding(new Insets(0));
        setSpacing(0);
        setFillWidth(true);

        getChildren().add(buildHeaderBanner());

        titleLabel = new Label("💪 Coping Exercises");
        titleLabel.setVisible(false);
        titleLabel.setManaged(false);
        getChildren().add(titleLabel);

        HBox body = new HBox(20);
        body.setPadding(new Insets(24));
        body.setFillHeight(true);
        VBox.setVgrow(body, Priority.ALWAYS);

        VBox left  = buildLeftPanel();
        exerciseDetailsPanel = buildDetailsPlaceholder();

        HBox.setHgrow(left,                 Priority.ALWAYS);
        HBox.setHgrow(exerciseDetailsPanel, Priority.ALWAYS);
        body.getChildren().addAll(left, exerciseDetailsPanel);
        getChildren().add(body);
    }

    // ── Header ────────────────────────────────────────────────────────────────

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

        Label h1 = new Label("💪 Coping Exercises");
        h1.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        h1.setTextFill(Color.WHITE);

        Label h2 = new Label("Choose and complete wellness exercises at your own pace");
        h2.setFont(Font.font("Segoe UI", 13));
        h2.setTextFill(Color.web("#d1fae5"));

        text.getChildren().addAll(h1, h2);

        Label deco = new Label("🏋");
        deco.setFont(Font.font("System", 56));
        deco.setOpacity(0.22);

        banner.getChildren().addAll(text, deco);
        return banner;
    }

    // ── Left panel ────────────────────────────────────────────────────────────

    private VBox buildLeftPanel() {
        Region bar = accentBar(MindDocTheme.PRIMARY);

        VBox inner = new VBox(14);
        inner.setPadding(new Insets(18, 20, 20, 20));
        VBox.setVgrow(inner, Priority.ALWAYS);

        // Header row
        HBox headerRow = new HBox(10);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label sectionTitle = new Label("Available Exercises");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        sectionTitle.setTextFill(Color.web(MindDocTheme.TEXT_PRIMARY));
        HBox.setHgrow(sectionTitle, Priority.ALWAYS);

        progressCountLabel = new Label("0 / 0 done");
        progressCountLabel.setFont(Font.font("Segoe UI", 11));
        progressCountLabel.setTextFill(Color.web(MindDocTheme.TEXT_MUTED));

        headerRow.getChildren().addAll(sectionTitle, progressCountLabel);

        // Progress bar
        progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(6);
        progressBar.setStyle(
            "-fx-accent: " + MindDocTheme.PRIMARY + "; " +
            "-fx-background-color: #e5e7eb; " +
            "-fx-background-radius: 3; " +
            "-fx-padding: 0;"
        );

        // Filter chips
        filterChipsBox = new HBox(8);
        filterChipsBox.setAlignment(Pos.CENTER_LEFT);
        rebuildFilterChips();

        // Card list
        exerciseCardsBox = new VBox(10);
        exerciseCardsBox.setStyle("-fx-background-color: transparent;");

        ScrollPane sp = new ScrollPane(exerciseCardsBox);
        sp.setFitToWidth(true);
        sp.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-background: transparent; " +
            "-fx-padding: 0;"
        );
        VBox.setVgrow(sp, Priority.ALWAYS);

        inner.getChildren().addAll(headerRow, progressBar, filterChipsBox, sp);

        VBox card = new VBox(0);
        card.setMinWidth(300);
        card.setPrefWidth(380);
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

    private void rebuildFilterChips() {
        filterChipsBox.getChildren().clear();
        for (String cat : CAT_META.keySet()) {
            String display = cat.substring(0, 1).toUpperCase() + cat.substring(1);
            Button chip = new Button(catEmoji(cat) + "  " + display);
            chip.setFont(Font.font("Segoe UI", 11));
            chip.setCursor(Cursor.HAND);
            boolean active = cat.equals(activeFilter);
            chip.setStyle(active ? chipActiveStyle(catColor(cat)) : chipInactiveStyle());
            chip.setOnAction(e -> {
                activeFilter = cat;
                rebuildFilterChips();
                applyFilter();
            });
            filterChipsBox.getChildren().add(chip);
        }
    }

    private String chipActiveStyle(String color) {
        return "-fx-background-color: " + color + "; " +
               "-fx-text-fill: white; " +
               "-fx-background-radius: 20; " +
               "-fx-padding: 5 12; " +
               "-fx-cursor: hand; " +
               "-fx-effect: dropshadow(three-pass-box, " + color + "55, 6, 0, 0, 2);";
    }

    private String chipInactiveStyle() {
        return "-fx-background-color: #f1f5f9; " +
               "-fx-text-fill: #6b7280; " +
               "-fx-background-radius: 20; " +
               "-fx-padding: 5 12; " +
               "-fx-cursor: hand;";
    }

    private void applyFilter() {
        exerciseCardsBox.getChildren().clear();
        for (Exercise ex : allExercises) {
            if ("All".equals(activeFilter) || activeFilter.equalsIgnoreCase(ex.getCategory())) {
                exerciseCardsBox.getChildren().add(buildExerciseCard(ex));
            }
        }
        if (exerciseCardsBox.getChildren().isEmpty()) {
            Label empty = new Label("No exercises in this category yet.");
            empty.setFont(Font.font("Segoe UI", 13));
            empty.setTextFill(Color.web(MindDocTheme.TEXT_MUTED));
            empty.setPadding(new Insets(20));
            exerciseCardsBox.getChildren().add(empty);
        }
    }

    // ── Exercise card ─────────────────────────────────────────────────────────

    private HBox buildExerciseCard(Exercise ex) {
        String color  = catColor(ex.getCategory());
        String status = exerciseStatuses.getOrDefault(ex.getId(), "not_started");
        boolean sel   = selectedExercise != null && selectedExercise.getId() == ex.getId();

        // Left color strip
        Region strip = new Region();
        strip.setPrefWidth(5);
        strip.setMinHeight(64);
        strip.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 12 0 0 12;");

        // Icon circle
        StackPane iconCircle = new StackPane();
        iconCircle.setStyle(
            "-fx-background-color: " + color + "22; " +
            "-fx-background-radius: 12; " +
            "-fx-min-width: 48; -fx-min-height: 48; " +
            "-fx-max-width: 48; -fx-max-height: 48;"
        );
        Label icon = new Label(catEmoji(ex.getCategory()));
        icon.setFont(Font.font("System", 22));
        iconCircle.getChildren().add(icon);

        // Text
        VBox text = new VBox(4);
        HBox.setHgrow(text, Priority.ALWAYS);

        Label name = new Label(ex.getTitle());
        name.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        name.setTextFill(Color.web(MindDocTheme.TEXT_PRIMARY));
        name.setWrapText(true);

        HBox meta = new HBox(10);
        meta.setAlignment(Pos.CENTER_LEFT);
        Label dur = new Label("⏱ " + ex.getDuration() + " min");
        dur.setFont(Font.font("Segoe UI", 11));
        dur.setTextFill(Color.web(MindDocTheme.TEXT_MUTED));
        HBox dots = buildDifficultyDots(parseDifficulty(ex.getDifficulty()), color);
        meta.getChildren().addAll(dur, dots);
        text.getChildren().addAll(name, meta);

        // Status chip
        VBox right = new VBox();
        right.setAlignment(Pos.CENTER);
        right.getChildren().add(buildStatusChip(status));

        HBox inner = new HBox(10);
        inner.setAlignment(Pos.CENTER_LEFT);
        inner.setPadding(new Insets(10, 12, 10, 10));
        HBox.setHgrow(inner, Priority.ALWAYS);
        inner.getChildren().addAll(iconCircle, text, right);

        HBox card = new HBox(0);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setCursor(Cursor.HAND);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle(sel ? cardSelectedStyle(color) : cardDefaultStyle());
        card.getChildren().addAll(strip, inner);

        card.setOnMouseEntered(e -> {
            if (selectedExercise == null || selectedExercise.getId() != ex.getId())
                card.setStyle(cardHoverStyle(color));
        });
        card.setOnMouseExited(e -> {
            if (selectedExercise == null || selectedExercise.getId() != ex.getId())
                card.setStyle(cardDefaultStyle());
        });
        card.setOnMouseClicked(e -> {
            selectedExercise = ex;
            applyFilter();
            showExerciseDetails(ex);
        });

        return card;
    }

    private int parseDifficulty(String diff) {
        if (diff == null) return 1;
        return switch (diff.toLowerCase()) {
            case "beginner"     -> 1;
            case "intermediate" -> 3;
            case "advanced"     -> 5;
            default -> {
                try { yield Integer.parseInt(diff); } catch (Exception e) { yield 1; }
            }
        };
    }

    private HBox buildDifficultyDots(int level, String color) {
        HBox dots = new HBox(3);
        dots.setAlignment(Pos.CENTER_LEFT);
        for (int i = 1; i <= 5; i++) {
            Circle dot = new Circle(4);
            dot.setFill(i <= level ? Color.web(color) : Color.web("#e5e7eb"));
            dots.getChildren().add(dot);
        }
        return dots;
    }

    private Label buildStatusChip(String status) {
        String text, bg, fg;
        switch (status) {
            case "completed":   text = "✓ Done";   bg = "#d1fae5"; fg = "#065f46"; break;
            case "in_progress": text = "⏳ Active"; bg = "#fef3c7"; fg = "#92400e"; break;
            default:            text = "New";       bg = "#f1f5f9"; fg = "#6b7280";
        }
        Label chip = new Label(text);
        chip.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        chip.setStyle(
            "-fx-background-color: " + bg + "; " +
            "-fx-text-fill: " + fg + "; " +
            "-fx-padding: 3 8; " +
            "-fx-background-radius: 10;"
        );
        return chip;
    }

    private String cardDefaultStyle() {
        return "-fx-background-color: #f8fafc; " +
               "-fx-background-radius: 12; -fx-border-radius: 12; " +
               "-fx-border-color: #e5e7eb; -fx-border-width: 1;";
    }

    private String cardHoverStyle(String color) {
        return "-fx-background-color: " + color + "0d; " +
               "-fx-background-radius: 12; -fx-border-radius: 12; " +
               "-fx-border-color: " + color + "66; -fx-border-width: 1.5;";
    }

    private String cardSelectedStyle(String color) {
        return "-fx-background-color: " + color + "15; " +
               "-fx-background-radius: 12; -fx-border-radius: 12; " +
               "-fx-border-color: " + color + "; -fx-border-width: 2; " +
               "-fx-effect: dropshadow(three-pass-box, " + color + "44, 8, 0, 0, 2);";
    }

    // ── Details panel ─────────────────────────────────────────────────────────

    private VBox buildDetailsPlaceholder() {
        VBox panel = new VBox(0);
        panel.setMinWidth(420);
        panel.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 16; " +
            "-fx-border-radius: 16; " +
            "-fx-effect: dropshadow(three-pass-box, #00000014, 10, 0, 0, 3);"
        );
        VBox.setVgrow(panel, Priority.ALWAYS);

        VBox ph = new VBox(16);
        ph.setAlignment(Pos.CENTER);
        ph.setPadding(new Insets(60, 40, 40, 40));
        VBox.setVgrow(ph, Priority.ALWAYS);

        Label ico = new Label("🏃");
        ico.setFont(Font.font("System", 52));
        ico.setOpacity(0.3);

        Label msg = new Label("Select an exercise to get started");
        msg.setFont(Font.font("Segoe UI", 15));
        msg.setTextFill(Color.web(MindDocTheme.TEXT_MUTED));

        Label sub = new Label("Click any exercise card on the left\nto view its details and instructions.");
        sub.setFont(Font.font("Segoe UI", 12));
        sub.setTextFill(Color.web(MindDocTheme.TEXT_MUTED));
        sub.setTextAlignment(TextAlignment.CENTER);
        sub.setAlignment(Pos.CENTER);

        ph.getChildren().addAll(ico, msg, sub);
        panel.getChildren().add(ph);
        return panel;
    }

    private void showExerciseDetails(Exercise ex) {
        String color  = catColor(ex.getCategory());
        String status = exerciseStatuses.getOrDefault(ex.getId(), "not_started");

        exerciseDetailsPanel.getChildren().clear();

        // ── Colored header ─────────────────────────────────────────────────
        HBox detailHeader = new HBox(16);
        detailHeader.setAlignment(Pos.CENTER_LEFT);
        detailHeader.setPadding(new Insets(22, 24, 22, 24));
        detailHeader.setStyle(
            "-fx-background-color: linear-gradient(from 0% 0% to 100% 0%, " + color + ", " + color + "99); " +
            "-fx-background-radius: 16 16 0 0;"
        );

        StackPane iconCircle = new StackPane();
        iconCircle.setStyle(
            "-fx-background-color: #ffffff33; " +
            "-fx-background-radius: 16; " +
            "-fx-min-width: 64; -fx-min-height: 64; " +
            "-fx-max-width: 64; -fx-max-height: 64;"
        );
        Label iconLbl = new Label(catEmoji(ex.getCategory()));
        iconLbl.setFont(Font.font("System", 32));
        iconCircle.getChildren().add(iconLbl);

        VBox headerText = new VBox(6);
        HBox.setHgrow(headerText, Priority.ALWAYS);

        Label titleLbl = new Label(ex.getTitle());
        titleLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 19));
        titleLbl.setTextFill(Color.WHITE);
        titleLbl.setWrapText(true);

        HBox chipRow = new HBox(8);
        chipRow.setAlignment(Pos.CENTER_LEFT);
        chipRow.getChildren().addAll(
            detailChip("📁 " + ex.getCategory(), "#ffffff44", "white"),
            detailChip("⏱ " + ex.getDuration() + " min", "#ffffff44", "white"),
            detailChip("★ " + ex.getDifficulty(), "#ffffff44", "white"),
            buildStatusChipLarge(status)
        );
        headerText.getChildren().addAll(titleLbl, chipRow);
        detailHeader.getChildren().addAll(iconCircle, headerText);

        // ── Timer row ─────────────────────────────────────────────────────
        HBox timerRow = new HBox(10);
        timerRow.setAlignment(Pos.CENTER);
        timerRow.setPadding(new Insets(14, 24, 0, 24));
        timerRow.setStyle(
            "-fx-background-color: " + color + "11; " +
            "-fx-border-color: " + color + "33; " +
            "-fx-border-width: 0 0 1 0;"
        );

        // Timer display widget
        StackPane timerWidget = new StackPane();
        timerWidget.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 50; " +
            "-fx-border-color: " + color + "; " +
            "-fx-border-width: 3; " +
            "-fx-border-radius: 50; " +
            "-fx-min-width: 80; -fx-min-height: 80; " +
            "-fx-max-width: 80; -fx-max-height: 80; " +
            "-fx-effect: dropshadow(three-pass-box, " + color + "44, 8, 0, 0, 2);"
        );
        VBox timerText = new VBox(0);
        timerText.setAlignment(Pos.CENTER);
        Label timerNum = new Label(String.valueOf(ex.getDuration()));
        timerNum.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        timerNum.setTextFill(Color.web(color));
        Label timerUnit = new Label("min");
        timerUnit.setFont(Font.font("Segoe UI", 10));
        timerUnit.setTextFill(Color.web(MindDocTheme.TEXT_MUTED));
        timerText.getChildren().addAll(timerNum, timerUnit);
        timerWidget.getChildren().add(timerText);

        VBox timerMeta = new VBox(3);
        timerMeta.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(timerMeta, Priority.ALWAYS);
        Label timerTitle = new Label("Exercise Duration");
        timerTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        timerTitle.setTextFill(Color.web(MindDocTheme.TEXT_PRIMARY));
        Label timerSub = new Label("Set aside " + ex.getDuration() + " minutes of uninterrupted time.");
        timerSub.setFont(Font.font("Segoe UI", 12));
        timerSub.setTextFill(Color.web(MindDocTheme.TEXT_MUTED));
        timerMeta.getChildren().addAll(timerTitle, timerSub);

        timerRow.getChildren().addAll(timerWidget, timerMeta);

        // ── Body ──────────────────────────────────────────────────────────
        VBox body = new VBox(16);
        body.setPadding(new Insets(20, 24, 24, 24));
        VBox.setVgrow(body, Priority.ALWAYS);

        body.getChildren().add(buildSection("📝 Description",
            safeText(ex.getDescription(), "No description available for this exercise."),
            color, 3));

        body.getChildren().add(buildSection("📋 Step-by-Step Instructions",
            safeText(ex.getInstructions(),
                "1. Find a quiet place.\n" +
                "2. Follow the exercise description step by step.\n" +
                "3. Keep steady breathing during the full duration.\n" +
                "4. After completion, note how you feel.").replace("\\n", "\n"),
            color, 6));

        startButton = new Button();
        startButton.setPrefWidth(Double.MAX_VALUE);
        startButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        updateStartButtonState(ex);
        startButton.setOnAction(e -> startExercise(ex));
        body.getChildren().add(startButton);

        ScrollPane sp = new ScrollPane();
        VBox spContent = new VBox(0);
        spContent.getChildren().addAll(timerRow, body);
        sp.setContent(spContent);
        sp.setFitToWidth(true);
        sp.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-background: transparent; " +
            "-fx-padding: 0;"
        );
        VBox.setVgrow(sp, Priority.ALWAYS);

        exerciseDetailsPanel.getChildren().addAll(detailHeader, sp);
    }

    private VBox buildSection(String label, String text, String color, int rows) {
        VBox box = new VBox(8);

        HBox labelRow = new HBox(8);
        labelRow.setAlignment(Pos.CENTER_LEFT);

        Region dot = new Region();
        dot.setPrefWidth(4);
        dot.setPrefHeight(16);
        dot.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 2;");

        Label lbl = new Label(label);
        lbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lbl.setTextFill(Color.web(MindDocTheme.TEXT_PRIMARY));
        labelRow.getChildren().addAll(dot, lbl);

        TextArea ta = new TextArea(text);
        ta.setWrapText(true);
        ta.setEditable(false);
        ta.setPrefRowCount(rows);
        ta.setStyle(
            "-fx-control-inner-background: #f8fafc; " +
            "-fx-background-color: #f8fafc; " +
            "-fx-border-color: #e5e7eb; " +
            "-fx-border-radius: 10; " +
            "-fx-background-radius: 10; " +
            "-fx-padding: 10; " +
            "-fx-font-size: 12;"
        );
        box.getChildren().addAll(labelRow, ta);
        return box;
    }

    private Label detailChip(String text, String bg, String fg) {
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", 11));
        l.setStyle(
            "-fx-background-color: " + bg + "; " +
            "-fx-text-fill: " + fg + "; " +
            "-fx-padding: 3 9; " +
            "-fx-background-radius: 10;"
        );
        return l;
    }

    private Label buildStatusChipLarge(String status) {
        String text, bg, fg;
        switch (status) {
            case "completed":   text = "✓ Completed";  bg = "#d1fae5"; fg = "#065f46"; break;
            case "in_progress": text = "⏳ In Progress"; bg = "#fef3c7"; fg = "#92400e"; break;
            default:            text = "🆕 Not Started"; bg = "#f1f5f9"; fg = "#6b7280";
        }
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        l.setStyle(
            "-fx-background-color: " + bg + "; " +
            "-fx-text-fill: " + fg + "; " +
            "-fx-padding: 4 10; " +
            "-fx-background-radius: 10;"
        );
        return l;
    }

    // ── Data / Actions ────────────────────────────────────────────────────────

    @Override
    public void refresh() {
        try {
            exerciseStatuses.clear();
            exerciseStatuses.putAll(progressRepository.findStatusByUserAndType(currentUserId, "exercise"));
            allExercises = exerciseRepository.findAll();

            int total = allExercises.size();
            long done  = allExercises.stream()
                .filter(e -> "completed".equals(exerciseStatuses.getOrDefault(e.getId(), "")))
                .count();

            if (progressCountLabel != null) progressCountLabel.setText(done + " / " + total + " done");
            if (progressBar        != null) progressBar.setProgress(total > 0 ? (double) done / total : 0);

            applyFilter();

            if (selectedExercise != null && startButton != null) {
                boolean stillThere = allExercises.stream().anyMatch(e -> e.getId() == selectedExercise.getId());
                if (stillThere) showExerciseDetails(selectedExercise);
                else {
                    selectedExercise = null;
                    exerciseDetailsPanel.getChildren().clear();
                    exerciseDetailsPanel.getChildren().addAll(buildDetailsPlaceholder().getChildren());
                }
            }
        } catch (SQLException e) {
            logger.error("Error refreshing exercises", e);
        }
    }

    private void startExercise(Exercise ex) {
        String status = exerciseStatuses.getOrDefault(ex.getId(), "not_started");
        if ("completed".equals(status)) return;

        try {
            if (!"in_progress".equals(status)) {
                progressRepository.saveStatus(currentUserId, "exercise", ex.getId(), "in_progress");
                exerciseStatuses.put(ex.getId(), "in_progress");
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(I18n.t("exercise_progress", "Exercise Progress"));
            alert.setHeaderText(ex.getTitle());
            alert.setContentText(I18n.t("marked_in_progress", "Exercise marked as in progress.\nMark it as completed when done."));
            ButtonType completeBtn = new ButtonType(I18n.t("mark_completed", "Mark as Completed"), ButtonBar.ButtonData.OK_DONE);
            ButtonType keepBtn     = new ButtonType(I18n.t("keep_in_progress", "Keep In Progress"), ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(completeBtn, keepBtn);

            alert.showAndWait().ifPresent(result -> {
                if (result == completeBtn) {
                    try {
                        progressRepository.saveStatus(currentUserId, "exercise", ex.getId(), "completed");
                        exerciseStatuses.put(ex.getId(), "completed");
                    } catch (SQLException e) {
                        logger.error("Error marking exercise completed", e);
                    }
                }
            });

            updateStartButtonState(ex);
            applyFilter();
            showExerciseDetails(ex);
        } catch (SQLException e) {
            logger.error("Error starting exercise", e);
        }
    }

    private void updateStartButtonState(Exercise ex) {
        if (startButton == null) return;
        String color  = catColor(ex.getCategory());
        String status = exerciseStatuses.getOrDefault(ex.getId(), "not_started");

        if ("completed".equals(status)) {
            startButton.setText("✓  Exercise Completed");
            startButton.setDisable(true);
            startButton.setStyle(
                "-fx-background-color: #d1fae5; " +
                "-fx-text-fill: #065f46; " +
                "-fx-background-radius: 12; " +
                "-fx-padding: 14 0; " +
                "-fx-font-size: 14;"
            );
            return;
        }
        startButton.setDisable(false);
        if ("in_progress".equals(status)) {
            startButton.setText("⏳  Continue Exercise");
            startButton.setStyle(
                "-fx-background-color: #fef3c7; " +
                "-fx-text-fill: #92400e; " +
                "-fx-background-radius: 12; " +
                "-fx-padding: 14 0; " +
                "-fx-font-size: 14; " +
                "-fx-cursor: hand;"
            );
        } else {
            startButton.setText("▶  Start Exercise");
            startButton.setStyle(
                "-fx-background-color: " + color + "; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 12; " +
                "-fx-padding: 14 0; " +
                "-fx-font-size: 14; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(three-pass-box, " + color + "55, 8, 0, 0, 3);"
            );
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Region accentBar(String color) {
        Region r = new Region();
        r.setPrefHeight(4);
        r.setMaxWidth(Double.MAX_VALUE);
        r.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 16 16 0 0;");
        return r;
    }

    private String safeText(String v, String fallback) {
        return (v == null || v.isBlank()) ? fallback : v;
    }

    public void applyLanguage(String language) {
        I18n.setLanguage(language);
        if (selectedExercise != null) showExerciseDetails(selectedExercise);
        applyFilter();
    }
}
