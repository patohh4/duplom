package com.mindoc.ui.auth;

import com.mindoc.MindDocApp;
import com.mindoc.database.DatabaseManager;
import com.mindoc.model.User;
import com.mindoc.repository.UserRepository;
import com.mindoc.ui.theme.MindDocTheme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Login and Registration Panel
 */
public class LoginPanel extends VBox {
    private static final Logger logger = LoggerFactory.getLogger(LoginPanel.class);
    
    private UserRepository userRepository;
    private Runnable onLoginSuccess;
    private MindDocApp app;
    
    private CardStackPane cardStack;
    
    public LoginPanel(DatabaseManager databaseManager, MindDocApp app, Runnable onLoginSuccess) {
        this.userRepository = new UserRepository(databaseManager.getConnection());
        this.app = app;
        this.onLoginSuccess = onLoginSuccess;
        
        initializeUI();
    }
    
    private void initializeUI() {
        setStyle("-fx-background-color: " + MindDocTheme.BACKGROUND + ";");
        setPadding(new Insets(20));
        setAlignment(Pos.CENTER);
        
        // Main container
        VBox mainContainer = new VBox(30);
        mainContainer.setStyle(
            "-fx-border-radius: 15;" +
            "-fx-background-color: white;" +
            "-fx-padding: 50px;" +
            "-fx-border-color: #e0e0e0;" +
            "-fx-border-radius: 15;"
        );
        mainContainer.setPrefWidth(450);
        mainContainer.setAlignment(Pos.TOP_CENTER);
        
        // Header
        VBox header = createHeader();
        mainContainer.getChildren().add(header);
        
        // Card stack for login/register
        cardStack = new CardStackPane();
        cardStack.addCard("Login", createLoginCard());
        cardStack.addCard("Register", createRegisterCard());
        mainContainer.getChildren().add(cardStack);
        
        // Buttons for switching
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));
        
        Button loginBtn = new Button("Login");
        Button registerBtn = new Button("Register");
        
        loginBtn.setStyle(getModeButtonStyle(true));
        registerBtn.setStyle(getModeButtonStyle(false));
        
        loginBtn.setOnAction(e -> {
            cardStack.showCard("Login");
            loginBtn.setStyle(getModeButtonStyle(true));
            registerBtn.setStyle(getModeButtonStyle(false));
        });
        
        registerBtn.setOnAction(e -> {
            cardStack.showCard("Register");
            loginBtn.setStyle(getModeButtonStyle(false));
            registerBtn.setStyle(getModeButtonStyle(true));
        });
        
        buttonBox.getChildren().addAll(loginBtn, registerBtn);
        mainContainer.getChildren().add(buttonBox);
        
        // Demo users info
        Label demoLabel = new Label("📝 Demo credentials:\nUsername: demo | Password: demo\nOr create a new account");
        demoLabel.setStyle(
            "-fx-text-fill: #999;" +
            "-fx-font-size: 11px;" +
            "-fx-text-alignment: center;"
        );
        demoLabel.setPadding(new Insets(15, 0, 0, 0));
        mainContainer.getChildren().add(demoLabel);
        
        getChildren().add(mainContainer);
    }
    
    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        
        Label titleLabel = new Label("🧠 MindDoc");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 32));
        titleLabel.setTextFill(javafx.scene.paint.Color.web(MindDocTheme.PRIMARY));
        
        Label subtitleLabel = new Label("Your Mental Health Companion");
        subtitleLabel.setFont(Font.font("System", 14));
        subtitleLabel.setTextFill(javafx.scene.paint.Color.web("#666"));
        
        header.getChildren().addAll(titleLabel, subtitleLabel);
        return header;
    }
    
    private VBox createLoginCard() {
        VBox card = new VBox(15);
        card.setPadding(new Insets(0));
        
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setStyle(getInputFieldStyle());
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle(getInputFieldStyle());
        
        CheckBox rememberMe = new CheckBox("Remember me");
        rememberMe.setStyle("-fx-font-size: 12px;");
        
        Button loginButton = new Button("Login");
        loginButton.setPrefWidth(Double.MAX_VALUE);
        loginButton.setStyle(
            "-fx-padding: 12px;" +
            "-fx-font-size: 14px;" +
            "-fx-background-color: " + MindDocTheme.PRIMARY + ";" +
            "-fx-text-fill: white;" +
            "-fx-border-radius: 5;" +
            "-fx-cursor: hand;"
        );
        
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #f44336; -fx-font-size: 12px;");
        errorLabel.setWrapText(true);
        
        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            
            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Please enter username and password");
                return;
            }
            
            try {
                User user = userRepository.findByUsername(username);
                if (user != null && user.getPassword().equals(password)) {
                    logger.info("User logged in: {}", username);
                    app.setCurrentUser(user.getId(), username);
                    onLoginSuccess.run();
                } else {
                    errorLabel.setText("Invalid username or password");
                }
            } catch (SQLException ex) {
                logger.error("Login error", ex);
                errorLabel.setText("Database error: " + ex.getMessage());
            }
        });
        
        card.getChildren().addAll(
            usernameField,
            passwordField,
            rememberMe,
            errorLabel,
            loginButton
        );
        
        return card;
    }
    
    private VBox createRegisterCard() {
        VBox card = new VBox(15);
        card.setPadding(new Insets(0));
        
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setStyle(getInputFieldStyle());
        
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setStyle(getInputFieldStyle());
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle(getInputFieldStyle());
        
        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Confirm Password");
        confirmField.setStyle(getInputFieldStyle());
        
        Button registerButton = new Button("Create Account");
        registerButton.setPrefWidth(Double.MAX_VALUE);
        registerButton.setStyle(
            "-fx-padding: 12px;" +
            "-fx-font-size: 14px;" +
            "-fx-background-color: " + MindDocTheme.PRIMARY + ";" +
            "-fx-text-fill: white;" +
            "-fx-border-radius: 5;" +
            "-fx-cursor: hand;"
        );
        
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #f44336; -fx-font-size: 12px;");
        errorLabel.setWrapText(true);
        
        Label successLabel = new Label();
        successLabel.setStyle("-fx-text-fill: " + MindDocTheme.SUCCESS + "; -fx-font-size: 12px;");
        successLabel.setWrapText(true);
        
        registerButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText().trim();
            String confirm = confirmField.getText().trim();
            
            errorLabel.setText("");
            successLabel.setText("");
            
            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Please fill in all fields");
                return;
            }
            
            if (!password.equals(confirm)) {
                errorLabel.setText("Passwords do not match");
                return;
            }
            
            if (password.length() < 4) {
                errorLabel.setText("Password must be at least 4 characters");
                return;
            }
            
            try {
                if (userRepository.findByUsername(username) != null) {
                    errorLabel.setText("Username already exists");
                    return;
                }
                
                User newUser = new User(0, username, email, password, 
                    null, null, null, LocalDate.now(), true);
                userRepository.create(newUser);
                
                successLabel.setText("Account created! Please log in.");
                usernameField.clear();
                emailField.clear();
                passwordField.clear();
                confirmField.clear();
                
            } catch (SQLException ex) {
                logger.error("Registration error", ex);
                errorLabel.setText("Registration failed: " + ex.getMessage());
            }
        });
        
        card.getChildren().addAll(
            usernameField,
            emailField,
            passwordField,
            confirmField,
            errorLabel,
            successLabel,
            registerButton
        );
        
        return card;
    }
    
    private String getInputFieldStyle() {
        return "-fx-padding: 12px;" +
               "-fx-font-size: 13px;" +
               "-fx-border-radius: 5;" +
               "-fx-border-color: #e0e0e0;" +
               "-fx-border-width: 1;";
    }
    
    private String getModeButtonStyle(boolean active) {
        if (active) {
            return "-fx-padding: 8px 20px;" +
                   "-fx-font-size: 12px;" +
                   "-fx-background-color: " + MindDocTheme.PRIMARY + ";" +
                   "-fx-text-fill: white;" +
                   "-fx-border-radius: 3;" +
                   "-fx-cursor: hand;";
        } else {
            return "-fx-padding: 8px 20px;" +
                   "-fx-font-size: 12px;" +
                   "-fx-background-color: #f0f0f0;" +
                   "-fx-text-fill: #666;" +
                   "-fx-border-radius: 3;" +
                   "-fx-cursor: hand;";
        }
    }
    
    public int getCurrentUserId() {
        try {
            User user = userRepository.findByUsername("demo");
            if (user != null) {
                return user.getId();
            }
        } catch (SQLException e) {
            logger.error("Error getting user ID", e);
        }
        return 1;
    }
    
    /**
     * Simple card stack for switching between login and register
     */
    private static class CardStackPane extends VBox {
        private java.util.Map<String, VBox> cards = new java.util.HashMap<>();
        private String currentCard;
        
        void addCard(String name, VBox card) {
            cards.put(name, card);
            if (currentCard == null) {
                currentCard = name;
                getChildren().add(card);
            }
        }
        
        void showCard(String name) {
            if (cards.containsKey(name) && !name.equals(currentCard)) {
                getChildren().clear();
                getChildren().add(cards.get(name));
                currentCard = name;
            }
        }
    }
}
