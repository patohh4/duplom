# 👤 User Profile & Personalization Guide

## Overview
MindDoc now includes a comprehensive user profile system that allows users to manage their personal information, preferences, and view their account details.

## Features

### 1. **Profile Information**
Users can view and edit their personal information:
- **First Name & Last Name** - User's full name
- **Email** - Contact email address
- **Date of Birth** - For demographic tracking
- **Gender** - User's gender preference
- **Member Since** - Account creation date (read-only)
- **Current Status** - Active/Inactive indicator

### 2. **Profile Management**
- **View Profile** - Click on "👤 Profile" tab to view complete profile
- **Edit Profile** - Click "✏️ Edit Profile" button to enable editing mode
- **Save Changes** - Click "💾 Save Changes" to persist updates
- **Cancel Changes** - Click "❌ Cancel" to discard unsaved changes

### 3. **Preferences**
- **Email Notifications** - Enable/disable email notifications
- **Theme Preference** - Choose between Light/Dark/Auto themes
- **Language** - Select preferred language (English, Українська, Русский)

### 4. **Access Profile**
There are two ways to access the profile:
1. **From Tab Bar** - Click the "👤 Profile" tab (2nd position in tab bar)
2. **From Menu** - Help → My Profile

## User Information Display

The profile panel displays:
```
🧠 User Profile
┌────────────────────────────────┐
│  🧠 [User Avatar]              │
│  John Doe                       │
│  john@example.com              │
│  Member since 2026-04-02       │
│                                │
│  ✅ Active                     │
└────────────────────────────────┘
```

## Edit Mode

### How to Use Edit Mode:
1. Click "✏️ Edit Profile" button
2. All input fields become enabled
3. Modify information as needed:
   - Update name
   - Change date of birth
   - Select gender
   - Add bio/about information
4. Click "💾 Save Changes" to save
5. Success message confirms update

## Database Schema

The user information is stored in the `users` table:

```sql
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    email TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    first_name TEXT,
    last_name TEXT,
    date_of_birth TEXT,
    gender TEXT,
    registration_date TEXT NOT NULL,
    last_login_date TEXT,
    notifications_enabled INTEGER DEFAULT 1
)
```

## Default Demo Account

For testing the profile functionality:
```
Username: demo
Password: demo
Email: demo@mindoc.com
```

After login, you can:
1. View the profile with demo user information
2. Click "✏️ Edit Profile" to modify details
3. Add your name, birth date, etc.
4. Save changes to update the database

## API Reference

### ProfilePanel Class
```java
public class ProfilePanel extends BasePanel {
    
    // Constructor
    public ProfilePanel(int userId, UserRepository userRepository)
    
    // Methods
    public void refresh()  // Refresh profile data from database
    
    // UI Components
    - nameLabel: Current user display name
    - emailLabel: User email address
    - memberSinceLabel: Account creation date
    - statusLabel: Current status
    - firstNameField: Editable first name
    - lastNameField: Editable last name
    - dobPicker: Date of birth picker
    - genderCombo: Gender selection dropdown
    - bioField: User biography/about field
    - notificationsCheckbox: Notification preferences
}
```

### User Model Updates
The User class now includes:
```java
private String password;      // User's password hash
private LocalDate registrationDate;  // Registration date
private String firstName;     // Editable first name
private String lastName;      // Editable last name
private String dateOfBirth;   // Date of birth (editable)
private String gender;        // Gender preference (editable)
```

## Features Overview

| Feature | Status | Notes |
|---------|--------|-------|
| View Profile | ✅ Complete | Full profile display |
| Edit Profile | ✅ Complete | All fields editable |
| Email Notifications | ✅ Complete | Toggle available |
| Theme Preferences | ⚠️ Framework | UI ready, theme switching in v2.1 |
| Language Selection | ⚠️ Framework | UI ready, i18n in v2.2 |
| Profile Avatar | ✅ Complete | Static emoji avatar |
| Account Status | ✅ Complete | Active/Inactive indicator |

## User Experience Flow

```
Login Screen
    ↓
    [Enter credentials]
    ↓
Dashboard (Welcome screen)
    ↓
Click "👤 Profile" Tab
    ↓
View Profile Information
    ├─ See: Name, Email, Status
    ├─ See: Member Since date
    └─ See: All personal info
    ↓
[Optional] Click "✏️ Edit Profile"
    ├─ Enable edit mode
    ├─ Modify fields
    └─ Click "💾 Save Changes"
    ↓
Profile Updated ✅
```

## Known Limitations (v2.0)

- Avatar is static (emoji) - custom upload coming in v2.2
- Theme and language changes don't apply immediately (v2.1)
- Profile image/avatar not yet implemented
- Password change feature not yet available (v2.1)
- Two-factor authentication not yet available (v3.0)

## Future Enhancements (v2.1+)

- [ ] Custom profile avatar upload
- [ ] Password change functionality
- [ ] Email verification
- [ ] Profile privacy settings
- [ ] Account deactivation option
- [ ] Export user data (GDPR compliance)
- [ ] Two-factor authentication (2FA)
- [ ] OAuth integration (Google, Facebook)
- [ ] Profile completion progress indicator

## Troubleshooting

### "Failed to save profile"
- Check database connection
- Ensure all required fields are filled
- Verify that your email is unique in the system

### "Profile information not updating"
- Close and reopen the Profile tab
- Try logging out and logging back in
- Check application logs for errors

### "Edit button not working"
- Ensure you have write permissions to the database
- Verify database file (mindoc.db) is not corrupted
- Check that mindoc.db is not locked by another process

## Support

For issues or feature requests related to profile functionality:
1. Check the logs in application folder
2. Verify database integrity
3. Contact development team with error messages

---

**Last Updated:** April 2, 2026
**Version:** 2.0.0
**Status:** Production Ready ✅
