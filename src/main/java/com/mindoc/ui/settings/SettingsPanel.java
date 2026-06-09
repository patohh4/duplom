package com.mindoc.ui.settings;

import com.mindoc.model.AppSettings;
import com.mindoc.repository.AppSettingsRepository;
import com.mindoc.ui.common.BasePanel;
import com.mindoc.ui.theme.MindDocTheme;
import com.mindoc.util.I18n;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.function.Consumer;

/**
 * Application settings UI panel (visual settings only).
 */
public class SettingsPanel extends BasePanel {
    private static final Logger logger = LoggerFactory.getLogger(SettingsPanel.class);

    private final int currentUserId;
    private final AppSettingsRepository settingsRepository;
    private final Consumer<AppSettings> onSettingsApplied;

    private ComboBox<String> languageCombo;
    private CheckBox notificationsCheck;
    private Slider textSizeSlider;
    private Label statusLabel;
    private Label titleLabel;
    private Label languageLabel;
    private Label textSizeLabel;
    private Button applyButton;
    private Button resetButton;

    public SettingsPanel(int currentUserId, AppSettingsRepository settingsRepository, Consumer<AppSettings> onSettingsApplied) {
        this.currentUserId = currentUserId;
        this.settingsRepository = settingsRepository;
        this.onSettingsApplied = onSettingsApplied;
        initializeUI();
        refresh();
    }

    private void initializeUI() {
        setStyle("-fx-background-color: " + MindDocTheme.BACKGROUND + ";");
        setPadding(new Insets(20));
        setSpacing(15);
        setFillWidth(true);

        titleLabel = new Label("⚙️ Settings");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setTextFill(javafx.scene.paint.Color.web(MindDocTheme.PRIMARY));
        getChildren().add(titleLabel);

        VBox card = new VBox(16);
        card.setStyle(
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-radius: 8;" +
            "-fx-padding: 20;" +
            "-fx-background-color: white;"
        );
        card.setMaxWidth(760);

        HBox languageRow = new HBox(12);
        languageRow.setAlignment(Pos.CENTER_LEFT);
        languageLabel = new Label("Language:");
        languageLabel.setMinWidth(180);
        languageCombo = new ComboBox<>();
        languageCombo.getItems().addAll("English", "Українська");
        languageCombo.setValue("English");
        languageCombo.setPrefWidth(220);
        languageRow.getChildren().addAll(languageLabel, languageCombo);

        HBox textSizeRow = new HBox(12);
        textSizeRow.setAlignment(Pos.CENTER_LEFT);
        textSizeLabel = new Label("Text size:");
        textSizeLabel.setMinWidth(180);
        textSizeSlider = new Slider(85, 130, 100);
        textSizeSlider.setShowTickLabels(true);
        textSizeSlider.setShowTickMarks(false);
        textSizeSlider.setMajorTickUnit(15);
        textSizeSlider.setMinorTickCount(0);
        textSizeSlider.setBlockIncrement(5);
        HBox.setHgrow(textSizeSlider, Priority.ALWAYS);
        textSizeRow.getChildren().addAll(textSizeLabel, textSizeSlider);

        notificationsCheck = new CheckBox("Enable app notifications");
        notificationsCheck.setSelected(true);

        HBox actions = new HBox(10);
        applyButton = new Button("Apply");
        applyButton.setStyle(
            "-fx-padding: 10px 20px;" +
            "-fx-font-size: 13px;" +
            "-fx-background-color: " + MindDocTheme.PRIMARY + ";" +
            "-fx-text-fill: white;" +
            "-fx-cursor: hand;"
        );
        resetButton = new Button("Reset");
        resetButton.setStyle(
            "-fx-padding: 10px 20px;" +
            "-fx-font-size: 13px;" +
            "-fx-background-color: #e5e7eb;" +
            "-fx-text-fill: #374151;" +
            "-fx-cursor: hand;"
        );
        actions.getChildren().addAll(applyButton, resetButton);

        statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: " + MindDocTheme.SUCCESS + "; -fx-font-size: 12px;");

        applyButton.setOnAction(e -> saveSettings());
        resetButton.setOnAction(e -> {
            languageCombo.setValue("English");
            textSizeSlider.setValue(100);
            notificationsCheck.setSelected(true);
            saveSettings();
            statusLabel.setText("Settings reset to defaults.");
        });

        card.getChildren().addAll(
            languageRow,
            textSizeRow,
            notificationsCheck,
            actions,
            statusLabel
        );

        getChildren().add(card);
    }

    @Override
    public void refresh() {
        try {
            AppSettings settings = settingsRepository.getOrCreateByUserId(currentUserId);
            languageCombo.setValue(safeLanguage(settings.getLanguage()));
            int textSize = Math.max(85, Math.min(130, settings.getTextSize()));
            textSizeSlider.setValue(textSize);
            notificationsCheck.setSelected(settings.isNotificationsEnabled());
            statusLabel.setText("");
        } catch (SQLException e) {
            logger.error("Failed to load settings for user {}", currentUserId, e);
            statusLabel.setStyle("-fx-text-fill: " + MindDocTheme.DANGER + "; -fx-font-size: 12px;");
            statusLabel.setText("Failed to load settings.");
        }
    }

    private void saveSettings() {
        try {
            AppSettings settings = settingsRepository.getOrCreateByUserId(currentUserId);
            settings.setLanguage(safeLanguage(languageCombo.getValue()));
            settings.setTextSize((int) Math.round(textSizeSlider.getValue()));
            settings.setNotificationsEnabled(notificationsCheck.isSelected());
            settingsRepository.save(settings);
            if (onSettingsApplied != null) {
                onSettingsApplied.accept(settings);
            }
            statusLabel.setStyle("-fx-text-fill: " + MindDocTheme.SUCCESS + "; -fx-font-size: 12px;");
            statusLabel.setText("Settings saved.");
        } catch (SQLException e) {
            logger.error("Failed to save settings for user {}", currentUserId, e);
            statusLabel.setStyle("-fx-text-fill: " + MindDocTheme.DANGER + "; -fx-font-size: 12px;");
            statusLabel.setText("Failed to save settings.");
        }
    }

    private String safeLanguage(String value) {
        if ("Українська".equals(value) || "English".equals(value)) {
            return value;
        }
        return "English";
    }

    public void applyLanguage(String language) {
        if (titleLabel != null) titleLabel.setText(I18n.t("settings", "⚙️ Settings"));
        if (languageLabel != null) languageLabel.setText(I18n.t("language", "Language:"));
        if (textSizeLabel != null) textSizeLabel.setText(I18n.t("text_size", "Text size:"));
        if (notificationsCheck != null) notificationsCheck.setText(I18n.t("notifications", "Enable app notifications"));
        if (applyButton != null) applyButton.setText(I18n.t("apply", "Apply"));
        if (resetButton != null) resetButton.setText(I18n.t("reset", "Reset"));
    }
}
