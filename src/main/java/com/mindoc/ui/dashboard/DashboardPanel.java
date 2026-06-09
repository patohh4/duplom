package com.mindoc.ui.dashboard;

import com.mindoc.database.DatabaseManager;
import com.mindoc.model.MoodEntry;
import com.mindoc.repository.MoodEntryRepository;
import com.mindoc.ui.common.BasePanel;
import com.mindoc.ui.theme.MindDocTheme;
import com.mindoc.util.I18n;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DashboardPanel extends BasePanel {
    private static final Logger logger = LoggerFactory.getLogger(DashboardPanel.class);

    private final int currentUserId;
    private final MoodEntryRepository moodEntryRepository;
    private String currentUsername = "User";

    private Label welcomeTitle;
    private Label welcomeSubtitle;

    private Label todayMoodEmoji;
    private Label todayMoodLevel;
    private Label todayMoodNote;

    private Label weekMoodAverage;
    private Label weekMoodAverageLabel;
    private Label streakCount;
    private Label streakLabel;
    private Label entriesCount;
    private Label entriesLabel;

    private Label tip1;
    private Label tip2;
    private Label tip3;

    public DashboardPanel(int currentUserId, DatabaseManager databaseManager) {
        this.currentUserId = currentUserId;
        this.moodEntryRepository = new MoodEntryRepository(databaseManager.getConnection());
        setStyle("-fx-background-color: " + MindDocTheme.BACKGROUND + ";");
        setPadding(new Insets(28));
        setSpacing(20);
        initializeUI();
        refresh();
    }

    private void initializeUI() {
        getChildren().add(createWelcomeSection());
        getChildren().add(createMainGrid());
        getChildren().add(createTipsSection());

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        getChildren().add(spacer);

        applyLanguage();
    }

    // ── Welcome ───────────────────────────────────────────────────────────────

    private HBox createWelcomeSection() {
        HBox section = new HBox();
        section.setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 100% 0%, " +
                        MindDocTheme.PRIMARY + ", " + MindDocTheme.SECONDARY + "); " +
                        "-fx-background-radius: 16; " +
                        "-fx-padding: 28 36; " +
                        "-fx-effect: dropshadow(three-pass-box, #00000020, 10, 0, 0, 4);"
        );
        section.setAlignment(Pos.CENTER_LEFT);

        VBox textBox = new VBox(5);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        welcomeTitle = new Label("Welcome back!");
        welcomeTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        welcomeTitle.setTextFill(Color.WHITE);

        welcomeSubtitle = new Label("Let's check in on your mental health today");
        welcomeSubtitle.setFont(Font.font("Segoe UI", 14));
        welcomeSubtitle.setTextFill(Color.web("#d1fae5"));

        textBox.getChildren().addAll(welcomeTitle, welcomeSubtitle);

        Label deco = new Label("🌿");
        deco.setFont(Font.font("System", 52));
        deco.setOpacity(0.7);

        section.getChildren().addAll(textBox, deco);
        return section;
    }

    // ── Main grid: big mood card left + 2 small cards right ──────────────────

    private HBox createMainGrid() {
        HBox row = new HBox(16);
        row.setPrefWidth(Double.MAX_VALUE);

        // Big Today's Mood card
        VBox moodCard = createBigMoodCard();
        HBox.setHgrow(moodCard, Priority.ALWAYS);

        // Right column: Weekly Avg + Streak stacked, + Entries
        VBox rightCol = new VBox(16);
        HBox.setHgrow(rightCol, Priority.ALWAYS);

        // Top pair: weekly avg + streak side by side
        HBox topPair = new HBox(16);
        VBox avgCard    = createSmallCard("📊 Weekly Average", weekMoodAverage  = numLabel("--",  MindDocTheme.INFO),    weekMoodAverageLabel = subLabel("out of 10"));
        VBox streakCard = createSmallCard("🔥 Streak",         streakCount      = numLabel("0",   MindDocTheme.WARNING), streakLabel          = subLabel("days"));
        HBox.setHgrow(avgCard,    Priority.ALWAYS);
        HBox.setHgrow(streakCard, Priority.ALWAYS);
        topPair.getChildren().addAll(avgCard, streakCard);

        // Bottom: entries full width
        VBox entriesCard = createSmallCard("📝 Total Entries",  entriesCount = numLabel("0", MindDocTheme.SUCCESS), entriesLabel = subLabel("tracked"));

        rightCol.getChildren().addAll(topPair, entriesCard);
        VBox.setVgrow(entriesCard, Priority.ALWAYS);

        row.getChildren().addAll(moodCard, rightCol);
        return row;
    }

    private VBox createBigMoodCard() {
        VBox card = new VBox(10);
        card.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 16; " +
                        "-fx-border-radius: 16; " +
                        "-fx-padding: 24; " +
                        "-fx-border-color: " + MindDocTheme.PRIMARY + "; " +
                        "-fx-border-width: 0 0 0 5; " +
                        "-fx-effect: dropshadow(three-pass-box, #00000014, 10, 0, 0, 3);"
        );
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMinWidth(220);

        Label headerLabel = new Label("🎯 Today's Mood");
        headerLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        headerLabel.setTextFill(Color.web(MindDocTheme.TEXT_SECONDARY));

        todayMoodEmoji = new Label("😐");
        todayMoodEmoji.setFont(Font.font("System", 56));

        todayMoodLevel = new Label("No entry yet");
        todayMoodLevel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        todayMoodLevel.setTextFill(Color.web(MindDocTheme.TEXT_PRIMARY));

        todayMoodNote = new Label("");
        todayMoodNote.setFont(Font.font("Segoe UI", 12));
        todayMoodNote.setTextFill(Color.web(MindDocTheme.TEXT_SECONDARY));
        todayMoodNote.setWrapText(true);

        card.getChildren().addAll(headerLabel, todayMoodEmoji, todayMoodLevel, todayMoodNote);
        return card;
    }

    private VBox createSmallCard(String title, Label num, Label sub) {
        VBox card = new VBox(4);
        card.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 14; " +
                        "-fx-border-radius: 14; " +
                        "-fx-padding: 16 18; " +
                        "-fx-effect: dropshadow(three-pass-box, #00000014, 8, 0, 0, 2);"
        );

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        titleLabel.setTextFill(Color.web(MindDocTheme.TEXT_SECONDARY));

        card.getChildren().addAll(titleLabel, num, sub);
        return card;
    }

    // ── Tips: 3 cards in a row ────────────────────────────────────────────────

    private HBox createTipsSection() {
        HBox row = new HBox(16);
        row.setPrefWidth(Double.MAX_VALUE);

        tip1 = new Label();
        tip2 = new Label();
        tip3 = new Label();

        VBox card1 = createTipCard("ti-chart-line", MindDocTheme.PRIMARY, tip1);
        VBox card2 = createTipCard("ti-brain",      MindDocTheme.INFO,    tip2);
        VBox card3 = createTipCard("ti-leaf",       MindDocTheme.SUCCESS, tip3);

        HBox.setHgrow(card1, Priority.ALWAYS);
        HBox.setHgrow(card2, Priority.ALWAYS);
        HBox.setHgrow(card3, Priority.ALWAYS);

        row.getChildren().addAll(card1, card2, card3);
        return row;
    }

    private VBox createTipCard(String iconHint, String accentColor, Label tipLabel) {
        // Top accent bar
        Region bar = new Region();
        bar.setPrefHeight(4);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setStyle("-fx-background-color: " + accentColor + "; -fx-background-radius: 14 14 0 0;");

        // Icon circle
        Label iconCircle = new Label(iconToEmoji(iconHint));
        iconCircle.setStyle(
                "-fx-background-color: " + accentColor + "22; " +
                        "-fx-background-radius: 20; " +
                        "-fx-padding: 8; " +
                        "-fx-font-size: 18px;"
        );

        tipLabel.setFont(Font.font("Segoe UI", 13));
        tipLabel.setTextFill(Color.web(MindDocTheme.TEXT_PRIMARY));
        tipLabel.setWrapText(true);

        VBox inner = new VBox(10);
        inner.setPadding(new Insets(14, 16, 18, 16));
        inner.getChildren().addAll(iconCircle, tipLabel);

        VBox card = new VBox(0);
        card.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 14; " +
                        "-fx-border-radius: 14; " +
                        "-fx-effect: dropshadow(three-pass-box, #00000014, 8, 0, 0, 2);"
        );
        card.getChildren().addAll(bar, inner);
        return card;
    }

    /** Maps icon hint to a fitting emoji since JavaFX has no icon font */
    private String iconToEmoji(String hint) {
        return switch (hint) {
            case "ti-chart-line" -> "📈";
            case "ti-brain"      -> "🧠";
            case "ti-leaf"       -> "🧘";
            default              -> "💡";
        };
    }

    // ── Label helpers ─────────────────────────────────────────────────────────

    private Label numLabel(String text, String color) {
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", FontWeight.BOLD, 30));
        l.setTextFill(Color.web(color));
        return l;
    }

    private Label subLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", 12));
        l.setTextFill(Color.web(MindDocTheme.TEXT_SECONDARY));
        return l;
    }

    // ── Data ──────────────────────────────────────────────────────────────────

    @Override
    public void refresh() {
        try {
            MoodEntry today = moodEntryRepository.findByUserIdAndDate(currentUserId, LocalDate.now());
            if (today == null) {
                todayMoodEmoji.setText("😐");
                todayMoodLevel.setText(I18n.t("no_entry_yet", "No entry yet"));
                todayMoodNote.setText("");
            } else {
                todayMoodEmoji.setText(today.getMoodEmoji());
                todayMoodLevel.setText(today.getMoodLevel() + "/10");
                String note = today.getNote() == null ? "" : today.getNote().trim();
                todayMoodNote.setText(note.isEmpty() ? "Keep tracking for better insights" : note);
            }

            LocalDate end   = LocalDate.now();
            LocalDate start = end.minusDays(6);
            double avg = moodEntryRepository.getAverageMoodForUser(currentUserId, start, end);
            weekMoodAverage.setText(avg > 0.0 ? String.format("%.1f", avg) : "--");

            List<MoodEntry> entries = moodEntryRepository.findByUserId(currentUserId);
            streakCount.setText(String.valueOf(calculateStreak(entries)));
            entriesCount.setText(String.valueOf(entries.size()));

            applyLanguage();
        } catch (Exception e) {
            logger.error("Error refreshing dashboard", e);
        }
    }

    public void setCurrentUsername(String username) {
        this.currentUsername = username;
        applyLanguage();
    }

    public void applyLanguage() {
        welcomeTitle.setText(I18n.t("welcome_back", "Welcome back, " + currentUsername + "!"));
        welcomeSubtitle.setText(I18n.t("lets_check_in", "Let's check in on your mental health today"));
        if (tip1 != null) tip1.setText(I18n.t("tip_1", "Track your mood regularly to identify patterns and triggers"));
        if (tip2 != null) tip2.setText(I18n.t("tip_2", "Try a new course or exercise today to improve your mental wellbeing"));
        if (tip3 != null) tip3.setText(I18n.t("tip_3", "Practice daily mindfulness for better emotional balance"));
    }

    private int calculateStreak(List<MoodEntry> entries) {
        if (entries == null || entries.isEmpty()) return 0;
        Set<LocalDate> dates = new HashSet<>();
        for (MoodEntry e : entries) dates.add(e.getEntryDate());
        int streak = 0;
        LocalDate cursor = LocalDate.now();
        while (dates.contains(cursor)) { streak++; cursor = cursor.minusDays(1); }
        return streak;
    }
}
