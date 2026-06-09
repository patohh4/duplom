package com.mindoc;

import com.mindoc.database.DatabaseManager;
import com.mindoc.model.AppSettings;
import com.mindoc.repository.AppSettingsRepository;
import com.mindoc.repository.UserRepository;
import com.mindoc.ui.auth.LoginPanel;
import com.mindoc.ui.dashboard.DashboardPanel;
import com.mindoc.ui.moodtracking.MoodTrackingPanel;
import com.mindoc.ui.profile.ProfilePanel;
import com.mindoc.ui.analytics.AnalyticsPanel;
import com.mindoc.ui.courses.CoursesPanel;
import com.mindoc.ui.exercises.ExercisesPanel;
import com.mindoc.ui.settings.SettingsPanel;
import com.mindoc.ui.symptomtracker.SymptomTrackerPanel;
import com.mindoc.ui.theme.MindDocTheme;
import com.mindoc.util.I18n;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main MindDoc Application entry point
 */
public class MindDocApp extends Application {
    private static final Logger logger = LoggerFactory.getLogger(MindDocApp.class);

    private DatabaseManager databaseManager;
    private UserRepository userRepository;
    private int currentUserId = -1;
    private String currentUsername = "";

    private Stage primaryStage;
    private Scene loginScene;
    private Scene mainScene;
    private BorderPane mainRoot;
    private TabPane mainTabPane;
    private HBox mainNavBar;
    private List<Button> navButtons = new ArrayList<>();
    private AppSettingsRepository appSettingsRepository;

    private DashboardPanel dashboardPanel;
    private MoodTrackingPanel moodTrackingPanel;
    private ProfilePanel profilePanel;
    private AnalyticsPanel analyticsPanel;
    private SymptomTrackerPanel symptomTrackerPanel;
    private CoursesPanel coursesPanel;
    private ExercisesPanel exercisesPanel;
    private SettingsPanel settingsPanel;
    // kept for applyLanguage text updates
    private Button[] navBtnRefs = new Button[8];

    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("Starting MindDoc Application");
            this.primaryStage = primaryStage;

            databaseManager = new DatabaseManager();
            appSettingsRepository = new AppSettingsRepository(databaseManager.getConnection());

            createLoginScene();
            createMainScene();

            primaryStage.setTitle("🧠 MindDoc - Mental Health Support");
            primaryStage.setScene(loginScene);
            primaryStage.setOnCloseRequest(e -> onApplicationClose());
            primaryStage.show();

            logger.info("MindDoc Application started successfully");
        } catch (Exception e) {
            logger.error("Failed to start application", e);
            showErrorDialog("Error", "Failed to start application: " + e.getMessage());
        }
    }

    private void createLoginScene() {
        LoginPanel loginPanel = new LoginPanel(databaseManager, this, this::onLoginSuccess);
        loginScene = new Scene(loginPanel, 1200, 800);
        loginScene.getStylesheets().add(MindDocTheme.toDataUri("Light"));
    }

    private void onLoginSuccess() {
        if (currentUserId > 0) {
            createMainScene();
            logger.info("User {} logged in successfully", currentUsername);
            if (dashboardPanel != null) {
                dashboardPanel.setCurrentUsername(currentUsername);
                dashboardPanel.refresh();
            }
        }
    }

    private void onLogout() {
        logger.info("User {} logged out", currentUsername);
        currentUserId = -1;
        currentUsername = "";
        createLoginScene();
        primaryStage.setScene(loginScene);
    }

    public void setCurrentUser(int userId, String username) {
        this.currentUserId = userId;
        this.currentUsername = username;
    }

    private void createMainScene() {
        mainRoot = new BorderPane();
        mainRoot.setStyle("-fx-background-color: " + MindDocTheme.BACKGROUND + ";");

        mainTabPane = createTabPane();
        mainNavBar  = createNavBar(mainTabPane);

        mainRoot.setTop(mainNavBar);
        mainRoot.setCenter(mainTabPane);

        mainScene = new Scene(mainRoot, 1280, 800);
        mainScene.getStylesheets().add(MindDocTheme.toDataUri("Light"));

        // hide the built-in tab header after scene is ready
        Platform.runLater(() -> {
            javafx.scene.Node header = mainTabPane.lookup(".tab-header-area");
            if (header != null) { header.setManaged(false); header.setVisible(false); }
        });

        applyInitialSettings();

        primaryStage.setTitle("MindDoc - Mental Health Support");
        primaryStage.setScene(mainScene);
        primaryStage.setOnCloseRequest(e -> onApplicationClose());
        primaryStage.show();

        logger.info("Main window initialized");
    }

    // ── Custom Navigation Bar ─────────────────────────────────────────────────

    private HBox createNavBar(TabPane tabPane) {
        HBox bar = new HBox(6);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(10, 16, 10, 16));
        bar.setStyle("-fx-background-color: " + MindDocTheme.PRIMARY + ";");

        // Brand logo + name
        HBox brand = new HBox(8);
        brand.setAlignment(Pos.CENTER_LEFT);
        brand.setPadding(new Insets(0, 12, 0, 0));

        Label logoEmoji = new Label("🧠");
        logoEmoji.setFont(Font.font("System", 22));

        Label brandName = new Label("MindDoc");
        brandName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        brandName.setTextFill(Color.WHITE);

        brand.getChildren().addAll(logoEmoji, brandName);

        // Vertical separator
        Separator vSep = new Separator(Orientation.VERTICAL);
        vSep.setStyle("-fx-background-color: #ffffff55;");
        vSep.setPrefHeight(28);
        HBox.setMargin(vSep, new Insets(0, 8, 0, 4));

        bar.getChildren().addAll(brand, vSep);

        // Tab navigation buttons
        String[][] tabs = {
            {"🏠", "Dashboard"},
            {"👤", "Profile"},
            {"😊", "Track Mood"},
            {"🩺", "Symptoms"},
            {"📚", "Learn"},
            {"💪", "Exercises"},
            {"📊", "Analytics"},
            {"⚙️", "Settings"}
        };

        navButtons.clear();
        for (int i = 0; i < tabs.length; i++) {
            final int idx = i;
            Button btn = new Button(tabs[i][0] + "  " + tabs[i][1]);
            btn.setFont(Font.font("Segoe UI", 13));
            btn.setCursor(Cursor.HAND);
            btn.setStyle(navInactiveStyle());
            btn.setOnAction(e -> {
                tabPane.getSelectionModel().select(idx);
                updateNavActive(idx);
            });
            navButtons.add(btn);
            navBtnRefs[i] = btn;
            bar.getChildren().add(btn);
        }

        // Spacer → logout button on right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("⎋  Logout");
        logoutBtn.setFont(Font.font("Segoe UI", 12));
        logoutBtn.setCursor(Cursor.HAND);
        logoutBtn.setStyle(
            "-fx-background-color: #ffffff22; " +
            "-fx-text-fill: white; " +
            "-fx-border-color: #ffffff66; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 6 12;"
        );
        logoutBtn.setOnAction(e -> onLogout());

        bar.getChildren().addAll(spacer, logoutBtn);

        // Sync nav highlight when tab changes externally
        tabPane.getSelectionModel().selectedIndexProperty().addListener(
            (obs, oldV, newV) -> updateNavActive(newV.intValue())
        );

        updateNavActive(0);
        return bar;
    }

    private void updateNavActive(int activeIdx) {
        for (int i = 0; i < navButtons.size(); i++) {
            navButtons.get(i).setStyle(i == activeIdx ? navActiveStyle() : navInactiveStyle());
            navButtons.get(i).setFont(Font.font("Segoe UI",
                i == activeIdx ? FontWeight.BOLD : FontWeight.NORMAL, 13));
        }
    }

    private String navActiveStyle() {
        return "-fx-background-color: white; " +
               "-fx-text-fill: " + MindDocTheme.PRIMARY + "; " +
               "-fx-background-radius: 10; " +
               "-fx-border-radius: 10; " +
               "-fx-padding: 7 16; " +
               "-fx-cursor: hand; " +
               "-fx-effect: dropshadow(three-pass-box, #00000022, 4, 0, 0, 2);";
    }

    private String navInactiveStyle() {
        return "-fx-background-color: transparent; " +
               "-fx-text-fill: white; " +
               "-fx-border-color: #ffffff66; " +
               "-fx-border-width: 1.5; " +
               "-fx-border-radius: 10; " +
               "-fx-background-radius: 10; " +
               "-fx-padding: 7 16; " +
               "-fx-cursor: hand;";
    }

    private TabPane createTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Стиль самого TabPane — фон хедера задається через CSS у MindDocTheme
        tabPane.setStyle("-fx-font-size: 13px;");

        // Dashboard tab
        dashboardPanel = new DashboardPanel(currentUserId, databaseManager);
        dashboardPanel.setCurrentUsername(currentUsername);
        Tab dashboardTab = createTab("🏠  Dashboard", dashboardPanel);

        // Profile tab
        userRepository = new UserRepository(databaseManager.getConnection());
        profilePanel = new ProfilePanel(currentUserId, userRepository);
        Tab profileTab = createTab("👤  Profile", profilePanel);

        // Mood Tracking tab
        moodTrackingPanel = new MoodTrackingPanel(currentUserId, databaseManager, this::refreshMoodDependentPanels);
        Tab moodTab = createTab("😊  Track Mood", moodTrackingPanel);

        // Symptoms tab
        symptomTrackerPanel = new SymptomTrackerPanel(currentUserId, databaseManager);
        Tab symptomsTab = createTab("🩺  Symptoms", symptomTrackerPanel);

        // Courses tab
        coursesPanel = new CoursesPanel(currentUserId, databaseManager);
        Tab coursesTab = createTab("📚  Learn", coursesPanel);

        // Exercises tab
        exercisesPanel = new ExercisesPanel(currentUserId, databaseManager);
        Tab exercisesTab = createTab("💪  Exercises", exercisesPanel);

        // Analytics tab
        analyticsPanel = new AnalyticsPanel(currentUserId, databaseManager);
        Tab analyticsTab = createTab("📊  Analytics", analyticsPanel);

        // Settings tab
        settingsPanel = new SettingsPanel(currentUserId, appSettingsRepository, this::applyAppSettings);
        Tab settingsTab = createTab("⚙️  Settings", settingsPanel);

        tabPane.getTabs().addAll(
                dashboardTab,
                profileTab,
                moodTab,
                symptomsTab,
                coursesTab,
                exercisesTab,
                analyticsTab,
                settingsTab
        );

        return tabPane;
    }

    /**
     * Допоміжний метод — створює Tab з однаковим базовим стилем.
     * Закривати вкладки заборонено.
     */
    private Tab createTab(String title, javafx.scene.Node content) {
        Tab tab = new Tab(title, content);
        tab.setClosable(false);
        return tab;
    }

    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(I18n.t("about_title", "About MindDoc"));
        alert.setHeaderText(I18n.t("about_header", "MindDoc - Mental Health Support"));
        alert.setContentText(I18n.t("about_body",
                "MindDoc is a professional desktop application for mental health tracking.\n\n" +
                        "Features:\n" +
                        "• Track your mood daily\n" +
                        "• Monitor symptoms\n" +
                        "• Learn through CBT courses\n" +
                        "• Practice exercises and strategies\n" +
                        "• Get personalized recommendations\n\n" +
                        "Version 2.0.0\n" +
                        "© 2026 MindDoc Team"
        ));
        alert.showAndWait();
    }

    private void refreshMoodDependentPanels() {
        if (dashboardPanel != null) {
            dashboardPanel.refresh();
        }
        if (analyticsPanel != null) {
            analyticsPanel.refresh();
        }
    }

    private void applyInitialSettings() {
        try {
            AppSettings settings = appSettingsRepository.getOrCreateByUserId(currentUserId);
            applyAppSettings(settings);
        } catch (Exception e) {
            logger.error("Failed to apply initial settings", e);
        }
    }

    private void applyAppSettings(AppSettings settings) {
        if (settings == null || mainRoot == null || mainTabPane == null) return;
        I18n.setLanguage(settings.getLanguage());

        boolean dark = "Dark".equalsIgnoreCase(settings.getTheme());
        String bg   = dark ? "#1f2937" : MindDocTheme.BACKGROUND;
        String text = dark ? "#f9fafb" : "#2d3748";
        int fontPx  = Math.max(11, Math.min(20, Math.round(settings.getTextSize() / 8.0f)));

        mainRoot.setStyle("-fx-background-color: " + bg + "; -fx-font-size: " + fontPx + "px;");
        mainTabPane.setStyle("-fx-font-size: " + fontPx + "px;");

        applyLanguage(settings.getLanguage());

        if (mainScene != null) {
            mainScene.getStylesheets().clear();
            mainScene.getStylesheets().add(MindDocTheme.toDataUri(settings.getTheme()));
            mainScene.getRoot().setStyle(
                "-fx-background-color: " + bg + "; " +
                "-fx-text-fill: " + text + "; " +
                "-fx-font-size: " + fontPx + "px;"
            );
        }
    }

    private void applyLanguage(String language) {
        boolean ua = "Українська".equalsIgnoreCase(language);

        // Update nav button labels
        String[][] labels = {
            {ua ? "🏠  Дашборд"      : "🏠  Dashboard"},
            {ua ? "👤  Профіль"      : "👤  Profile"},
            {ua ? "😊  Настрій"      : "😊  Track Mood"},
            {ua ? "🩺  Симптоми"     : "🩺  Symptoms"},
            {ua ? "📚  Навчання"     : "📚  Learn"},
            {ua ? "💪  Вправи"       : "💪  Exercises"},
            {ua ? "📊  Аналітика"    : "📊  Analytics"},
            {ua ? "⚙️  Налаштування" : "⚙️  Settings"}
        };
        for (int i = 0; i < navBtnRefs.length && i < labels.length; i++) {
            if (navBtnRefs[i] != null) navBtnRefs[i].setText(labels[i][0]);
        }

        if (symptomTrackerPanel != null)  symptomTrackerPanel.applyLanguage(language);
        if (dashboardPanel != null)       dashboardPanel.applyLanguage();
        if (moodTrackingPanel != null)    moodTrackingPanel.applyLanguage();
        if (coursesPanel != null)         coursesPanel.applyLanguage(language);
        if (exercisesPanel != null)       exercisesPanel.applyLanguage(language);
        if (profilePanel != null)         profilePanel.applyLanguage(language);
        if (analyticsPanel != null)       analyticsPanel.applyLanguage(language);
        if (settingsPanel != null)        settingsPanel.applyLanguage(language);
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void onApplicationClose() {
        try {
            if (databaseManager != null) {
                databaseManager.closeConnection();
                logger.info("Application closed successfully");
            }
        } catch (Exception e) {
            logger.error("Error closing database", e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}