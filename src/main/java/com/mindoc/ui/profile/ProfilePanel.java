package com.mindoc.ui.profile;

import com.mindoc.model.User;
import com.mindoc.repository.UserRepository;
import com.mindoc.ui.common.BasePanel;
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
 * User Profile Panel for viewing and editing profile information
 */
public class ProfilePanel extends BasePanel {
    private static final Logger logger = LoggerFactory.getLogger(ProfilePanel.class);
    
    private int currentUserId;
    private UserRepository userRepository;
    private User currentUser;
    
    private Label nameLabel;
    private Label emailLabel;
    private Label memberSinceLabel;
    private Label statusLabel;
    
    private TextField firstNameField;
    private TextField lastNameField;
    private DatePicker dobPicker;
    private ComboBox<String> genderCombo;
    private TextArea bioField;
    private CheckBox notificationsCheckbox;
    
    private boolean editMode = false;
    
    public ProfilePanel(int userId, UserRepository userRepository) {
        this.currentUserId = userId;
        this.userRepository = userRepository;
        
        try {
            this.currentUser = userRepository.findById(userId);
        } catch (SQLException e) {
            logger.error("Error loading user", e);
        }
        
        initializeUI();
    }
    
    private void initializeUI() {
        setSpacing(15);
        setPadding(new Insets(20));
        
        // Header
        VBox headerSection = createHeaderSection();
        getChildren().add(headerSection);
        
        // Divider
        Separator separator = new Separator();
        getChildren().add(separator);
        
        // Content
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        VBox contentSection = createContentSection();
        scrollPane.setContent(contentSection);
        getChildren().add(scrollPane);
    }
    
    private VBox createHeaderSection() {
        VBox header = new VBox(15);
        header.setPadding(new Insets(20));
        header.setStyle(
            "-fx-border-radius: 10;" +
            "-fx-background-color: #f5f5f5;" +
            "-fx-padding: 20;"
        );
        
        // Profile title
        Label profileTitle = new Label("👤 User Profile");
        profileTitle.setFont(Font.font("System", FontWeight.BOLD, 24));
        profileTitle.setTextFill(javafx.scene.paint.Color.web(MindDocTheme.PRIMARY));
        
        // User info box
        HBox userInfoBox = new HBox(30);
        userInfoBox.setAlignment(Pos.CENTER_LEFT);
        userInfoBox.setPadding(new Insets(15));
        
        // Profile avatar
        VBox avatarBox = new VBox();
        avatarBox.setAlignment(Pos.CENTER);
        avatarBox.setStyle(
            "-fx-border-radius: 50;" +
            "-fx-background-color: " + MindDocTheme.PRIMARY + ";" +
            "-fx-min-width: 80;" +
            "-fx-min-height: 80;"
        );
        Label avatarLabel = new Label("🧠");
        avatarLabel.setFont(Font.font("System", 40));
        avatarBox.getChildren().add(avatarLabel);
        
        // User details
        VBox detailsBox = new VBox(5);
        
        nameLabel = new Label(currentUser != null ? 
            (currentUser.getFirstName() != null && currentUser.getLastName() != null ?
            currentUser.getFirstName() + " " + currentUser.getLastName() : currentUser.getUsername()) :
            "User");
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        nameLabel.setTextFill(javafx.scene.paint.Color.web("#333"));
        
        emailLabel = new Label(currentUser != null ? currentUser.getEmail() : "");
        emailLabel.setFont(Font.font("System", 12));
        emailLabel.setTextFill(javafx.scene.paint.Color.web("#666"));
        
        memberSinceLabel = new Label("Member since " + 
            (currentUser != null && currentUser.getRegistrationDate() != null ?
            currentUser.getRegistrationDate().toString() : "N/A"));
        memberSinceLabel.setFont(Font.font("System", 11));
        memberSinceLabel.setTextFill(javafx.scene.paint.Color.web("#999"));
        
        detailsBox.getChildren().addAll(nameLabel, emailLabel, memberSinceLabel);
        
        userInfoBox.getChildren().addAll(avatarBox, detailsBox);
        
        // Status box
        HBox statusBox = new HBox(10);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        statusBox.setPadding(new Insets(10));
        statusBox.setStyle("-fx-background-color: #e8f5e9; -fx-border-radius: 5;");
        
        Label statusLabelTitle = new Label("Current Status:");
        statusLabelTitle.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        statusLabel = new Label("✅ Active");
        statusLabel.setFont(Font.font("System", 12));
        statusLabel.setTextFill(javafx.scene.paint.Color.web(MindDocTheme.SUCCESS));
        
        statusBox.getChildren().addAll(statusLabelTitle, statusLabel);
        
        header.getChildren().addAll(profileTitle, userInfoBox, statusBox);
        return header;
    }
    
    private VBox createContentSection() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        
        // Personal Information Section
        VBox personalSection = createPersonalSection();
        content.getChildren().add(personalSection);
        
        // Preferences Section
        VBox preferencesSection = createPreferencesSection();
        content.getChildren().add(preferencesSection);
        
        // Action buttons
        HBox buttonsBox = createActionButtons();
        content.getChildren().add(buttonsBox);
        
        return content;
    }
    
    private VBox createPersonalSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(15));
        section.setStyle(
            "-fx-border-radius: 10;" +
            "-fx-border-color: #e0e0e0;" +
            "-fx-border-width: 1;"
        );
        
        Label sectionTitle = new Label("📋 Personal Information");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        sectionTitle.setTextFill(javafx.scene.paint.Color.web(MindDocTheme.PRIMARY));
        section.getChildren().add(sectionTitle);
        
        // First Name
        VBox firstNameBox = new VBox(5);
        Label firstNameLbl = new Label("First Name");
        firstNameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");
        firstNameField = new TextField();
        firstNameField.setPromptText("Enter your first name");
        firstNameField.setDisable(true);
        firstNameField.setText(currentUser != null && currentUser.getFirstName() != null ? 
            currentUser.getFirstName() : "");
        firstNameBox.getChildren().addAll(firstNameLbl, firstNameField);
        section.getChildren().add(firstNameBox);
        
        // Last Name
        VBox lastNameBox = new VBox(5);
        Label lastNameLbl = new Label("Last Name");
        lastNameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");
        lastNameField = new TextField();
        lastNameField.setPromptText("Enter your last name");
        lastNameField.setDisable(true);
        lastNameField.setText(currentUser != null && currentUser.getLastName() != null ? 
            currentUser.getLastName() : "");
        lastNameBox.getChildren().addAll(lastNameLbl, lastNameField);
        section.getChildren().add(lastNameBox);
        
        // Date of Birth
        HBox dobBox = new HBox(20);
        VBox dobLabelBox = new VBox(5);
        Label dobLbl = new Label("Date of Birth");
        dobLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");
        dobPicker = new DatePicker();
        dobPicker.setDisable(true);
        if (currentUser != null && currentUser.getDateOfBirth() != null) {
            try {
                dobPicker.setValue(LocalDate.parse(currentUser.getDateOfBirth()));
            } catch (Exception e) {
                // Invalid date format
            }
        }
        dobLabelBox.getChildren().addAll(dobLbl, dobPicker);
        dobLabelBox.setMaxWidth(250);
        
        VBox genderBox = new VBox(5);
        Label genderLbl = new Label("Gender");
        genderLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");
        genderCombo = new ComboBox<>();
        genderCombo.getItems().addAll("Male", "Female", "Other", "Prefer not to say");
        genderCombo.setDisable(true);
        if (currentUser != null && currentUser.getGender() != null) {
            genderCombo.setValue(currentUser.getGender());
        }
        genderBox.getChildren().addAll(genderLbl, genderCombo);
        genderBox.setMaxWidth(250);
        
        dobBox.getChildren().addAll(dobLabelBox, genderBox);
        section.getChildren().add(dobBox);
        
        // Bio
        VBox bioBox = new VBox(5);
        Label bioLbl = new Label("About You (Bio)");
        bioLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");
        bioField = new TextArea();
        bioField.setPromptText("Tell us about yourself...");
        bioField.setWrapText(true);
        bioField.setPrefRowCount(4);
        bioField.setDisable(true);
        bioField.setText("");
        bioBox.getChildren().addAll(bioLbl, bioField);
        section.getChildren().add(bioBox);
        
        return section;
    }
    
    private VBox createPreferencesSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(15));
        section.setStyle(
            "-fx-border-radius: 10;" +
            "-fx-border-color: #e0e0e0;" +
            "-fx-border-width: 1;"
        );
        
        Label sectionTitle = new Label("⚙️ Preferences");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        sectionTitle.setTextFill(javafx.scene.paint.Color.web(MindDocTheme.PRIMARY));
        section.getChildren().add(sectionTitle);
        
        // Notifications
        notificationsCheckbox = new CheckBox("Enable Email Notifications");
        notificationsCheckbox.setFont(Font.font("System", 12));
        notificationsCheckbox.setDisable(true);
        if (currentUser != null) {
            notificationsCheckbox.setSelected(true);
        }
        section.getChildren().add(notificationsCheckbox);
        
        // Theme preference
        HBox themeBox = new HBox(10);
        Label themeLbl = new Label("Theme:");
        themeLbl.setStyle("-fx-font-weight: bold;");
        ComboBox<String> themeCombo = new ComboBox<>();
        themeCombo.getItems().addAll("Light", "Dark", "Auto");
        themeCombo.setValue("Light");
        themeCombo.setDisable(true);
        themeBox.getChildren().addAll(themeLbl, themeCombo);
        section.getChildren().add(themeBox);
        
        // Language preference
        HBox languageBox = new HBox(10);
        Label languageLbl = new Label("Language:");
        languageLbl.setStyle("-fx-font-weight: bold;");
        ComboBox<String> languageCombo = new ComboBox<>();
        languageCombo.getItems().addAll("English", "Українська", "Русский");
        languageCombo.setValue("English");
        languageCombo.setDisable(true);
        languageBox.getChildren().addAll(languageLbl, languageCombo);
        section.getChildren().add(languageBox);
        
        return section;
    }
    
    private HBox createActionButtons() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));
        
        Button editButton = new Button("✏️ Edit Profile");
        editButton.setStyle(
            "-fx-padding: 12px 30px;" +
            "-fx-font-size: 13px;" +
            "-fx-background-color: " + MindDocTheme.PRIMARY + ";" +
            "-fx-text-fill: white;" +
            "-fx-border-radius: 5;" +
            "-fx-cursor: hand;"
        );
        
        Button cancelButton = new Button("❌ Cancel");
        cancelButton.setStyle(
            "-fx-padding: 12px 30px;" +
            "-fx-font-size: 13px;" +
            "-fx-background-color: #999;" +
            "-fx-text-fill: white;" +
            "-fx-border-radius: 5;" +
            "-fx-cursor: hand;"
        );
        cancelButton.setDisable(true);
        
        Button saveButton = new Button("💾 Save Changes");
        saveButton.setStyle(
            "-fx-padding: 12px 30px;" +
            "-fx-font-size: 13px;" +
            "-fx-background-color: " + MindDocTheme.SUCCESS + ";" +
            "-fx-text-fill: white;" +
            "-fx-border-radius: 5;" +
            "-fx-cursor: hand;"
        );
        saveButton.setDisable(true);
        
        editButton.setOnAction(e -> {
            editMode = !editMode;
            setEditMode(editMode);
            
            if (editMode) {
                editButton.setText("⏸️ Editing...");
                editButton.setDisable(true);
                cancelButton.setDisable(false);
                saveButton.setDisable(false);
            } else {
                editButton.setText("✏️ Edit Profile");
                editButton.setDisable(false);
                cancelButton.setDisable(true);
                saveButton.setDisable(true);
            }
        });
        
        cancelButton.setOnAction(e -> {
            editMode = false;
            setEditMode(false);
            editButton.setText("✏️ Edit Profile");
            editButton.setDisable(false);
            cancelButton.setDisable(true);
            saveButton.setDisable(true);
            refreshUserData();
        });
        
        saveButton.setOnAction(e -> {
            saveProfileChanges();
            editMode = false;
            setEditMode(false);
            editButton.setText("✏️ Edit Profile");
            editButton.setDisable(false);
            cancelButton.setDisable(true);
            saveButton.setDisable(true);
        });
        
        buttonBox.getChildren().addAll(editButton, cancelButton, saveButton);
        return buttonBox;
    }
    
    private void setEditMode(boolean enabled) {
        firstNameField.setDisable(!enabled);
        lastNameField.setDisable(!enabled);
        dobPicker.setDisable(!enabled);
        genderCombo.setDisable(!enabled);
        bioField.setDisable(!enabled);
        notificationsCheckbox.setDisable(!enabled);
    }
    
    private void refreshUserData() {
        try {
            currentUser = userRepository.findById(currentUserId);
            firstNameField.setText(currentUser.getFirstName() != null ? currentUser.getFirstName() : "");
            lastNameField.setText(currentUser.getLastName() != null ? currentUser.getLastName() : "");
            nameLabel.setText(currentUser.getFirstName() != null && currentUser.getLastName() != null ?
                currentUser.getFirstName() + " " + currentUser.getLastName() : currentUser.getUsername());
        } catch (SQLException e) {
            logger.error("Error refreshing user data", e);
        }
    }
    
    private void saveProfileChanges() {
        try {
            currentUser.setFirstName(firstNameField.getText());
            currentUser.setLastName(lastNameField.getText());
            currentUser.setDateOfBirth(dobPicker.getValue() != null ? dobPicker.getValue().toString() : null);
            currentUser.setGender(genderCombo.getValue());
            
            userRepository.update(currentUser);
            
            // Update display
            nameLabel.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
            
            showSuccessAlert("Success", "Profile updated successfully!");
            logger.info("Profile updated for user: {}", currentUser.getUsername());
        } catch (SQLException e) {
            logger.error("Error saving profile", e);
            showErrorAlert("Error", "Failed to save profile: " + e.getMessage());
        }
    }
    
    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public void refresh() {
        try {
            currentUser = userRepository.findById(currentUserId);
            nameLabel.setText(currentUser.getFirstName() != null && currentUser.getLastName() != null ?
                currentUser.getFirstName() + " " + currentUser.getLastName() : currentUser.getUsername());
            emailLabel.setText(currentUser.getEmail());
            memberSinceLabel.setText("Member since " + currentUser.getRegistrationDate());
            refreshUserData();
        } catch (SQLException e) {
            logger.error("Error refreshing profile", e);
        }
    }
}
