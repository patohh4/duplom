package com.mindoc.ui.profile;

import com.mindoc.model.User;
import com.mindoc.repository.UserRepository;
import com.mindoc.ui.common.BasePanel;
import com.mindoc.ui.theme.MindDocTheme;
import com.mindoc.util.I18n;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;

public class ProfilePanel extends BasePanel {
    private static final Logger logger = LoggerFactory.getLogger(ProfilePanel.class);

    private int currentUserId;
    private UserRepository userRepository;
    private User currentUser;

    // header labels
    private Label nameLabel;
    private Label usernameLabel;
    private Label emailLabel;
    private Label memberSinceLabel;
    private Label statusLabel;
    private Label profileTitle; // hidden, for applyLanguage
    private Label avatarInitials;

    // form fields
    private TextField firstNameField;
    private TextField lastNameField;
    private DatePicker dobPicker;
    private ComboBox<String> genderCombo;
    private TextArea bioField;

    // action buttons (kept as fields so we can update text)
    private Button editButton;
    private Button cancelButton;
    private Button saveButton;

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

    // ── Layout ───────────────────────────────────────────────────────────────

    private void initializeUI() {
        setSpacing(0);
        setPadding(new Insets(0));
        setStyle("-fx-background-color: " + MindDocTheme.BACKGROUND + ";");

        getChildren().addAll(buildHeader(), buildScrollContent());
    }

    /** Green gradient banner + thin status bar */
    private VBox buildHeader() {
        VBox header = new VBox(0);

        // ── Banner ──────────────────────────────────────────────────────────
        HBox banner = new HBox(20);
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.setPadding(new Insets(28, 36, 28, 36));
        banner.setStyle(
            "-fx-background-color: linear-gradient(from 0% 0% to 100% 0%, " +
                MindDocTheme.PRIMARY + ", " + MindDocTheme.SECONDARY + ");"
        );

        // Avatar circle with initials
        StackPane avatar = new StackPane();
        avatar.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 50; " +
            "-fx-min-width: 80; -fx-min-height: 80; " +
            "-fx-max-width: 80; -fx-max-height: 80; " +
            "-fx-effect: dropshadow(three-pass-box, #00000033, 10, 0, 0, 3);"
        );
        avatarInitials = new Label(buildInitials());
        avatarInitials.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        avatarInitials.setTextFill(Color.web(MindDocTheme.PRIMARY));
        avatar.getChildren().add(avatarInitials);

        // Text block
        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);

        nameLabel = new Label(buildDisplayName());
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        nameLabel.setTextFill(Color.WHITE);

        usernameLabel = new Label(currentUser != null ? "@" + currentUser.getUsername() : "");
        usernameLabel.setFont(Font.font("Segoe UI", 13));
        usernameLabel.setTextFill(Color.web("#d1fae5"));

        emailLabel = new Label(currentUser != null && currentUser.getEmail() != null
            ? currentUser.getEmail() : "");
        emailLabel.setFont(Font.font("Segoe UI", 12));
        emailLabel.setTextFill(Color.web("#a7f3d0"));

        memberSinceLabel = new Label(memberSinceText());
        memberSinceLabel.setFont(Font.font("Segoe UI", 11));
        memberSinceLabel.setTextFill(Color.web("#6ee7b7"));

        info.getChildren().addAll(nameLabel, usernameLabel, emailLabel, memberSinceLabel);

        // Decorative emoji (right side, very faint)
        Label deco = new Label("🌿");
        deco.setFont(Font.font("System", 80));
        deco.setOpacity(0.12);

        // hidden label kept for applyLanguage
        profileTitle = new Label("👤 User Profile");
        profileTitle.setVisible(false);
        profileTitle.setManaged(false);

        banner.getChildren().addAll(avatar, info, deco, profileTitle);

        // ── Status bar ──────────────────────────────────────────────────────
        HBox statusBar = new HBox(8);
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setPadding(new Insets(9, 36, 9, 36));
        statusBar.setStyle("-fx-background-color: #d1fae5;");

        Label statusIcon   = new Label("✅");
        Label statusKey    = new Label(I18n.t("current_status", "Account Status:"));
        statusKey.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        statusKey.setTextFill(Color.web("#065f46"));

        statusLabel = new Label(I18n.t("active", "Active"));
        statusLabel.setFont(Font.font("Segoe UI", 12));
        statusLabel.setTextFill(Color.web(MindDocTheme.SUCCESS));

        statusBar.getChildren().addAll(statusIcon, statusKey, statusLabel);

        header.getChildren().addAll(banner, statusBar);
        return header;
    }

    /** Scrollable body */
    private ScrollPane buildScrollContent() {
        VBox body = new VBox(0);
        body.setStyle("-fx-background-color: " + MindDocTheme.BACKGROUND + ";");
        body.setPadding(new Insets(28, 36, 36, 36));

        body.getChildren().add(buildPersonalCard());

        ScrollPane sp = new ScrollPane(body);
        sp.setFitToWidth(true);
        sp.setStyle(
            "-fx-background-color: " + MindDocTheme.BACKGROUND + "; " +
            "-fx-background: " + MindDocTheme.BACKGROUND + "; " +
            "-fx-padding: 0;"
        );
        VBox.setVgrow(sp, Priority.ALWAYS);
        return sp;
    }

    /** White card: accent bar + form fields + action buttons */
    private VBox buildPersonalCard() {
        // Top accent bar
        Region bar = new Region();
        bar.setPrefHeight(4);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setStyle("-fx-background-color: " + MindDocTheme.PRIMARY +
            "; -fx-background-radius: 16 16 0 0;");

        // Card body
        VBox body = new VBox(18);
        body.setPadding(new Insets(22, 28, 10, 28));

        Label title = new Label("📋 " + I18n.t("personal_info", "Personal Information"));
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        title.setTextFill(Color.web(MindDocTheme.TEXT_PRIMARY));

        // Row 1: First Name | Last Name
        HBox nameRow = new HBox(16);
        VBox fnBox = buildFieldBox(I18n.t("first_name", "First Name"));
        firstNameField = (TextField) fnBox.getChildren().get(1);
        firstNameField.setText(currentUser != null && currentUser.getFirstName() != null
            ? currentUser.getFirstName() : "");
        firstNameField.setDisable(true);

        VBox lnBox = buildFieldBox(I18n.t("last_name", "Last Name"));
        lastNameField = (TextField) lnBox.getChildren().get(1);
        lastNameField.setText(currentUser != null && currentUser.getLastName() != null
            ? currentUser.getLastName() : "");
        lastNameField.setDisable(true);

        HBox.setHgrow(fnBox, Priority.ALWAYS);
        HBox.setHgrow(lnBox, Priority.ALWAYS);
        nameRow.getChildren().addAll(fnBox, lnBox);

        // Row 2: Date of Birth | Gender
        HBox dobGenderRow = new HBox(16);

        VBox dobBox = new VBox(6);
        Label dobLbl = fieldLabel(I18n.t("date_of_birth", "Date of Birth"));
        dobPicker = new DatePicker();
        dobPicker.setMaxWidth(Double.MAX_VALUE);
        dobPicker.setDisable(true);
        if (currentUser != null && currentUser.getDateOfBirth() != null) {
            try { dobPicker.setValue(LocalDate.parse(currentUser.getDateOfBirth())); }
            catch (Exception ignored) {}
        }
        dobBox.getChildren().addAll(dobLbl, dobPicker);
        HBox.setHgrow(dobBox, Priority.ALWAYS);

        VBox genBox = new VBox(6);
        Label genLbl = fieldLabel(I18n.t("gender", "Gender"));
        genderCombo = new ComboBox<>();
        genderCombo.getItems().addAll("Male", "Female", "Other", "Prefer not to say");
        genderCombo.setMaxWidth(Double.MAX_VALUE);
        genderCombo.setDisable(true);
        if (currentUser != null && currentUser.getGender() != null)
            genderCombo.setValue(currentUser.getGender());
        genBox.getChildren().addAll(genLbl, genderCombo);
        HBox.setHgrow(genBox, Priority.ALWAYS);

        dobGenderRow.getChildren().addAll(dobBox, genBox);

        // Row 3: Bio
        VBox bioBox = new VBox(6);
        Label bioLbl = fieldLabel(I18n.t("bio", "About You (Bio)"));
        bioField = new TextArea();
        bioField.setPromptText(I18n.t("bio_placeholder", "Tell us something about yourself…"));
        bioField.setWrapText(true);
        bioField.setPrefRowCount(3);
        bioField.setDisable(true);
        bioBox.getChildren().addAll(bioLbl, bioField);

        body.getChildren().addAll(title, nameRow, dobGenderRow, bioBox);

        // Divider + buttons row inside the card
        Separator sep = new Separator();
        sep.setPadding(new Insets(6, 0, 0, 0));

        HBox buttons = buildButtons();
        buttons.setPadding(new Insets(16, 28, 24, 28));

        VBox card = new VBox(0);
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 16; " +
            "-fx-border-radius: 16; " +
            "-fx-effect: dropshadow(three-pass-box, #00000014, 10, 0, 0, 3);"
        );
        card.getChildren().addAll(bar, body, sep, buttons);
        return card;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /** Label + TextField in a VBox */
    private VBox buildFieldBox(String labelText) {
        VBox box = new VBox(6);
        box.getChildren().addAll(fieldLabel(labelText), buildTextField(labelText));
        return box;
    }

    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        l.setTextFill(Color.web(MindDocTheme.TEXT_SECONDARY));
        return l;
    }

    private TextField buildTextField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setMaxWidth(Double.MAX_VALUE);
        return tf;
    }

    private String buildInitials() {
        if (currentUser == null) return "?";
        String fn = currentUser.getFirstName();
        String ln = currentUser.getLastName();
        if (fn != null && !fn.isBlank() && ln != null && !ln.isBlank())
            return (fn.substring(0, 1) + ln.substring(0, 1)).toUpperCase();
        if (fn != null && !fn.isBlank())
            return fn.substring(0, 1).toUpperCase();
        return currentUser.getUsername().substring(0, 1).toUpperCase();
    }

    private String buildDisplayName() {
        if (currentUser == null) return "User";
        String fn = currentUser.getFirstName();
        String ln = currentUser.getLastName();
        if (fn != null && !fn.isBlank() && ln != null && !ln.isBlank())
            return fn + " " + ln;
        return currentUser.getUsername();
    }

    private String memberSinceText() {
        return I18n.t("member_since", "Member since") + " " +
            (currentUser != null && currentUser.getRegistrationDate() != null
                ? currentUser.getRegistrationDate() : "N/A");
    }

    // ── Action Buttons ───────────────────────────────────────────────────────

    private HBox buildButtons() {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);

        editButton   = styledBtn(I18n.t("edit_profile",  "✏️  Edit Profile"),  MindDocTheme.PRIMARY);
        cancelButton = styledBtn(I18n.t("cancel",         "✕  Cancel"),         "#9ca3af");
        saveButton   = styledBtn(I18n.t("save_changes",   "💾  Save Changes"),  MindDocTheme.SUCCESS);

        cancelButton.setDisable(true);
        saveButton.setDisable(true);

        editButton.setOnAction(e -> enterEditMode());
        cancelButton.setOnAction(e -> cancelEdit());
        saveButton.setOnAction(e -> commitSave());

        box.getChildren().addAll(editButton, cancelButton, saveButton);
        return box;
    }

    private Button styledBtn(String text, String bg) {
        Button b = new Button(text);
        b.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        b.setStyle(
            "-fx-background-color: " + bg + "; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 9; " +
            "-fx-padding: 11 26; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(three-pass-box, #00000022, 4, 0, 0, 2);"
        );
        return b;
    }

    private void enterEditMode() {
        editMode = true;
        setEditMode(true);
        editButton.setText(I18n.t("editing", "✏️  Editing…"));
        editButton.setDisable(true);
        cancelButton.setDisable(false);
        saveButton.setDisable(false);
    }

    private void cancelEdit() {
        editMode = false;
        setEditMode(false);
        editButton.setText(I18n.t("edit_profile", "✏️  Edit Profile"));
        editButton.setDisable(false);
        cancelButton.setDisable(true);
        saveButton.setDisable(true);
        refreshUserData();
    }

    private void commitSave() {
        saveProfileChanges();
        editMode = false;
        setEditMode(false);
        editButton.setText(I18n.t("edit_profile", "✏️  Edit Profile"));
        editButton.setDisable(false);
        cancelButton.setDisable(true);
        saveButton.setDisable(true);
    }

    private void setEditMode(boolean enabled) {
        firstNameField.setDisable(!enabled);
        lastNameField.setDisable(!enabled);
        dobPicker.setDisable(!enabled);
        genderCombo.setDisable(!enabled);
        bioField.setDisable(!enabled);
    }

    // ── Data ─────────────────────────────────────────────────────────────────

    private void refreshUserData() {
        try {
            currentUser = userRepository.findById(currentUserId);
            firstNameField.setText(currentUser.getFirstName() != null ? currentUser.getFirstName() : "");
            lastNameField.setText(currentUser.getLastName() != null ? currentUser.getLastName() : "");
            updateHeaderLabels();
        } catch (SQLException e) {
            logger.error("Error refreshing user data", e);
        }
    }

    private void saveProfileChanges() {
        try {
            currentUser.setFirstName(firstNameField.getText().trim());
            currentUser.setLastName(lastNameField.getText().trim());
            currentUser.setDateOfBirth(dobPicker.getValue() != null
                ? dobPicker.getValue().toString() : null);
            currentUser.setGender(genderCombo.getValue());
            userRepository.update(currentUser);
            updateHeaderLabels();
            showSuccessAlert(I18n.t("success", "Success"),
                I18n.t("profile_updated", "Profile updated successfully!"));
            logger.info("Profile updated for user: {}", currentUser.getUsername());
        } catch (SQLException e) {
            logger.error("Error saving profile", e);
            showErrorAlert(I18n.t("error", "Error"),
                I18n.t("failed_save_profile", "Failed to save profile: ") + e.getMessage());
        }
    }

    private void updateHeaderLabels() {
        nameLabel.setText(buildDisplayName());
        avatarInitials.setText(buildInitials());
        if (currentUser != null) {
            emailLabel.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
        }
        memberSinceLabel.setText(memberSinceText());
    }

    @Override
    public void refresh() {
        try {
            currentUser = userRepository.findById(currentUserId);
            updateHeaderLabels();
            refreshUserData();
        } catch (SQLException e) {
            logger.error("Error refreshing profile", e);
        }
    }

    public void applyLanguage(String language) {
        memberSinceLabel.setText(memberSinceText());
    }

    private void showSuccessAlert(String title, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(message);
        a.showAndWait();
    }

    private void showErrorAlert(String title, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(message);
        a.showAndWait();
    }
}
