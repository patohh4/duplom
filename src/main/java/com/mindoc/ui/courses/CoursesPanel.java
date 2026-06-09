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
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;

public class CoursesPanel extends BasePanel {
    private static final Logger logger = LoggerFactory.getLogger(CoursesPanel.class);

    private int currentUserId;
    private DatabaseManager databaseManager;
    private CBTCourseRepository courseRepository;
    private LearningProgressRepository progressRepository;

    private CBTCourse selectedCourse;
    private Button startButton;
    private final Map<Integer, String> courseStatuses = new HashMap<>();

    // Left panel UI
    private VBox courseCardsBox;
    private Label progressCountLabel;
    private ProgressBar progressBar;
    private String activeFilter = "All";
    private FlowPane filterChipsBox;
    private List<CBTCourse> allCourses = new ArrayList<>();

    // Right panel
    private VBox courseDetailsPanel;

    // i18n
    private Label titleLabel;

    public CoursesPanel(int userId, DatabaseManager databaseManager) {
        this.currentUserId = userId;
        this.databaseManager = databaseManager;
        this.courseRepository = new CBTCourseRepository(databaseManager.getConnection());
        this.progressRepository = new LearningProgressRepository(databaseManager.getConnection());
        initializeUI();
        refresh();
    }

    // ── Category meta ─────────────────────────────────────────────────────────

    private static final Map<String, String[]> CAT_META = new LinkedHashMap<>();
    static {
        // category → [hex color, emoji]
        CAT_META.put("All",        new String[]{"#10B981", "📚"});
        CAT_META.put("depression", new String[]{"#818cf8", "💙"});
        CAT_META.put("anxiety",    new String[]{"#f59e0b", "🌬"});
        CAT_META.put("stress",     new String[]{"#ef4444", "🧘"});
        CAT_META.put("sleep",      new String[]{"#3b82f6", "😴"});
        CAT_META.put("general",    new String[]{"#10B981", "🌱"});
        CAT_META.put("cbt",        new String[]{"#8b5cf6", "🧠"});
    }

    private String catColor(String cat) {
        if (cat == null) return "#10B981";
        String[] m = CAT_META.get(cat.toLowerCase());
        return m != null ? m[0] : "#10B981";
    }

    private String catEmoji(String cat) {
        if (cat == null) return "📖";
        String[] m = CAT_META.get(cat.toLowerCase());
        return m != null ? m[1] : "📖";
    }

    // ── Layout ────────────────────────────────────────────────────────────────

    private void initializeUI() {
        setStyle("-fx-background-color: " + MindDocTheme.BACKGROUND + ";");
        setPadding(new Insets(0));
        setSpacing(0);
        setFillWidth(true);

        // Flush header
        getChildren().add(buildHeaderBanner());

        // hidden label for applyLanguage
        titleLabel = new Label("📚 CBT Learning Courses");
        titleLabel.setVisible(false);
        titleLabel.setManaged(false);
        getChildren().add(titleLabel);

        // Body
        HBox body = new HBox(20);
        body.setPadding(new Insets(24));
        body.setFillHeight(true);
        VBox.setVgrow(body, Priority.ALWAYS);

        VBox left  = buildLeftPanel();
        courseDetailsPanel = buildDetailsPlaceholderPanel();

        HBox.setHgrow(left,               Priority.ALWAYS);
        HBox.setHgrow(courseDetailsPanel, Priority.ALWAYS);
        body.getChildren().addAll(left, courseDetailsPanel);
        getChildren().add(body);
    }

    // ── Header banner ─────────────────────────────────────────────────────────

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

        Label h1 = new Label("📚 CBT Learning Courses");
        h1.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        h1.setTextFill(Color.WHITE);

        Label h2 = new Label("Explore evidence-based courses to support your mental wellbeing");
        h2.setFont(Font.font("Segoe UI", 13));
        h2.setTextFill(Color.web("#d1fae5"));

        text.getChildren().addAll(h1, h2);

        Label deco = new Label("🎓");
        deco.setFont(Font.font("System", 56));
        deco.setOpacity(0.22);

        banner.getChildren().addAll(text, deco);
        return banner;
    }

    // ── Left panel ────────────────────────────────────────────────────────────

    private VBox buildLeftPanel() {
        // accent bar
        Region bar = accentBar(MindDocTheme.PRIMARY);

        VBox inner = new VBox(14);
        inner.setPadding(new Insets(18, 20, 20, 20));
        VBox.setVgrow(inner, Priority.ALWAYS);

        // Header row with title + progress
        HBox headerRow = new HBox(10);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label sectionTitle = new Label("Available Courses");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        sectionTitle.setTextFill(Color.web(MindDocTheme.TEXT_PRIMARY));
        HBox.setHgrow(sectionTitle, Priority.ALWAYS);

        progressCountLabel = new Label("0 / 0 completed");
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

        // Category filter chips — FlowPane wraps when too narrow
        filterChipsBox = new FlowPane(8, 6);
        filterChipsBox.setAlignment(Pos.CENTER_LEFT);
        rebuildFilterChips();

        // Scrollable cards
        courseCardsBox = new VBox(10);
        courseCardsBox.setStyle("-fx-background-color: transparent;");

        ScrollPane sp = new ScrollPane(courseCardsBox);
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
            String displayName = cat.substring(0, 1).toUpperCase() + cat.substring(1);
            Button chip = new Button(catEmoji(cat) + "  " + displayName);
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
        courseCardsBox.getChildren().clear();
        for (CBTCourse c : allCourses) {
            if ("All".equals(activeFilter) || activeFilter.equalsIgnoreCase(c.getCategory())) {
                courseCardsBox.getChildren().add(buildCourseCard(c));
            }
        }
        if (courseCardsBox.getChildren().isEmpty()) {
            Label empty = new Label("No courses in this category yet.");
            empty.setFont(Font.font("Segoe UI", 13));
            empty.setTextFill(Color.web(MindDocTheme.TEXT_MUTED));
            empty.setPadding(new Insets(20));
            courseCardsBox.getChildren().add(empty);
        }
    }

    // ── Course card ───────────────────────────────────────────────────────────

    private HBox buildCourseCard(CBTCourse course) {
        String color  = catColor(course.getCategory());
        String status = courseStatuses.getOrDefault(course.getId(), "not_started");
        boolean selected = selectedCourse != null && selectedCourse.getId() == course.getId();

        // Left color strip
        Region strip = new Region();
        strip.setPrefWidth(5);
        strip.setMinHeight(60);
        strip.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 12 0 0 12;");

        // Emoji circle
        StackPane iconCircle = new StackPane();
        iconCircle.setStyle(
            "-fx-background-color: " + color + "22; " +
            "-fx-background-radius: 12; " +
            "-fx-min-width: 48; -fx-min-height: 48; " +
            "-fx-max-width: 48; -fx-max-height: 48;"
        );
        Label icon = new Label(catEmoji(course.getCategory()));
        icon.setFont(Font.font("System", 22));
        iconCircle.getChildren().add(icon);

        // Text block
        VBox text = new VBox(4);
        HBox.setHgrow(text, Priority.ALWAYS);
        text.setMinWidth(0); // allow shrink so right chip keeps its size

        Label name = new Label(course.getTitle());
        name.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        name.setTextFill(Color.web(MindDocTheme.TEXT_PRIMARY));
        name.setWrapText(true);

        HBox meta = new HBox(10);
        meta.setAlignment(Pos.CENTER_LEFT);

        Label dur = new Label("⏱ " + course.getDuration() + " min");
        dur.setFont(Font.font("Segoe UI", 11));
        dur.setTextFill(Color.web(MindDocTheme.TEXT_MUTED));

        HBox dots = buildDifficultyDots(course.getDifficulty(), color);

        meta.getChildren().addAll(dur, dots);
        text.getChildren().addAll(name, meta);

        // Status chip — fixed width, never shrinks
        VBox right = new VBox();
        right.setAlignment(Pos.CENTER);
        right.setMinWidth(Region.USE_PREF_SIZE);
        right.getChildren().add(buildStatusChip(status, color));

        // Wrapper
        HBox inner = new HBox(10);
        inner.setAlignment(Pos.CENTER_LEFT);
        inner.setPadding(new Insets(10, 12, 10, 10));
        HBox.setHgrow(inner, Priority.ALWAYS);
        inner.getChildren().addAll(iconCircle, text, right);

        HBox card = new HBox(0);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setCursor(Cursor.HAND);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle(selected ? cardSelectedStyle(color) : cardDefaultStyle());
        card.getChildren().addAll(strip, inner);

        card.setOnMouseEntered(e -> {
            if (selectedCourse == null || selectedCourse.getId() != course.getId())
                card.setStyle(cardHoverStyle(color));
        });
        card.setOnMouseExited(e -> {
            if (selectedCourse == null || selectedCourse.getId() != course.getId())
                card.setStyle(cardDefaultStyle());
        });
        card.setOnMouseClicked(e -> {
            selectedCourse = course;
            applyFilter(); // re-render to update selected state
            showCourseDetails(course);
        });

        return card;
    }

    private String cardDefaultStyle() {
        return "-fx-background-color: #f8fafc; " +
               "-fx-background-radius: 12; " +
               "-fx-border-radius: 12; " +
               "-fx-border-color: #e5e7eb; " +
               "-fx-border-width: 1;";
    }

    private String cardHoverStyle(String color) {
        return "-fx-background-color: " + color + "0d; " +
               "-fx-background-radius: 12; " +
               "-fx-border-radius: 12; " +
               "-fx-border-color: " + color + "66; " +
               "-fx-border-width: 1.5;";
    }

    private String cardSelectedStyle(String color) {
        return "-fx-background-color: " + color + "15; " +
               "-fx-background-radius: 12; " +
               "-fx-border-radius: 12; " +
               "-fx-border-color: " + color + "; " +
               "-fx-border-width: 2; " +
               "-fx-effect: dropshadow(three-pass-box, " + color + "44, 8, 0, 0, 2);";
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

    private Label buildStatusChip(String status, String color) {
        String text, bg, fg;
        switch (status) {
            case "completed":
                text = "✓ Done"; bg = "#d1fae5"; fg = "#065f46"; break;
            case "in_progress":
                text = "⏳ Active"; bg = "#fef3c7"; fg = "#92400e"; break;
            default:
                text = "New"; bg = "#f1f5f9"; fg = "#6b7280";
        }
        Label chip = new Label(text);
        chip.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        chip.setMinWidth(Region.USE_PREF_SIZE); // never truncate
        chip.setStyle(
            "-fx-background-color: " + bg + "; " +
            "-fx-text-fill: " + fg + "; " +
            "-fx-padding: 3 8; " +
            "-fx-background-radius: 10;"
        );
        return chip;
    }

    // ── Details panel ─────────────────────────────────────────────────────────

    private VBox buildDetailsPlaceholderPanel() {
        VBox panel = new VBox(0);
        panel.setMinWidth(420);
        panel.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 16; " +
            "-fx-border-radius: 16; " +
            "-fx-effect: dropshadow(three-pass-box, #00000014, 10, 0, 0, 3);"
        );
        VBox.setVgrow(panel, Priority.ALWAYS);

        // Placeholder content
        VBox ph = new VBox(16);
        ph.setAlignment(Pos.CENTER);
        ph.setPadding(new Insets(60, 40, 40, 40));
        VBox.setVgrow(ph, Priority.ALWAYS);

        Label icon = new Label("📖");
        icon.setFont(Font.font("System", 52));
        icon.setOpacity(0.3);

        Label msg = new Label("Select a course to get started");
        msg.setFont(Font.font("Segoe UI", 15));
        msg.setTextFill(Color.web(MindDocTheme.TEXT_MUTED));

        Label sub = new Label("Click any course card on the left to view\nits details, description and content.");
        sub.setFont(Font.font("Segoe UI", 12));
        sub.setTextFill(Color.web(MindDocTheme.TEXT_MUTED));
        sub.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        sub.setAlignment(Pos.CENTER);

        ph.getChildren().addAll(icon, msg, sub);
        panel.getChildren().add(ph);
        return panel;
    }

    private void showCourseDetails(CBTCourse course) {
        String color = catColor(course.getCategory());
        String status = courseStatuses.getOrDefault(course.getId(), "not_started");

        courseDetailsPanel.getChildren().clear();

        // ── Colored header ─────────────────────────────────────────────────
        HBox detailHeader = new HBox(16);
        detailHeader.setAlignment(Pos.CENTER_LEFT);
        detailHeader.setPadding(new Insets(22, 24, 22, 24));
        detailHeader.setStyle(
            "-fx-background-color: linear-gradient(from 0% 0% to 100% 0%, " + color + ", " + color + "aa); " +
            "-fx-background-radius: 16 16 0 0;"
        );

        StackPane iconCircle = new StackPane();
        iconCircle.setStyle(
            "-fx-background-color: #ffffff33; " +
            "-fx-background-radius: 16; " +
            "-fx-min-width: 64; -fx-min-height: 64; " +
            "-fx-max-width: 64; -fx-max-height: 64;"
        );
        Label iconLbl = new Label(catEmoji(course.getCategory()));
        iconLbl.setFont(Font.font("System", 32));
        iconCircle.getChildren().add(iconLbl);

        VBox headerText = new VBox(5);
        HBox.setHgrow(headerText, Priority.ALWAYS);

        Label titleLbl = new Label(course.getTitle());
        titleLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 19));
        titleLbl.setTextFill(Color.WHITE);
        titleLbl.setWrapText(true);

        FlowPane chipRow = new FlowPane(8, 6);
        chipRow.setAlignment(Pos.CENTER_LEFT);
        chipRow.getChildren().addAll(
            detailChip("📁 " + course.getCategory(), "#ffffff44", "white"),
            detailChip("⏱ " + course.getDuration() + " min", "#ffffff44", "white"),
            detailChip("★ Level " + course.getDifficulty() + "/5", "#ffffff44", "white"),
            buildStatusChipLarge(status)
        );
        headerText.getChildren().addAll(titleLbl, chipRow);
        detailHeader.getChildren().addAll(iconCircle, headerText);

        // ── Body ──────────────────────────────────────────────────────────
        VBox body = new VBox(16);
        body.setPadding(new Insets(22, 24, 24, 24));
        VBox.setVgrow(body, Priority.ALWAYS);

        // Description card
        body.getChildren().add(buildSection("📝 Description",
            safeText(course.getDescription(), "No description available."), color, 3));

        // Content card
        body.getChildren().add(buildSection("📋 Course Content",
            safeText(course.getContent(),
                "1. Read the description carefully.\n" +
                "2. Practice one core idea from this topic today.\n" +
                "3. Write down one observation in your journal.\n" +
                "4. Repeat tomorrow and compare how you feel.").replace("\\n", "\n"),
            color, 6));

        // Start button
        startButton = new Button();
        startButton.setPrefWidth(Double.MAX_VALUE);
        startButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        updateStartButtonState(course);
        startButton.setOnAction(e -> startCourse(course));
        body.getChildren().add(startButton);

        ScrollPane sp = new ScrollPane(body);
        sp.setFitToWidth(true);
        sp.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-background: transparent; " +
            "-fx-padding: 0;"
        );
        VBox.setVgrow(sp, Priority.ALWAYS);

        courseDetailsPanel.getChildren().addAll(detailHeader, sp);
    }

    /** A labeled section with a read-only TextArea */
    private VBox buildSection(String label, String text, String accentColor, int rows) {
        VBox box = new VBox(8);

        HBox labelRow = new HBox(8);
        labelRow.setAlignment(Pos.CENTER_LEFT);

        Region dot = new Region();
        dot.setPrefWidth(4);
        dot.setPrefHeight(16);
        dot.setStyle("-fx-background-color: " + accentColor + "; -fx-background-radius: 2;");

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
        l.setMinWidth(Region.USE_PREF_SIZE);
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
            case "completed":   text = "✓ Completed";   bg = "#d1fae5"; fg = "#065f46"; break;
            case "in_progress": text = "⏳ In Progress"; bg = "#fef3c7"; fg = "#92400e"; break;
            default:            text = "🆕 Not Started"; bg = "#f1f5f9"; fg = "#6b7280";
        }
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        l.setMinWidth(Region.USE_PREF_SIZE);
        l.setStyle(
            "-fx-background-color: " + bg + "; " +
            "-fx-text-fill: " + fg + "; " +
            "-fx-padding: 4 10; " +
            "-fx-background-radius: 10;"
        );
        return l;
    }

    // ── Data / Logic ──────────────────────────────────────────────────────────

    @Override
    public void refresh() {
        try {
            courseStatuses.clear();
            courseStatuses.putAll(progressRepository.findStatusByUserAndType(currentUserId, "course"));

            allCourses = courseRepository.findAll();

            // Update progress bar
            int total     = allCourses.size();
            long completed = allCourses.stream()
                .filter(c -> "completed".equals(courseStatuses.getOrDefault(c.getId(), "")))
                .count();
            if (progressCountLabel != null) {
                progressCountLabel.setText(completed + " / " + total + " completed");
            }
            if (progressBar != null) {
                progressBar.setProgress(total > 0 ? (double) completed / total : 0);
            }

            applyFilter();

            if (selectedCourse != null && startButton != null) {
                updateStartButtonState(selectedCourse);
                // refresh details header status chip
                showCourseDetails(selectedCourse);
            }
        } catch (SQLException e) {
            logger.error("Error refreshing courses", e);
        }
    }

    private void startCourse(CBTCourse course) {
        String status = courseStatuses.getOrDefault(course.getId(), "not_started");
        if ("completed".equals(status)) return;

        try {
            if (!"in_progress".equals(status)) {
                progressRepository.saveStatus(currentUserId, "course", course.getId(), "in_progress");
                courseStatuses.put(course.getId(), "in_progress");
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(I18n.t("course_progress", "Course Progress"));
            alert.setHeaderText(course.getTitle());
            alert.setContentText(I18n.t("marked_in_progress", "Course marked as in progress.\nMark it as completed when done."));
            ButtonType completedButton   = new ButtonType(I18n.t("mark_completed", "Mark Completed"), ButtonBar.ButtonData.OK_DONE);
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
            applyFilter();
            showCourseDetails(course);
        } catch (SQLException e) {
            logger.error("Error starting course", e);
        }
    }

    private void updateStartButtonState(CBTCourse course) {
        if (startButton == null) return;
        String status = courseStatuses.getOrDefault(course.getId(), "not_started");
        if ("completed".equals(status)) {
            startButton.setText(I18n.t("course_completed", "✓  Course Completed"));
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
            startButton.setText(I18n.t("in_progress", "⏳  Continue Course"));
            startButton.setStyle(
                "-fx-background-color: #fef3c7; " +
                "-fx-text-fill: #92400e; " +
                "-fx-background-radius: 12; " +
                "-fx-padding: 14 0; " +
                "-fx-font-size: 14; " +
                "-fx-cursor: hand;"
            );
        } else {
            String color = catColor(course.getCategory());
            startButton.setText(I18n.t("start_course", "▶  Start Course"));
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
        if (selectedCourse != null) showCourseDetails(selectedCourse);
        applyFilter();
    }
}
