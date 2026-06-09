package com.mindoc.ui.auth;

import com.mindoc.MindDocApp;
import com.mindoc.database.DatabaseManager;
import com.mindoc.model.User;
import com.mindoc.repository.UserRepository;
import com.mindoc.ui.theme.MindDocTheme;
import com.mindoc.util.I18n;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Login and Registration Panel — redesigned
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
        // Full-screen green gradient background
        setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, " +
                        MindDocTheme.PRIMARY + ", " + MindDocTheme.PRIMARY_DARK + ");"
        );
        setAlignment(Pos.CENTER);
        setPadding(new Insets(40));

        // White card
        VBox card = new VBox(24);
        card.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 20; " +
                        "-fx-padding: 48 40 40 40; " +
                        "-fx-effect: dropshadow(three-pass-box, #00000033, 24, 0, 0, 8);"
        );
        card.setMaxWidth(460);
        card.setAlignment(Pos.TOP_CENTER);

        // Logo circle + title
        card.getChildren().add(createHeader());

        // Card stack (login / register forms)
        cardStack = new CardStackPane();
        cardStack.addCard("Login", createLoginCard());
        cardStack.addCard("Register", createRegisterCard());
        card.getChildren().add(cardStack);

        // Login / Register toggle buttons
        card.getChildren().add(createToggleButtons());

        // Demo hint
        Label demoLabel = new Label(
                I18n.t("demo_creds", "📝 Demo credentials:\nUsername: demo | Password: demo\nOr create a new account")
        );
        demoLabel.setStyle(
                "-fx-text-fill: " + MindDocTheme.TEXT_MUTED + "; " +
                        "-fx-font-size: 11px;"
        );
        demoLabel.setTextAlignment(TextAlignment.CENTER);
        demoLabel.setWrapText(true);
        card.getChildren().add(demoLabel);

        getChildren().add(card);
    }

    // ── Header: coloured circle with brain emoji + app name ──────────────────

    private VBox createHeader() {
        VBox header = new VBox(14);
        header.setAlignment(Pos.CENTER);

        // Green circle with brain emoji
        StackPane logoCircle = new StackPane();
        Circle circle = new Circle(40);
        circle.setFill(Color.web(MindDocTheme.PRIMARY));

        Label brainEmoji = new Label("🧠");
        brainEmoji.setFont(Font.font("System", 34));

        logoCircle.getChildren().addAll(circle, brainEmoji);

        Label titleLabel = new Label("MindDoc");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.web(MindDocTheme.TEXT_PRIMARY));

        Label subtitleLabel = new Label(I18n.t("subtitle", "Your Mental Health Companion"));
        subtitleLabel.setFont(Font.font("Segoe UI", 13));
        subtitleLabel.setTextFill(Color.web(MindDocTheme.TEXT_SECONDARY));

        header.getChildren().addAll(logoCircle, titleLabel, subtitleLabel);
        return header;
    }

    // ── Login form ────────────────────────────────────────────────────────────

    private VBox createLoginCard() {
        VBox card = new VBox(14);

        TextField usernameField = new TextField();
        usernameField.setPromptText(I18n.t("username", "Username"));
        usernameField.setStyle(inputStyle());

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText(I18n.t("password", "Password"));
        passwordField.setStyle(inputStyle());

        CheckBox rememberMe = new CheckBox(I18n.t("remember_me", "Remember me"));
        rememberMe.setStyle("-fx-font-size: 12px; -fx-text-fill: " + MindDocTheme.TEXT_SECONDARY + ";");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: " + MindDocTheme.DANGER + "; -fx-font-size: 12px;");
        errorLabel.setWrapText(true);

        Button loginButton = new Button(I18n.t("login", "Login"));
        loginButton.setPrefWidth(Double.MAX_VALUE);
        loginButton.setStyle(primaryButtonStyle());

        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText(I18n.t("please_fill", "Please fill in all fields"));
                return;
            }
            try {
                User user = userRepository.findByUsername(username);
                if (user != null && user.getPassword().equals(password)) {
                    logger.info("User logged in: {}", username);
                    app.setCurrentUser(user.getId(), username);
                    onLoginSuccess.run();
                } else {
                    errorLabel.setText(I18n.t("invalid_credentials", "Invalid username or password"));
                }
            } catch (SQLException ex) {
                logger.error("Login error", ex);
                errorLabel.setText("Database error: " + ex.getMessage());
            }
        });

        card.getChildren().addAll(usernameField, passwordField, rememberMe, errorLabel, loginButton);
        return card;
    }

    // ── Register form ─────────────────────────────────────────────────────────

    private VBox createRegisterCard() {
        VBox card = new VBox(14);

        TextField usernameField = new TextField();
        usernameField.setPromptText(I18n.t("username", "Username"));
        usernameField.setStyle(inputStyle());

        TextField emailField = new TextField();
        emailField.setPromptText(I18n.t("email", "Email"));
        emailField.setStyle(inputStyle());

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText(I18n.t("password", "Password"));
        passwordField.setStyle(inputStyle());

        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText(I18n.t("confirm_password", "Confirm Password"));
        confirmField.setStyle(inputStyle());

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: " + MindDocTheme.DANGER + "; -fx-font-size: 12px;");
        errorLabel.setWrapText(true);

        Label successLabel = new Label();
        successLabel.setStyle("-fx-text-fill: " + MindDocTheme.SUCCESS + "; -fx-font-size: 12px;");
        successLabel.setWrapText(true);

        Button registerButton = new Button(I18n.t("create_account", "Create Account"));
        registerButton.setPrefWidth(Double.MAX_VALUE);
        registerButton.setStyle(primaryButtonStyle());

        registerButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String email    = emailField.getText().trim();
            String password = passwordField.getText().trim();
            String confirm  = confirmField.getText().trim();

            errorLabel.setText("");
            successLabel.setText("");

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                errorLabel.setText(I18n.t("please_fill", "Please fill in all fields"));
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
                usernameField.clear(); emailField.clear();
                passwordField.clear(); confirmField.clear();
            } catch (SQLException ex) {
                logger.error("Registration error", ex);
                errorLabel.setText("Registration failed: " + ex.getMessage());
            }
        });

        card.getChildren().addAll(
                usernameField, emailField, passwordField, confirmField,
                errorLabel, successLabel, registerButton
        );
        return card;
    }

    // ── Toggle buttons (Login / Register) ────────────────────────────────────

    private HBox createToggleButtons() {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER);

        Button loginBtn    = new Button(I18n.t("login", "Login"));
        Button registerBtn = new Button(I18n.t("register", "Register"));

        loginBtn.setStyle(toggleActive());
        registerBtn.setStyle(toggleInactive());

        loginBtn.setOnAction(e -> {
            cardStack.showCard("Login");
            loginBtn.setStyle(toggleActive());
            registerBtn.setStyle(toggleInactive());
        });
        registerBtn.setOnAction(e -> {
            cardStack.showCard("Register");
            loginBtn.setStyle(toggleInactive());
            registerBtn.setStyle(toggleActive());
        });

        box.getChildren().addAll(loginBtn, registerBtn);
        return box;
    }

    // ── Style helpers ─────────────────────────────────────────────────────────

    private String inputStyle() {
        return  "-fx-padding: 12 14; " +
                "-fx-font-size: 13px; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8; " +
                "-fx-border-color: " + MindDocTheme.BORDER + "; " +
                "-fx-border-width: 1; " +
                "-fx-background-color: white; " +
                "-fx-text-fill: " + MindDocTheme.TEXT_PRIMARY + ";";
    }

    private String primaryButtonStyle() {
        return  "-fx-padding: 13 0; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-color: " + MindDocTheme.PRIMARY + "; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 8; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(three-pass-box, #00000026, 6, 0, 0, 3);";
    }

    private String toggleActive() {
        return  "-fx-padding: 8 24; " +
                "-fx-font-size: 13px; " +
                "-fx-background-color: " + MindDocTheme.PRIMARY + "; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 20; " +
                "-fx-cursor: hand;";
    }

    private String toggleInactive() {
        return  "-fx-padding: 8 24; " +
                "-fx-font-size: 13px; " +
                "-fx-background-color: transparent; " +
                "-fx-text-fill: " + MindDocTheme.PRIMARY + "; " +
                "-fx-border-color: " + MindDocTheme.PRIMARY + "; " +
                "-fx-border-width: 1.5; " +
                "-fx-border-radius: 20; " +
                "-fx-background-radius: 20; " +
                "-fx-cursor: hand;";
    }

    public int getCurrentUserId() {
        try {
            User user = userRepository.findByUsername("demo");
            if (user != null) return user.getId();
        } catch (SQLException e) {
            logger.error("Error getting user ID", e);
        }
        return 1;
    }

    // ── Card stack ────────────────────────────────────────────────────────────

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
