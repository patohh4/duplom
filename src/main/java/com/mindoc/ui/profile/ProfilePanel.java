package com.mindoc.ui.profile;

import com.mindoc.model.User;
import com.mindoc.repository.UserRepository;
import com.mindoc.ui.common.BasePanel;
import com.mindoc.ui.theme.MindDocTheme;
import com.mindoc.util.I18n;
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
    private Label profileTitle;
    
    private TextField firstNameField;
    private TextField lastNameField;
    private DatePicker dobPicker;
    private ComboBox<String> genderCombo;
    private TextArea bioField;
    
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
        setSpacing(0);
        setPadding(new Insets(0));
        setStyle("-fx-background-color: " + MindDocTheme.BACKGROUND + ";");

        // Header
        VBox headerSection = createHeaderSection();
        getChildren().add(headerSection);

        // Content
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: " + MindDocTheme.BACKGROUND + "; -fx-background: " + MindDocTheme.BACKGROUND + ";");
        VBox contentSection = createContentSection();
        scrollPane.setContent(contentSection);
        VBox.setVgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS);
        getChildren().add(scrollPane);
    }

    private VBox createHeaderSection() {
        VBox header = new VBox(0);

        // Gradient banner
        HBox banner = new HBox();
        banner.setStyle(
            "-fx-background-color: linear-gradient(from 0% 0% to 100% 0%, " +
                MindDocTheme.PRIMARY + ", " + MindDocTheme.SECONDARY + "); " +
            "-fx-padding: 28 32 0 32; " +
            "-fx-effect: dropshadow(three-pass-box, #00000020, 10, 0, 0, 4);"
        );
        banner.setAlignment(Pos.CENTER_LEFT);

        // Profile title (hidden, kept for applyLanguage)
        profileTitle = new Label("👤 User Profile");
        profileTitle.setVisible(false);
        profileTitle.setManaged(false);

        // Avatar circle
        javafx.scene.layout.StackPane avatarBox = new javafx.scene.layout.StackPane();
        avatarBox.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 50; " +
            "-fx-min-width: 80; -fx-min-height: 80; " +
            "-fx-max-width: 80; -fx-max-height: 80; " +
            "-fx-effect: dropshadow(three-pass-box, #00000033, 8, 0, 0, 2);"
        );
        Label avatarLabel = new Label("🧠");
        avatarLabel.setFont(Font.font("System", 38));
        avatarBox.getChildren().add(avatarLabel);

        // User details
        VBox detailsBox = new VBox(4);
        detailsBox.setPadding(new Insets(0, 0, 0, 16));

        nameLabel = new Label(currentUser != null ?
            (currentUser.getFirstName() != null && currentUser.getLastName() != null ?
            currentUser.getFirstName() + " " + currentUser.getLastName() : currentUser.getUsername()) :
            "User");
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        nameLabel.setTextFill(javafx.scene.paint.Color.WHITE);

        emailLabel = new Label(currentUser != null ? currentUser.getEmail() : "");
        emailLabel.setFont(Font.font("Segoe UI", 13));
        emailLabel.setTextFill(javafx.scene.paint.Color.web("#d1fae5"));

        memberSinceLabel = new Label(I18n.t("member_since", "Member since") + " " +
            (currentUser != null && currentUser.getRegistrationDate() != null ?
            currentUser.getRegistrationDate().toString() : "N/A"));
        memberSinceLabel.setFont(Font.font("Segoe UI", 11));
        memberSinceLabel.setTextFill(javafx.scene.paint.Color.web("#a7f3d0"));

        detailsBox.getChildren().addAll(nameLabel, emailLabel, memberSinceLabel);

        HBox.setHgrow(detailsBox, javafx.scene.layout.Priority.ALWAYS);
        banner.getChildren().addAll(avatarBox, detailsBox, profileTitle);

        // Status bar below banner
        HBox statusBox = new HBox(8);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        statusBox.setPadding(new Insets(10, 32, 10, 32));
        statusBox.setStyle(
            "-fx-background-color: " + MindDocTheme.PRIMARY + "22; " +
            "-fx-border-color: " + MindDocTheme.PRIMARY + "33; " +
            "-fx-border-width: 0 0 1 0;"
        );

        Label statusLabelTitle = new Label(I18n.t("current_status", "Current Status:"));
        statusLabelTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        statusLabelTitle.setTextFill(javafx.scene.paint.Color.web(MindDocTheme.TEXT_SECONDARY));

        statusLabel = new Label(I18n.t("active", "✅ Active"));
        statusLabel.setFont(Font.font("Segoe UI", 12));
        statusLabel.setTextFill(javafx.scene.paint.Color.web(MindDocTheme.SUCCESS));

        statusBox.getChildren().addAll(statusLabelTitle, statusLabel);

        header.getChildren().addAll(banner, statusBox);
        return header;
    }
    
    private VBox createContentSection() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: " + MindDocTheme.BACKGROUND + ";");
        
        // Personal Information Section
        VBox personalSection = createPersonalSection();
        content.getChildren().add(personalSection);
        
        // Action buttons
        HBox buttonsBox = createActionButtons();
        content.getChildren().add(buttonsBox);
        
        return content;
    }
    
    private VBox createPersonalSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(22));
        section.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 14; " +
            "-fx-border-radius: 14; " +
            "-fx-effect: dropshadow(three-pass-box, #00000014, 8, 0, 0, 2);"
        );
        
        Label sectionTitle = new Label(I18n.t("personal_info", "📋 Personal Information"));
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        sectionTitle.setTextFill(javafx.scene.paint.Color.web(MindDocTheme.PRIMARY));
        section.getChildren().add(sectionTitle);
        
        // First Name
        VBox firstNameBox = new VBox(5);
        Label firstNameLbl = new Label(I18n.t("first_name", "First Name"));
        firstNameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");
        firstNameField = new TextField();
        firstNameField.setPromptText(I18n.t("first_name", "First Name"));
        firstNameField.setDisable(true);
        firstNameField.setText(currentUser != null && currentUser.getFirstName() != null ? 
            currentUser.getFirstName() : "");
        firstNameBox.getChildren().addAll(firstNameLbl, firstNameField);
        section.getChildren().add(firstNameBox);
        
        // Last Name
        VBox lastNameBox = new VBox(5);
        Label lastNameLbl = new Label(I18n.t("last_name", "Last Name"));
        lastNameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");
        lastNameField = new TextField();
        lastNameField.setPromptText(I18n.t("last_name", "Last Name"));
        lastNameField.setDisable(true);
        lastNameField.setText(currentUser != null && currentUser.getLastName() != null ? 
            currentUser.getLastName() : "");
        lastNameBox.getChildren().addAll(lastNameLbl, lastNameField);
        section.getChildren().add(lastNameBox);
        
        // Date of Birth
        HBox dobBox = new HBox(20);
        VBox dobLabelBox = new VBox(5);
        Label dobLbl = new Label(I18n.t("date_of_birth", "Date of Birth"));
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
        Label genderLbl = new Label(I18n.t("gender", "Gender"));
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
        Label bioLbl = new Label(I18n.t("bio", "About You (Bio)"));
        bioLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");
        bioField = new TextArea();
        bioField.setPromptText(I18n.t("bio", "About You (Bio)"));
        bioField.setWrapText(true);
        bioField.setPrefRowCount(4);
        bioField.setDisable(true);
        bioField.setText("");
        bioBox.getChildren().addAll(bioLbl, bioField);
        section.getChildren().add(bioBox);
        
        return section;
    }
    
    private HBox createActionButtons() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));
        
        Button editButton = new Button(I18n.t("edit_profile", "✏️ Edit Profile"));
        editButton.setStyle(
            "-fx-padding: 12 28; " +
            "-fx-font-size: 13px; " +
            "-fx-background-color: " + MindDocTheme.PRIMARY + "; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 8; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(three-pass-box, #00000026, 4, 0, 0, 2);"
        );

        Button cancelButton = new Button(I18n.t("cancel", "❌ Cancel"));
        cancelButton.setStyle(
            "-fx-padding: 12 28; " +
            "-fx-font-size: 13px; " +
            "-fx-background-color: #9ca3af; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 8; " +
            "-fx-cursor: hand;"
        );
        cancelButton.setDisable(true);

        Button saveButton = new Button(I18n.t("save_changes", "💾 Save Changes"));
        saveButton.setStyle(
            "-fx-padding: 12 28; " +
            "-fx-font-size: 13px; " +
            "-fx-background-color: " + MindDocTheme.SUCCESS + "; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 8; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(three-pass-box, #00000026, 4, 0, 0, 2);"
        );
        saveButton.setDisable(true);
        
        editButton.setOnAction(e -> {
            editMode = !editMode;
            setEditMode(editMode);
            
            if (editMode) {
                editButton.setText(I18n.t("editing", "⏸️ Editing..."));
                editButton.setDisable(true);
                cancelButton.setDisable(false);
                saveButton.setDisable(false);
            } else {
                editButton.setText(I18n.t("edit_profile", "✏️ Edit Profile"));
                editButton.setDisable(false);
                cancelButton.setDisable(true);
                saveButton.setDisable(true);
            }
        });
        
        cancelButton.setOnAction(e -> {
            editMode = false;
            setEditMode(false);
            editButton.setText(I18n.t("edit_profile", "✏️ Edit Profile"));
            editButton.setDisable(false);
            cancelButton.setDisable(true);
            saveButton.setDisable(true);
            refreshUserData();
        });
        
        saveButton.setOnAction(e -> {
            saveProfileChanges();
            editMode = false;
            setEditMode(false);
            editButton.setText(I18n.t("edit_profile", "✏️ Edit Profile"));
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
            
            showSuccessAlert(I18n.t("success", "Success"), I18n.t("profile_updated", "Profile updated successfully!"));
            logger.info("Profile updated for user: {}", currentUser.getUsername());
        } catch (SQLException e) {
            logger.error("Error saving profile", e);
            showErrorAlert(I18n.t("error", "Error"), I18n.t("failed_save_profile", "Failed to save profile: ") + e.getMessage());
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
            memberSinceLabel.setText(I18n.t("member_since", "Member since") + " " + currentUser.getRegistrationDate());
            refreshUserData();
        } catch (SQLException e) {
            logger.error("Error refreshing profile", e);
        }
    }

    public void applyLanguage(String language) {
        if (profileTitle != null) {
            profileTitle.setText(I18n.t("profile", "👤 User Profile"));
        }
        memberSinceLabel.setText(I18n.t("member_since", "Member since") + " " +
            (currentUser != null && currentUser.getRegistrationDate() != null ? currentUser.getRegistrationDate() : "N/A"));
    }
}
