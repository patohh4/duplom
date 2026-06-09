package com.mindoc.ui.analytics;

import com.mindoc.database.DatabaseManager;
import com.mindoc.model.MoodEntry;
import com.mindoc.repository.MoodEntryRepository;
import com.mindoc.ui.common.BasePanel;
import com.mindoc.ui.theme.MindDocTheme;
import com.mindoc.util.I18n;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class AnalyticsPanel extends BasePanel {
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsPanel.class);

    private int currentUserId;
    private DatabaseManager databaseManager;
    private MoodEntryRepository moodEntryRepository;

    private Canvas moodChartCanvas;
    private Canvas statsCanvas;
    private Label averageMoodLabel;
    private Label highestMoodLabel;
    private Label lowestMoodLabel;
    private Label entriesCountLabel;
    private Label titleLabel;

    // sparkline mini-bars for each stat card
    private ProgressBar avgBar, bestBar, toughBar, totalBar;

    public AnalyticsPanel(int userId, DatabaseManager databaseManager) {
        this.currentUserId = userId;
        this.databaseManager = databaseManager;
        this.moodEntryRepository = new MoodEntryRepository(databaseManager.getConnection());
        initializeUI();
        refresh();
    }

    // ── Layout ────────────────────────────────────────────────────────────────

    private void initializeUI() {
        // Override BasePanel defaults — flush banner
        setStyle("-fx-background-color: " + MindDocTheme.BACKGROUND + "; -fx-padding: 0;");
        setPadding(new Insets(0));
        setSpacing(0);
        setFillWidth(true);

        getChildren().add(buildHeaderBanner());

        titleLabel = new Label("📊 Analytics & Statistics");
        titleLabel.setVisible(false);
        titleLabel.setManaged(false);
        getChildren().add(titleLabel);

        // Body with padding
        VBox body = new VBox(20);
        body.setPadding(new Insets(24));
        VBox.setVgrow(body, Priority.ALWAYS);

        body.getChildren().add(buildStatCards());

        VBox charts = buildChartsSection();
        VBox.setVgrow(charts, Priority.ALWAYS);
        body.getChildren().add(charts);

        ScrollPane sp = new ScrollPane(body);
        sp.setFitToWidth(true);
        sp.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-background: transparent; " +
            "-fx-padding: 0;"
        );
        VBox.setVgrow(sp, Priority.ALWAYS);
        getChildren().add(sp);
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

        Label h1 = new Label("📊 Analytics & Statistics");
        h1.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        h1.setTextFill(Color.WHITE);

        Label h2 = new Label("Insights from your mood and wellness data");
        h2.setFont(Font.font("Segoe UI", 13));
        h2.setTextFill(Color.web("#d1fae5"));

        text.getChildren().addAll(h1, h2);

        Label deco = new Label("📈");
        deco.setFont(Font.font("System", 56));
        deco.setOpacity(0.22);

        banner.getChildren().addAll(text, deco);
        return banner;
    }

    // ── Stat cards row ────────────────────────────────────────────────────────

    private HBox buildStatCards() {
        HBox row = new HBox(16);
        row.setFillHeight(true);

        Label[] valueLabels = new Label[4];
        ProgressBar[] bars = new ProgressBar[4];

        VBox c1 = buildStatCard("😊", "Average Mood",   MindDocTheme.PRIMARY,  valueLabels, bars, 0);
        VBox c2 = buildStatCard("🏆", "Best Mood",      MindDocTheme.SUCCESS,  valueLabels, bars, 1);
        VBox c3 = buildStatCard("📉", "Toughest Day",   MindDocTheme.WARNING,  valueLabels, bars, 2);
        VBox c4 = buildStatCard("📝", "Total Entries",  MindDocTheme.INFO,     valueLabels, bars, 3);

        averageMoodLabel  = valueLabels[0];
        highestMoodLabel  = valueLabels[1];
        lowestMoodLabel   = valueLabels[2];
        entriesCountLabel = valueLabels[3];
        avgBar    = bars[0];
        bestBar   = bars[1];
        toughBar  = bars[2];
        totalBar  = bars[3];

        for (VBox c : new VBox[]{c1, c2, c3, c4}) {
            HBox.setHgrow(c, Priority.ALWAYS);
        }
        row.getChildren().addAll(c1, c2, c3, c4);
        return row;
    }

    private VBox buildStatCard(String emoji, String title, String color,
                               Label[] out, ProgressBar[] bars, int idx) {
        // Top accent bar
        Region bar = new Region();
        bar.setPrefHeight(4);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 14 14 0 0;");

        VBox inner = new VBox(10);
        inner.setPadding(new Insets(16, 20, 20, 20));

        // Title row: emoji circle + label
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        StackPane iconCircle = new StackPane();
        iconCircle.setStyle(
            "-fx-background-color: " + color + "22; " +
            "-fx-background-radius: 10; " +
            "-fx-min-width: 36; -fx-min-height: 36; " +
            "-fx-max-width: 36; -fx-max-height: 36;"
        );
        Label emojiLbl = new Label(emoji);
        emojiLbl.setFont(Font.font("System", 16));
        iconCircle.getChildren().add(emojiLbl);

        Label titleLbl = new Label(title);
        titleLbl.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        titleLbl.setTextFill(Color.web(MindDocTheme.TEXT_SECONDARY));
        titleRow.getChildren().addAll(iconCircle, titleLbl);

        // Big number
        Label valueLbl = new Label("--");
        valueLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 36));
        valueLbl.setTextFill(Color.web(color));
        out[idx] = valueLbl;

        // Progress bar (visual fill from 0-10 for mood, or count-based)
        ProgressBar pb = new ProgressBar(0);
        pb.setMaxWidth(Double.MAX_VALUE);
        pb.setPrefHeight(5);
        pb.setStyle(
            "-fx-accent: " + color + "; " +
            "-fx-background-color: " + color + "22; " +
            "-fx-background-radius: 3; " +
            "-fx-padding: 0;"
        );
        bars[idx] = pb;

        inner.getChildren().addAll(titleRow, valueLbl, pb);

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

    // ── Charts section ────────────────────────────────────────────────────────

    private VBox buildChartsSection() {
        VBox section = new VBox(0);
        section.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 16; " +
            "-fx-border-radius: 16; " +
            "-fx-effect: dropshadow(three-pass-box, #00000014, 10, 0, 0, 3);"
        );

        // Accent bar
        Region accentBar = new Region();
        accentBar.setPrefHeight(4);
        accentBar.setMaxWidth(Double.MAX_VALUE);
        accentBar.setStyle(
            "-fx-background-color: linear-gradient(from 0% 0% to 100% 0%, " +
                MindDocTheme.PRIMARY + ", " + MindDocTheme.SECONDARY + "); " +
            "-fx-background-radius: 16 16 0 0;"
        );

        VBox inner = new VBox(24);
        inner.setPadding(new Insets(22, 24, 24, 24));

        // ── Trend chart ────────────────────────────────────────────────────
        HBox trendHeader = new HBox(8);
        trendHeader.setAlignment(Pos.CENTER_LEFT);
        Region trendDot = new Region();
        trendDot.setPrefWidth(4); trendDot.setPrefHeight(16);
        trendDot.setStyle("-fx-background-color: " + MindDocTheme.PRIMARY + "; -fx-background-radius: 2;");
        Label trendTitle = new Label("Mood Trend — Last 30 Days");
        trendTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        trendTitle.setTextFill(Color.web(MindDocTheme.TEXT_PRIMARY));
        trendHeader.getChildren().addAll(trendDot, trendTitle);

        moodChartCanvas = new Canvas(760, 240);
        VBox trendBox = new VBox(12, trendHeader, moodChartCanvas);

        // ── Weekly bar chart ───────────────────────────────────────────────
        HBox weekHeader = new HBox(8);
        weekHeader.setAlignment(Pos.CENTER_LEFT);
        Region weekDot = new Region();
        weekDot.setPrefWidth(4); weekDot.setPrefHeight(16);
        weekDot.setStyle("-fx-background-color: " + MindDocTheme.INFO + "; -fx-background-radius: 2;");
        Label weekTitle = new Label("Weekly Mood Summary");
        weekTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        weekTitle.setTextFill(Color.web(MindDocTheme.TEXT_PRIMARY));
        weekHeader.getChildren().addAll(weekDot, weekTitle);

        statsCanvas = new Canvas(760, 180);
        VBox weekBox = new VBox(12, weekHeader, statsCanvas);

        inner.getChildren().addAll(trendBox, new Separator(), weekBox);
        section.getChildren().addAll(accentBar, inner);

        // make canvases resize with the panel
        section.widthProperty().addListener((obs, ov, nv) -> {
            double w = nv.doubleValue() - 48;
            if (w > 0) {
                moodChartCanvas.setWidth(w);
                statsCanvas.setWidth(w);
                drawMoodChart();
                drawWeeklyStats();
            }
        });

        return section;
    }

    // helper separator
    private static class Separator extends Region {
        Separator() {
            setPrefHeight(1);
            setMaxWidth(Double.MAX_VALUE);
            setStyle("-fx-background-color: #f1f5f9;");
        }
    }

    // ── Chart drawing ─────────────────────────────────────────────────────────

    private void drawMoodChart() {
        try {
            List<MoodEntry> entries = moodEntryRepository.findByUserId(currentUserId);
            GraphicsContext gc = moodChartCanvas.getGraphicsContext2D();
            double w = moodChartCanvas.getWidth();
            double h = moodChartCanvas.getHeight();
            double padL = 44, padB = 36, padT = 14, padR = 16;

            gc.clearRect(0, 0, w, h);
            gc.setFill(Color.WHITE);
            gc.fillRect(0, 0, w, h);

            if (entries.isEmpty()) {
                gc.setFont(Font.font("Segoe UI", 14));
                gc.setFill(Color.web(MindDocTheme.TEXT_MUTED));
                gc.fillText("No mood data yet — start tracking your mood!", padL + 20, h / 2);
                return;
            }

            double chartW = w - padL - padR;
            double chartH = h - padT - padB;

            // Grid lines + Y labels
            gc.setFont(Font.font("Segoe UI", 10));
            for (int i = 0; i <= 10; i += 2) {
                double y = h - padB - (i / 10.0 * chartH);
                gc.setStroke(i == 0 ? Color.web("#d1d5db") : Color.web("#f3f4f6"));
                gc.setLineWidth(i == 0 ? 1.5 : 1);
                gc.strokeLine(padL, y, w - padR, y);
                gc.setFill(Color.web(MindDocTheme.TEXT_MUTED));
                gc.fillText(String.valueOf(i), padL - 22, y + 4);
            }

            // Gradient fill under the line
            double ptSpacing = entries.size() > 1 ? chartW / (entries.size() - 1) : chartW;
            double[] xs = new double[entries.size()];
            double[] ys = new double[entries.size()];
            for (int i = 0; i < entries.size(); i++) {
                xs[i] = padL + i * ptSpacing;
                ys[i] = h - padB - (entries.get(i).getMoodLevel() / 10.0 * chartH);
            }

            // Fill polygon
            gc.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web(MindDocTheme.PRIMARY + "44")),
                new Stop(1, Color.web(MindDocTheme.PRIMARY + "05"))));
            double[] fillX = new double[entries.size() + 2];
            double[] fillY = new double[entries.size() + 2];
            System.arraycopy(xs, 0, fillX, 0, entries.size());
            fillX[entries.size()]     = xs[entries.size() - 1];
            fillX[entries.size() + 1] = xs[0];
            System.arraycopy(ys, 0, fillY, 0, entries.size());
            fillY[entries.size()]     = h - padB;
            fillY[entries.size() + 1] = h - padB;
            gc.fillPolygon(fillX, fillY, fillX.length);

            // Line
            gc.setStroke(Color.web(MindDocTheme.PRIMARY));
            gc.setLineWidth(2.5);
            gc.beginPath();
            gc.moveTo(xs[0], ys[0]);
            for (int i = 1; i < entries.size(); i++) gc.lineTo(xs[i], ys[i]);
            gc.stroke();

            // Data points + date labels
            for (int i = 0; i < entries.size(); i++) {
                // white halo
                gc.setFill(Color.WHITE);
                gc.fillOval(xs[i] - 5, ys[i] - 5, 10, 10);
                gc.setFill(Color.web(MindDocTheme.PRIMARY));
                gc.fillOval(xs[i] - 4, ys[i] - 4, 8, 8);

                // date label (every nth)
                int step = Math.max(1, entries.size() / 8);
                if (i % step == 0 || i == entries.size() - 1) {
                    String dateStr = entries.get(i).getEntryDate().toString();
                    gc.setFill(Color.web(MindDocTheme.TEXT_MUTED));
                    gc.setFont(Font.font("Segoe UI", 9));
                    gc.fillText(dateStr.substring(5), xs[i] - 10, h - padB + 14);
                }
            }

        } catch (SQLException e) {
            logger.error("Error drawing mood chart", e);
        }
    }

    private void drawWeeklyStats() {
        try {
            List<MoodEntry> entries = moodEntryRepository.findByUserId(currentUserId);
            GraphicsContext gc = statsCanvas.getGraphicsContext2D();
            double w = statsCanvas.getWidth();
            double h = statsCanvas.getHeight();
            double padL = 20, padB = 36, padT = 10;

            gc.clearRect(0, 0, w, h);
            gc.setFill(Color.WHITE);
            gc.fillRect(0, 0, w, h);

            String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
            Map<String, List<Double>> dailyValues = new LinkedHashMap<>();
            for (String d : days) dailyValues.put(d, new ArrayList<>());

            for (MoodEntry e : entries) {
                try {
                    LocalDate ld = e.getEntryDate();
                    String dayKey = days[ld.getDayOfWeek().getValue() - 1];
                    dailyValues.get(dayKey).add((double) e.getMoodLevel());
                } catch (Exception ex) {
                    logger.warn("Skipping entry: {}", ex.getMessage());
                }
            }

            double chartH = h - padT - padB;
            double barW   = (w - padL) / 7.0;

            int idx = 0;
            for (Map.Entry<String, List<Double>> de : dailyValues.entrySet()) {
                double avg = de.getValue().isEmpty() ? 0
                    : de.getValue().stream().mapToDouble(d -> d).average().orElse(0);
                double barH = avg / 10.0 * chartH;
                double x    = padL + idx * barW;
                double y    = h - padB - barH;

                if (avg > 0) {
                    // Colored gradient bar
                    String barColor = avg >= 7 ? MindDocTheme.SUCCESS
                        : avg >= 4 ? MindDocTheme.PRIMARY
                        : MindDocTheme.WARNING;

                    gc.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web(barColor)),
                        new Stop(1, Color.web(barColor + "88"))));
                    double bw = barW * 0.6;
                    double bx = x + (barW - bw) / 2;
                    // Rounded top (simulate with arc)
                    gc.fillRoundRect(bx, y, bw, barH, 6, 6);

                    // Value on top
                    gc.setFill(Color.web(barColor));
                    gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
                    String val = String.format(Locale.US, "%.1f", avg);
                    gc.fillText(val, bx + bw / 2 - 8, y - 4);
                } else {
                    // Empty bar placeholder
                    gc.setFill(Color.web("#f3f4f6"));
                    double bw = barW * 0.6;
                    double bx = x + (barW - bw) / 2;
                    gc.fillRoundRect(bx, h - padB - 4, bw, 4, 3, 3);
                }

                // Day label
                gc.setFill(Color.web(MindDocTheme.TEXT_SECONDARY));
                gc.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
                gc.fillText(de.getKey(), x + barW / 2 - 10, h - padB + 16);

                idx++;
            }

        } catch (SQLException e) {
            logger.error("Error drawing weekly stats", e);
        }
    }

    // ── Data ─────────────────────────────────────────────────────────────────

    @Override
    public void refresh() {
        try {
            List<MoodEntry> entries = moodEntryRepository.findByUserId(currentUserId);

            if (entries.isEmpty()) {
                averageMoodLabel.setText("N/A");
                highestMoodLabel.setText("N/A");
                lowestMoodLabel.setText("N/A");
                entriesCountLabel.setText("0");
                if (avgBar  != null) avgBar.setProgress(0);
                if (bestBar != null) bestBar.setProgress(0);
                if (toughBar!= null) toughBar.setProgress(0);
                if (totalBar!= null) totalBar.setProgress(0);
                drawMoodChart();
                drawWeeklyStats();
                return;
            }

            double average = entries.stream().mapToInt(MoodEntry::getMoodLevel).average().orElse(0);
            int    highest = entries.stream().mapToInt(MoodEntry::getMoodLevel).max().orElse(0);
            int    lowest  = entries.stream().mapToInt(MoodEntry::getMoodLevel).min().orElse(0);
            int    total   = entries.size();

            // Use Locale.US so decimal separator is always "." not ","
            averageMoodLabel.setText(String.format(Locale.US, "%.1f", average));
            highestMoodLabel.setText(String.valueOf(highest));
            lowestMoodLabel.setText(String.valueOf(lowest));
            entriesCountLabel.setText(String.valueOf(total));

            // Progress bars (0-10 scale for mood, capped at 50 for entries)
            if (avgBar  != null) avgBar.setProgress(average / 10.0);
            if (bestBar != null) bestBar.setProgress(highest / 10.0);
            if (toughBar!= null) toughBar.setProgress(lowest / 10.0);
            if (totalBar!= null) totalBar.setProgress(Math.min(total / 50.0, 1.0));

            drawMoodChart();
            drawWeeklyStats();

        } catch (SQLException e) {
            logger.error("Error refreshing analytics", e);
        }
    }

    public void applyLanguage(String language) {
        if (titleLabel != null)
            titleLabel.setText(I18n.t("analytics", "📊 Analytics & Statistics"));
    }
}
