package com.mindoc.ui.settings;

import com.mindoc.model.AppSettings;
import com.mindoc.repository.AppSettingsRepository;
import com.mindoc.ui.common.BasePanel;
import com.mindoc.ui.theme.MindDocTheme;
import com.mindoc.util.I18n;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.function.Consumer;

public class SettingsPanel extends BasePanel {
    private static final Logger logger = LoggerFactory.getLogger(SettingsPanel.class);

    private final int currentUserId;
    private final AppSettingsRepository settingsRepository;
    private final Consumer<AppSettings> onSettingsApplied;

    // Controls
    private Button langEnBtn, langUaBtn;
    private Slider textSizeSlider;
    private Label textPreviewLabel;
    private CheckBox notificationsCheck;
    private Button applyButton, resetButton;
    private Label statusLabel;

    // i18n labels
    private Label titleLabel;
    private Label appearanceTitle, prefsTitle, aboutTitle;
    private Label langRowLabel, sizeRowLabel, notiRowLabel;
    private Label langRowDesc, sizeRowDesc, notiRowDesc;

    public SettingsPanel(int currentUserId, AppSettingsRepository settingsRepository,
                         Consumer<AppSettings> onSettingsApplied) {
        this.currentUserId      = currentUserId;
        this.settingsRepository = settingsRepository;
        this.onSettingsApplied  = onSettingsApplied;
        initializeUI();
        refresh();
    }

    // ── Layout ────────────────────────────────────────────────────────────────

    private void initializeUI() {
        setStyle("-fx-background-color: " + MindDocTheme.BACKGROUND + "; -fx-padding: 0; -fx-spacing: 0;");
        setPadding(new Insets(0));
        setSpacing(0);
        setFillWidth(true);

        getChildren().add(buildHeaderBanner());

        titleLabel = new Label("⚙️ Settings");
        titleLabel.setVisible(false);
        titleLabel.setManaged(false);
        getChildren().add(titleLabel);

        // Scrollable body
        VBox body = new VBox(20);
        body.setPadding(new Insets(28, 36, 36, 36));
        body.setMaxWidth(Double.MAX_VALUE);

        VBox centeredContent = new VBox(20);
        centeredContent.setMaxWidth(800);
        centeredContent.setStyle("-fx-alignment: center;");

        centeredContent.getChildren().addAll(
            buildAppearanceCard(),
            buildPreferencesCard(),
            buildActionRow(),
            buildAboutCard()
        );

        body.getChildren().add(centeredContent);

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

        Label h1 = new Label("⚙️ Settings");
        h1.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        h1.setTextFill(Color.WHITE);

        Label h2 = new Label("Customize your MindDoc experience");
        h2.setFont(Font.font("Segoe UI", 13));
        h2.setTextFill(Color.web("#d1fae5"));

        text.getChildren().addAll(h1, h2);

        Label deco = new Label("🎛");
        deco.setFont(Font.font("System", 56));
        deco.setOpacity(0.22);

        banner.getChildren().addAll(text, deco);
        return banner;
    }

    // ── Appearance card ───────────────────────────────────────────────────────

    private VBox buildAppearanceCard() {
        Region bar = accentBar(MindDocTheme.PRIMARY);

        VBox inner = new VBox(0);
        inner.setPadding(new Insets(20, 24, 8, 24));

        appearanceTitle = sectionTitle("🎨  Appearance");
        inner.getChildren().add(appearanceTitle);
        inner.getChildren().add(divider());

        // ── Language row ────────────────────────────────────────────────────
        inner.getChildren().add(buildSettingRow(
            "🌐", "Language", "Choose the display language for the app",
            buildLanguageToggle()
        ));
        inner.getChildren().add(lightDivider());

        // ── Text size row ───────────────────────────────────────────────────
        inner.getChildren().add(buildSettingRow(
            "🔤", "Text Size", "Adjust the font size across the app",
            buildTextSizeControl()
        ));

        VBox card = card();
        card.getChildren().addAll(bar, inner);
        return card;
    }

    private HBox buildLanguageToggle() {
        langEnBtn = toggleBtn("🇬🇧  English");
        langUaBtn = toggleBtn("🇺🇦  Українська");

        langEnBtn.setOnAction(e -> selectLanguage("English"));
        langUaBtn.setOnAction(e -> selectLanguage("Українська"));
        selectLanguage("English"); // default active

        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().addAll(langEnBtn, langUaBtn);
        return row;
    }

    private void selectLanguage(String lang) {
        boolean en = "English".equals(lang);
        langEnBtn.setStyle(en ? toggleActiveStyle() : toggleInactiveStyle());
        langUaBtn.setStyle(en ? toggleInactiveStyle() : toggleActiveStyle());
    }

    private String currentSelectedLanguage() {
        return langUaBtn.getStyle().equals(toggleActiveStyle()) ? "Українська" : "English";
    }

    private HBox buildTextSizeControl() {
        textSizeSlider = new Slider(85, 130, 100);
        textSizeSlider.setShowTickLabels(true);
        textSizeSlider.setShowTickMarks(false);
        textSizeSlider.setMajorTickUnit(15);
        textSizeSlider.setMinorTickCount(0);
        textSizeSlider.setBlockIncrement(5);
        textSizeSlider.setPrefWidth(200);
        HBox.setHgrow(textSizeSlider, Priority.ALWAYS);

        textPreviewLabel = new Label("Aa");
        textPreviewLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        textPreviewLabel.setTextFill(Color.web(MindDocTheme.PRIMARY));
        textPreviewLabel.setMinWidth(48);
        textPreviewLabel.setAlignment(Pos.CENTER);
        textPreviewLabel.setStyle(
            "-fx-background-color: " + MindDocTheme.PRIMARY + "15; " +
            "-fx-background-radius: 10; " +
            "-fx-padding: 6 10;"
        );

        textSizeSlider.valueProperty().addListener((obs, ov, nv) -> {
            int sz = (int) Math.round(nv.doubleValue());
            textPreviewLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10 + (sz - 85) / 4.0));
        });

        Label sizeVal = new Label("100%");
        sizeVal.setFont(Font.font("Segoe UI", 11));
        sizeVal.setTextFill(Color.web(MindDocTheme.TEXT_MUTED));
        sizeVal.setMinWidth(36);
        textSizeSlider.valueProperty().addListener((obs, ov, nv) ->
            sizeVal.setText((int) Math.round(nv.doubleValue()) + "%"));

        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().addAll(textSizeSlider, sizeVal, textPreviewLabel);
        HBox.setHgrow(textSizeSlider, Priority.ALWAYS);
        return row;
    }

    // ── Preferences card ──────────────────────────────────────────────────────

    private VBox buildPreferencesCard() {
        Region bar = accentBar(MindDocTheme.INFO);

        VBox inner = new VBox(0);
        inner.setPadding(new Insets(20, 24, 8, 24));

        prefsTitle = sectionTitle("🔔  Preferences");
        inner.getChildren().add(prefsTitle);
        inner.getChildren().add(divider());

        // Notifications row
        notificationsCheck = new CheckBox();
        notificationsCheck.setSelected(true);
        notificationsCheck.setStyle("-fx-cursor: hand;");

        // Custom styled toggle look
        Label notiToggle = new Label("Notifications");
        notiToggle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        notiToggle.setTextFill(Color.web(MindDocTheme.PRIMARY));

        HBox notiControl = new HBox(10);
        notiControl.setAlignment(Pos.CENTER_LEFT);
        notiControl.getChildren().addAll(notificationsCheck,
            new Label("Receive in-app reminders and updates") {{
                setFont(Font.font("Segoe UI", 12));
                setTextFill(Color.web(MindDocTheme.TEXT_SECONDARY));
            }});

        inner.getChildren().add(buildSettingRow(
            "🔔", "Notifications", "Receive reminders and wellness tips",
            notiControl
        ));

        VBox card = card();
        card.getChildren().addAll(bar, inner);
        return card;
    }

    // ── Action row ────────────────────────────────────────────────────────────

    private VBox buildActionRow() {
        applyButton = new Button("💾  Save Settings");
        applyButton.setPrefWidth(180);
        applyButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        applyButton.setCursor(Cursor.HAND);
        applyButton.setStyle(
            "-fx-background-color: " + MindDocTheme.PRIMARY + "; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 12; " +
            "-fx-padding: 13 0; " +
            "-fx-effect: dropshadow(three-pass-box, #00000033, 6, 0, 0, 3);"
        );

        resetButton = new Button("↺  Reset to Defaults");
        resetButton.setPrefWidth(180);
        resetButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        resetButton.setCursor(Cursor.HAND);
        resetButton.setStyle(
            "-fx-background-color: #f1f5f9; " +
            "-fx-text-fill: #374151; " +
            "-fx-background-radius: 12; " +
            "-fx-padding: 13 0; " +
            "-fx-border-color: #e5e7eb; " +
            "-fx-border-width: 1.5; " +
            "-fx-border-radius: 12;"
        );

        statusLabel = new Label();
        statusLabel.setFont(Font.font("Segoe UI", 12));
        statusLabel.setVisible(false);

        applyButton.setOnAction(e -> saveSettings());
        resetButton.setOnAction(e -> resetToDefaults());

        HBox btnRow = new HBox(12);
        btnRow.setAlignment(Pos.CENTER_LEFT);
        btnRow.getChildren().addAll(applyButton, resetButton, statusLabel);

        VBox wrapper = new VBox(btnRow);
        return wrapper;
    }

    // ── About card ────────────────────────────────────────────────────────────

    private VBox buildAboutCard() {
        Region bar = accentBar("#6b7280");

        VBox inner = new VBox(12);
        inner.setPadding(new Insets(20, 24, 22, 24));

        aboutTitle = sectionTitle("ℹ️  About MindDoc");
        inner.getChildren().add(aboutTitle);
        inner.getChildren().add(divider());

        // Two-column info grid
        VBox info = new VBox(10);

        HBox row1 = aboutRow("📱 Version",     "2.0.0");
        HBox row2 = aboutRow("👨‍💻 Developer",   "MindDoc Team");
        HBox row3 = aboutRow("📅 Build date",   "2026");
        HBox row4 = aboutRow("🧠 Description",  "Mental Health Support Desktop Application");

        info.getChildren().addAll(row1, row2, row3, row4);

        // Mini motto
        Label motto = new Label("\"Your mental wellbeing matters — one day at a time.\"");
        motto.setFont(Font.font("Segoe UI", 12));
        motto.setTextFill(Color.web(MindDocTheme.TEXT_MUTED));
        motto.setStyle(
            "-fx-background-color: " + MindDocTheme.PRIMARY + "0d; " +
            "-fx-padding: 10 14; " +
            "-fx-background-radius: 10; " +
            "-fx-border-color: " + MindDocTheme.PRIMARY + "33; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 10;"
        );

        inner.getChildren().addAll(info, motto);

        VBox card = card();
        card.getChildren().addAll(bar, inner);
        return card;
    }

    private HBox aboutRow(String key, String value) {
        Label k = new Label(key);
        k.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        k.setTextFill(Color.web(MindDocTheme.TEXT_SECONDARY));
        k.setMinWidth(140);

        Label v = new Label(value);
        v.setFont(Font.font("Segoe UI", 12));
        v.setTextFill(Color.web(MindDocTheme.TEXT_PRIMARY));

        HBox row = new HBox(12, k, v);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    // ── Generic setting row ───────────────────────────────────────────────────

    private HBox buildSettingRow(String emoji, String label, String desc, javafx.scene.Node control) {
        // Icon
        StackPane iconBox = new StackPane();
        iconBox.setStyle(
            "-fx-background-color: " + MindDocTheme.PRIMARY + "15; " +
            "-fx-background-radius: 10; " +
            "-fx-min-width: 40; -fx-min-height: 40; " +
            "-fx-max-width: 40; -fx-max-height: 40;"
        );
        Label ico = new Label(emoji);
        ico.setFont(Font.font("System", 18));
        iconBox.getChildren().add(ico);

        // Text
        VBox text = new VBox(2);
        HBox.setHgrow(text, Priority.ALWAYS);
        Label nameLbl = new Label(label);
        nameLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        nameLbl.setTextFill(Color.web(MindDocTheme.TEXT_PRIMARY));
        Label descLbl = new Label(desc);
        descLbl.setFont(Font.font("Segoe UI", 11));
        descLbl.setTextFill(Color.web(MindDocTheme.TEXT_MUTED));
        text.getChildren().addAll(nameLbl, descLbl);

        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 0, 14, 0));
        row.getChildren().addAll(iconBox, text, control);
        return row;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private VBox card() {
        VBox c = new VBox(0);
        c.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 16; " +
            "-fx-border-radius: 16; " +
            "-fx-effect: dropshadow(three-pass-box, #00000014, 10, 0, 0, 3);"
        );
        return c;
    }

    private Region accentBar(String color) {
        Region r = new Region();
        r.setPrefHeight(4);
        r.setMaxWidth(Double.MAX_VALUE);
        r.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 16 16 0 0;");
        return r;
    }

    private Label sectionTitle(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        l.setTextFill(Color.web(MindDocTheme.TEXT_PRIMARY));
        l.setPadding(new Insets(0, 0, 6, 0));
        return l;
    }

    private Region divider() {
        Region r = new Region();
        r.setPrefHeight(1);
        r.setMaxWidth(Double.MAX_VALUE);
        r.setStyle("-fx-background-color: #f1f5f9;");
        return r;
    }

    private Region lightDivider() {
        Region r = new Region();
        r.setPrefHeight(1);
        r.setMaxWidth(Double.MAX_VALUE);
        r.setStyle("-fx-background-color: #f8fafc;");
        return r;
    }

    private Button toggleBtn(String text) {
        Button b = new Button(text);
        b.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        b.setCursor(Cursor.HAND);
        b.setStyle(toggleInactiveStyle());
        return b;
    }

    private String toggleActiveStyle() {
        return "-fx-background-color: " + MindDocTheme.PRIMARY + "; " +
               "-fx-text-fill: white; " +
               "-fx-background-radius: 10; " +
               "-fx-padding: 8 20; " +
               "-fx-cursor: hand; " +
               "-fx-effect: dropshadow(three-pass-box, #00000022, 4, 0, 0, 2);";
    }

    private String toggleInactiveStyle() {
        return "-fx-background-color: #f1f5f9; " +
               "-fx-text-fill: #6b7280; " +
               "-fx-background-radius: 10; " +
               "-fx-padding: 8 20; " +
               "-fx-cursor: hand; " +
               "-fx-border-color: #e5e7eb; " +
               "-fx-border-width: 1; " +
               "-fx-border-radius: 10;";
    }

    private void showStatus(String msg, boolean success) {
        statusLabel.setText(success ? "✓  " + msg : "✕  " + msg);
        statusLabel.setTextFill(Color.web(success ? MindDocTheme.SUCCESS : MindDocTheme.DANGER));
        statusLabel.setVisible(true);
    }

    // ── Data ─────────────────────────────────────────────────────────────────

    @Override
    public void refresh() {
        try {
            AppSettings s = settingsRepository.getOrCreateByUserId(currentUserId);
            selectLanguage(safeLanguage(s.getLanguage()));
            int size = Math.max(85, Math.min(130, s.getTextSize()));
            textSizeSlider.setValue(size);
            notificationsCheck.setSelected(s.isNotificationsEnabled());
            if (statusLabel != null) statusLabel.setVisible(false);
        } catch (SQLException e) {
            logger.error("Failed to load settings for user {}", currentUserId, e);
            showStatus("Failed to load settings.", false);
        }
    }

    private void saveSettings() {
        try {
            AppSettings s = settingsRepository.getOrCreateByUserId(currentUserId);
            s.setLanguage(safeLanguage(currentSelectedLanguage()));
            s.setTextSize((int) Math.round(textSizeSlider.getValue()));
            s.setNotificationsEnabled(notificationsCheck.isSelected());
            settingsRepository.save(s);
            if (onSettingsApplied != null) onSettingsApplied.accept(s);
            showStatus("Settings saved successfully!", true);
        } catch (SQLException e) {
            logger.error("Failed to save settings for user {}", currentUserId, e);
            showStatus("Failed to save settings.", false);
        }
    }

    private void resetToDefaults() {
        selectLanguage("English");
        textSizeSlider.setValue(100);
        notificationsCheck.setSelected(true);
        saveSettings();
        showStatus("Settings reset to defaults.", true);
    }

    private String safeLanguage(String v) {
        return "Українська".equals(v) ? "Українська" : "English";
    }

    // ── i18n ─────────────────────────────────────────────────────────────────

    public void applyLanguage(String language) {
        boolean ua = "Українська".equalsIgnoreCase(language);
        if (titleLabel        != null) titleLabel.setText(I18n.t("settings", "⚙️ Settings"));
        if (appearanceTitle   != null) appearanceTitle.setText(ua ? "🎨  Зовнішній вигляд" : "🎨  Appearance");
        if (prefsTitle        != null) prefsTitle.setText(ua ? "🔔  Налаштування" : "🔔  Preferences");
        if (aboutTitle        != null) aboutTitle.setText(ua ? "ℹ️  Про MindDoc"   : "ℹ️  About MindDoc");
        if (applyButton       != null) applyButton.setText(ua ? "💾  Зберегти" : "💾  Save Settings");
        if (resetButton       != null) resetButton.setText(ua ? "↺  За замовчуванням" : "↺  Reset to Defaults");
    }
}
