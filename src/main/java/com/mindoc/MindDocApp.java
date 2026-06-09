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
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
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
    private MenuBar mainMenuBar;
    private AppSettingsRepository appSettingsRepository;

    private DashboardPanel dashboardPanel;
    private MoodTrackingPanel moodTrackingPanel;
    private ProfilePanel profilePanel;
    private AnalyticsPanel analyticsPanel;
    private SymptomTrackerPanel symptomTrackerPanel;
    private CoursesPanel coursesPanel;
    private ExercisesPanel exercisesPanel;
    private SettingsPanel settingsPanel;
    private Menu fileMenu;
    private Menu moodMenu;
    private Menu learningMenu;
    private Menu helpMenu;
    private MenuItem logoutItem;
    private MenuItem exitItem;
    private MenuItem logMoodItem;
    private MenuItem viewHistoryItem;
    private MenuItem courseItem;
    private MenuItem exerciseItem;
    private MenuItem aboutItem;
    private MenuItem profileItem;
    private MenuItem settingsItem;

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
        mainRoot.setCenter(mainTabPane);

        mainMenuBar = createMenuBar(primaryStage, mainTabPane);
        mainRoot.setTop(mainMenuBar);

        mainScene = new Scene(mainRoot, 1200, 800);
        mainScene.getStylesheets().add(MindDocTheme.toDataUri("Light"));
        applyInitialSettings();

        primaryStage.setTitle("🧠 MindDoc - Mental Health Support");
        primaryStage.setScene(mainScene);
        primaryStage.setOnCloseRequest(e -> onApplicationClose());
        primaryStage.show();

        logger.info("Main window initialized");
    }

    private MenuBar createMenuBar(Stage primaryStage, TabPane tabPane) {
        MenuBar menuBar = new MenuBar();
        // Темніший зелений для менюбару — щоб відрізнявся від таб-панелі
        menuBar.setStyle(
                "-fx-background-color: " + MindDocTheme.PRIMARY_DARK + "; " +
                        "-fx-padding: 2 8;"
        );

        // File menu
        fileMenu = new Menu("File");
        fileMenu.setStyle("-fx-text-fill: white;");

        logoutItem = new MenuItem("Logout");
        logoutItem.setOnAction(e -> onLogout());

        exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> primaryStage.close());
        fileMenu.getItems().addAll(logoutItem, new SeparatorMenuItem(), exitItem);

        // Mood menu
        moodMenu = new Menu("Mood");
        moodMenu.setStyle("-fx-text-fill: white;");

        logMoodItem = new MenuItem("Log Mood");
        logMoodItem.setOnAction(e -> tabPane.getSelectionModel().select(2));

        viewHistoryItem = new MenuItem("View History");
        viewHistoryItem.setOnAction(e -> tabPane.getSelectionModel().select(6));

        moodMenu.getItems().addAll(logMoodItem, new SeparatorMenuItem(), viewHistoryItem);

        // Learning menu
        learningMenu = new Menu("Learn");
        learningMenu.setStyle("-fx-text-fill: white;");

        courseItem = new MenuItem("CBT Courses");
        courseItem.setOnAction(e -> tabPane.getSelectionModel().select(4));
        exerciseItem = new MenuItem("Exercises & Strategies");
        exerciseItem.setOnAction(e -> tabPane.getSelectionModel().select(5));

        learningMenu.getItems().addAll(courseItem, exerciseItem);

        // Help menu
        helpMenu = new Menu("Help");
        helpMenu.setStyle("-fx-text-fill: white;");

        aboutItem = new MenuItem("About MindDoc");
        aboutItem.setOnAction(e -> showAboutDialog());

        profileItem = new MenuItem("My Profile");
        profileItem.setOnAction(e -> {
            tabPane.getSelectionModel().select(1);
            if (profilePanel != null) {
                profilePanel.refresh();
            }
        });

        settingsItem = new MenuItem("Settings");
        settingsItem.setOnAction(e -> tabPane.getSelectionModel().select(7));

        helpMenu.getItems().addAll(profileItem, new SeparatorMenuItem(), aboutItem, new SeparatorMenuItem(), settingsItem);

        menuBar.getMenus().addAll(fileMenu, moodMenu, learningMenu, helpMenu);
        return menuBar;
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
        if (settings == null || mainRoot == null || mainTabPane == null) {
            return;
        }
        I18n.setLanguage(settings.getLanguage());

        boolean dark = "Dark".equalsIgnoreCase(settings.getTheme());
        String bg   = dark ? "#1f2937" : MindDocTheme.BACKGROUND;
        String text = dark ? "#f9fafb" : "#2d3748";
        int fontPx  = Math.max(11, Math.min(20, Math.round(settings.getTextSize() / 8.0f)));

        mainRoot.setStyle("-fx-background-color: " + bg + "; -fx-font-size: " + fontPx + "px;");
        mainTabPane.setStyle("-fx-font-size: " + fontPx + "px;");
        if (mainMenuBar != null) {
            mainMenuBar.setStyle(
                    "-fx-background-color: " + MindDocTheme.PRIMARY_DARK + "; " +
                            "-fx-padding: 2 8; " +
                            "-fx-font-size: " + fontPx + "px;"
            );
        }

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
        if (fileMenu != null)     fileMenu.setText(ua ? "Файл" : "File");
        if (moodMenu != null)     moodMenu.setText(ua ? "Настрій" : "Mood");
        if (learningMenu != null) learningMenu.setText(ua ? "Навчання" : "Learn");
        if (helpMenu != null)     helpMenu.setText(ua ? "Допомога" : "Help");

        if (logoutItem != null)     logoutItem.setText(ua ? "Вийти з акаунту" : "Logout");
        if (exitItem != null)       exitItem.setText(ua ? "Закрити" : "Exit");
        if (logMoodItem != null)    logMoodItem.setText(ua ? "Записати настрій" : "Log Mood");
        if (viewHistoryItem != null) viewHistoryItem.setText(ua ? "Історія" : "View History");
        if (courseItem != null)     courseItem.setText(ua ? "Курси КПТ" : "CBT Courses");
        if (exerciseItem != null)   exerciseItem.setText(ua ? "Вправи та стратегії" : "Exercises & Strategies");
        if (aboutItem != null)      aboutItem.setText(ua ? "Про MindDoc" : "About MindDoc");
        if (profileItem != null)    profileItem.setText(ua ? "Мій профіль" : "My Profile");
        if (settingsItem != null)   settingsItem.setText(ua ? "Налаштування" : "Settings");

        if (mainTabPane != null && mainTabPane.getTabs().size() >= 8) {
            mainTabPane.getTabs().get(0).setText(ua ? "🏠  Дашборд"       : "🏠  Dashboard");
            mainTabPane.getTabs().get(1).setText(ua ? "👤  Профіль"       : "👤  Profile");
            mainTabPane.getTabs().get(2).setText(ua ? "😊  Настрій"       : "😊  Track Mood");
            mainTabPane.getTabs().get(3).setText(ua ? "🩺  Симптоми"      : "🩺  Symptoms");
            mainTabPane.getTabs().get(4).setText(ua ? "📚  Навчання"      : "📚  Learn");
            mainTabPane.getTabs().get(5).setText(ua ? "💪  Вправи"        : "💪  Exercises");
            mainTabPane.getTabs().get(6).setText(ua ? "📊  Аналітика"     : "📊  Analytics");
            mainTabPane.getTabs().get(7).setText(ua ? "⚙️  Налаштування"  : "⚙️  Settings");
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