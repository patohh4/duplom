package com.mindoc;

import com.mindoc.database.DatabaseManager;
import com.mindoc.repository.UserRepository;
import com.mindoc.ui.auth.LoginPanel;
import com.mindoc.ui.dashboard.DashboardPanel;
import com.mindoc.ui.moodtracking.MoodTrackingPanel;
import com.mindoc.ui.profile.ProfilePanel;
import com.mindoc.ui.analytics.AnalyticsPanel;
import com.mindoc.ui.courses.CoursesPanel;
import com.mindoc.ui.exercises.ExercisesPanel;
import com.mindoc.ui.symptomtracker.SymptomTrackerPanel;
import com.mindoc.ui.theme.MindDocTheme;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
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
    private int currentUserId = -1; // No user logged in initially
    private String currentUsername = "";
    
    private Stage primaryStage;
    private Scene loginScene;
    
    private DashboardPanel dashboardPanel;
    private MoodTrackingPanel moodTrackingPanel;
    private ProfilePanel profilePanel;
    
    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("Starting MindDoc Application");
            this.primaryStage = primaryStage;
            
            // Initialize database
            databaseManager = new DatabaseManager();
            
            // Create scenes
            createLoginScene();
            createMainScene();
            
            // Show login first
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
        loginScene.getStylesheets().add("data:text/css," + 
            MindDocTheme.getStylesheet().replace("\n", "").replace("\"", "'"));
    }
    
    private void onLoginSuccess() {
        if (currentUserId > 0) {
            createMainScene();
            logger.info("User {} logged in successfully", currentUsername);
            if (dashboardPanel != null) {
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
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + MindDocTheme.BACKGROUND + ";");
        
        // Create menu bar
        MenuBar menuBar = createMenuBar(primaryStage);
        root.setTop(menuBar);
        
        // Create tab pane
        TabPane tabPane = createTabPane();
        root.setCenter(tabPane);
        
        // Create status bar
        Label statusBar = new Label("Welcome to MindDoc - Your Mental Health Companion");
        statusBar.setStyle(
            "-fx-padding: 10px 20px;" +
            "-fx-background-color: " + MindDocTheme.PRIMARY + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 12px;"
        );
        root.setBottom(statusBar);
        
        // Create scene
        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add("data:text/css," + 
            MindDocTheme.getStylesheet().replace("\n", "").replace("\"", "'"));
        
        primaryStage.setTitle("🧠 MindDoc - Mental Health Support");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> onApplicationClose());
        primaryStage.show();
        
        logger.info("Main window initialized");
    }
    
    private MenuBar createMenuBar(Stage primaryStage) {
        MenuBar menuBar = new MenuBar();
        menuBar.setStyle("-fx-background-color: " + MindDocTheme.PRIMARY + ";");
        
        // File menu
        Menu fileMenu = new Menu("File");
        fileMenu.setStyle("-fx-text-fill: white;");
        
        MenuItem logoutItem = new MenuItem("Logout");
        logoutItem.setOnAction(e -> onLogout());
        
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> primaryStage.close());
        fileMenu.getItems().addAll(logoutItem, new SeparatorMenuItem(), exitItem);
        
        // Mood menu
        Menu moodMenu = new Menu("Mood");
        moodMenu.setStyle("-fx-text-fill: white;");
        
        MenuItem logMoodItem = new MenuItem("Log Mood");
        logMoodItem.setOnAction(e -> {/* Switch to mood tracking */});
        
        MenuItem viewHistoryItem = new MenuItem("View History");
        viewHistoryItem.setOnAction(e -> {/* Switch to mood history */});
        
        moodMenu.getItems().addAll(logMoodItem, new SeparatorMenuItem(), viewHistoryItem);
        
        // Learning menu
        Menu learningMenu = new Menu("Learn");
        learningMenu.setStyle("-fx-text-fill: white;");
        
        MenuItem courseItem = new MenuItem("CBT Courses");
        MenuItem exerciseItem = new MenuItem("Exercises & Strategies");
        
        learningMenu.getItems().addAll(courseItem, exerciseItem);
        
        // Help menu
        Menu helpMenu = new Menu("Help");
        helpMenu.setStyle("-fx-text-fill: white;");
        
        MenuItem aboutItem = new MenuItem("About MindDoc");
        aboutItem.setOnAction(e -> showAboutDialog());
        
        MenuItem profileItem = new MenuItem("My Profile");
        profileItem.setOnAction(e -> {
            if (profilePanel != null) {
                profilePanel.refresh();
            }
        });
        
        MenuItem settingsItem = new MenuItem("Settings");
        
        helpMenu.getItems().addAll(profileItem, new SeparatorMenuItem(), aboutItem, new SeparatorMenuItem(), settingsItem);
        
        menuBar.getMenus().addAll(fileMenu, moodMenu, learningMenu, helpMenu);
        return menuBar;
    }
    
    private TabPane createTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-font-size: 13px;");
        
        // Dashboard tab
        dashboardPanel = new DashboardPanel(currentUserId);
        Tab dashboardTab = new Tab("🏠 Dashboard", dashboardPanel);
        dashboardTab.setClosable(false);
        
        // Profile tab
        userRepository = new UserRepository(databaseManager.getConnection());
        profilePanel = new ProfilePanel(currentUserId, userRepository);
        Tab profileTab = new Tab("👤 Profile", profilePanel);
        profileTab.setClosable(false);
        
        // Mood Tracking tab
        moodTrackingPanel = new MoodTrackingPanel(currentUserId);
        Tab moodTab = new Tab("😊 Track Mood", moodTrackingPanel);
        moodTab.setClosable(false);
        
        // Symptoms tab - with new SymptomTrackerPanel
        SymptomTrackerPanel symptomTrackerPanel = new SymptomTrackerPanel(currentUserId, databaseManager);
        Tab symptomsTab = new Tab("🩺 Symptoms", symptomTrackerPanel);
        symptomsTab.setClosable(false);
        
        // Courses tab - with new CoursesPanel
        CoursesPanel coursesPanel = new CoursesPanel(currentUserId, databaseManager);
        Tab coursesTab = new Tab("📚 Learn", coursesPanel);
        coursesTab.setClosable(false);
        
        // Exercises tab - with new ExercisesPanel
        ExercisesPanel exercisesPanel = new ExercisesPanel(currentUserId, databaseManager);
        Tab exercisesTab = new Tab("💪 Exercises", exercisesPanel);
        exercisesTab.setClosable(false);
        
        // Analytics tab - with new AnalyticsPanel
        AnalyticsPanel analyticsPanel = new AnalyticsPanel(currentUserId, databaseManager);
        Tab analyticsTab = new Tab("📊 Analytics", analyticsPanel);
        analyticsTab.setClosable(false);
        
        tabPane.getTabs().addAll(
            dashboardTab,
            profileTab,
            moodTab,
            symptomsTab,
            coursesTab,
            exercisesTab,
            analyticsTab
        );
        
        return tabPane;
    }
    
    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About MindDoc");
        alert.setHeaderText("MindDoc - Mental Health Support");
        alert.setContentText(
            "MindDoc is a professional desktop application for mental health tracking.\n\n" +
            "Features:\n" +
            "• Track your mood daily\n" +
            "• Monitor symptoms\n" +
            "• Learn through CBT courses\n" +
            "• Practice exercises and strategies\n" +
            "• Get personalized recommendations\n\n" +
            "Version 2.0.0\n" +
            "© 2026 MindDoc Team"
        );
        alert.showAndWait();
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
