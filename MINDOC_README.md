# 🧠 MindDoc - Mental Health Support Application

Professional desktop application for mental health tracking, similar to MindDoc/MoodPath, but designed for PC.

## Features

### 📊 Dashboard
- Daily mood overview
- Weekly statistics and trends
- Current streak tracking
- Quick tips and recommendations

### 😊 Mood Tracking
- Intuitive mood slider (1-10 scale)
- Visual emoji indicators
- Context tracking (what triggered the mood)
- Symptom selection
- Detailed notes with rich text support

### 🩺 Symptom Tracking
- Monitor specific mental health symptoms
- Track severity levels
- Symptom categories:
  - Depression
  - Anxiety
  - Stress
  - Sleep issues
- Historical symptom patterns

### 📚 CBT Learning
- Evidence-based Cognitive Behavioral Therapy courses
- Progressive difficulty levels (Beginner, Intermediate, Advanced)
- Course categories:
  - Understanding mental health conditions
  - Coping strategies
  - Mindfulness and relaxation
  - Sleep improvement
  - Stress management

### 💪 Exercises & Coping Strategies
- Breathing techniques (Box Breathing)
- Grounding exercises (5-4-3-2-1 technique)
- Progressive muscle relaxation
- Thought record exercises
- Gratitude practices
- Meditation guides

### 📊 Analytics & Reports
- Detailed mood graphs over time
- Symptom severity trends
- Progress tracking
- Export reports to PDF
- Share data with healthcare providers

### 🎯 Personalized Recommendations
- AI-powered suggestions based on mood patterns
- Course recommendations
- Exercise suggestions
- Assessment recommendations

## System Requirements

- **Java**: 21 LTS or higher
- **JavaFX**: 21.0.1
- **OS**: Windows, macOS, Linux
- **RAM**: 2GB minimum, 4GB recommended
- **Disk**: 100MB for application and database

## Technology Stack

- **Language**: Java 21
- **UI Framework**: JavaFX 21.0.1
- **Database**: SQLite
- **Build System**: Maven
- **Logging**: SLF4J with Logback

## Project Structure

```
src/main/java/com/mindoc/
├── MindDocApp.java                 # Main application entry point
├── database/
│   └── DatabaseManager.java        # Database initialization and connection
├── model/                          # Data models
│   ├── User.java
│   ├── MoodEntry.java
│   ├── Symptom.java
│   ├── SymptomLog.java
│   ├── CBTCourse.java
│   ├── Exercise.java
│   ├── Assessment.java
│   └── Recommendation.java
├── repository/                     # Data access layer
│   ├── UserRepository.java
│   ├── MoodEntryRepository.java
│   ├── SymptomRepository.java
│   ├── CBTCourseRepository.java
│   ├── ExerciseRepository.java
│   └── AssessmentRepository.java
├── service/                        # Business logic
│   ├── MindDocService.java
│   ├── AnalyticsService.java
│   └── RecommendationEngine.java
├── ui/                             # User interface components
│   ├── MindDocApp.java
│   ├── theme/
│   │   └── MindDocTheme.java      # Application theme and colors
│   ├── common/
│   │   └── BasePanel.java         # Base class for all panels
│   ├── dashboard/
│   │   └── DashboardPanel.java
│   ├── moodtracking/
│   │   └── MoodTrackingPanel.java
│   ├── symptoms/
│   │   └── SymptomTrackerPanel.java
│   ├── learning/
│   │   ├── CoursesPanel.java
│   │   └── ExercisesPanel.java
│   ├── analytics/
│   │   └── AnalyticsPanel.java
│   └── common/
│       ├── MoodChart.java
│       └── ProgressIndicator.java
└── util/
    ├── DateUtils.java
    └── ColorUtils.java
```

## Building

### Prerequisites
- JDK 21 installed
- Maven 3.6+ installed

### Build Steps

```bash
# Navigate to project directory
cd /path/to/mindoc

# Build the application
mvn clean package

# Run the application
mvn javafx:run

# Or run the JAR directly
java -jar target/mood-tracker-app-1.0.0.jar
```

## Usage

### First Launch
1. Create a user account
2. Complete the onboarding questionnaire
3. Start logging your mood daily

### Daily Routine
1. Go to "Track Mood" tab
2. Select your current mood using the slider
3. Choose context (what triggered the mood)
4. Select any symptoms you're experiencing
5. Add optional notes
6. Click "Save Entry"

### Learning & Growth
1. Explore available CBT courses in "Learn" tab
2. Choose courses by category or difficulty
3. Practice exercises from "Exercises" tab
4. Review your progress in "Analytics" tab

## Database Schema

### Users Table
- id, username, email, first_name, last_name, date_of_birth, gender, registration_date, notifications_enabled

### Mood Entries Table
- id, user_id, mood_level, mood_emoji, note, context, symptoms, entry_date

### Symptoms Table
- id, name, description, category, severity, icon

### Symptom Logs Table
- id, user_id, symptom_id, severity, date, notes

### CBT Courses Table
- id, title, description, category, duration, difficulty, content, icon

### Exercises Table
- id, title, description, instructions, category, duration, difficulty, icon

### Assessments Table
- id, user_id, assessment_date, mood_score, anxiety_score, depression_score, stress_score, overall_wellbeing, summary, recommendation

### Recommendations Table
- id, user_id, title, description, type, target_id, reason, priority, date

## Default Data

The application comes preloaded with:

**12 Predefined Symptoms**
- Depression category: sadness, loss of interest, fatigue, sleep problems
- Anxiety category: worry, racing thoughts, tension, panic
- Stress category: overwhelmed, irritability, concentration issues, physical tension

**5 CBT Courses**
- Understanding Depression
- Cognitive Behavioral Therapy Basics
- Mindfulness for Anxiety
- Sleep Hygiene
- Stress Management Techniques

**6 Exercises**
- Box Breathing
- 5-4-3-2-1 Grounding
- Progressive Muscle Relaxation
- Thought Record
- Gratitude Exercise
- Meditation

## Color Theme

The application uses a professional color scheme inspired by modern mental health applications:

- **Primary**: #667eea (Indigo)
- **Primary Dark**: #764ba2 (Purple)
- **Accent**: #f093fb (Pink)
- **Success**: #4caf50 (Green)
- **Warning**: #ff9800 (Orange)
- **Danger**: #f44336 (Red)
- **Background**: #f7fafc (Light Gray)

## Mood Indicators

- 😭 1-2: Very Bad
- 😔 3-4: Bad
- 😐 5-6: Neutral/Okay
- 😊 7-8: Good
- 😄 9-10: Excellent

## Security & Privacy

- All data stored locally in SQLite database
- No cloud syncing (local-first approach)
- Encrypted database support (future enhancement)
- GDPR compliant design

## Configuration

Database location: `mindoc.db` (created in application directory)

## Future Enhancements

- [ ] Cloud synchronization
- [ ] Export to PDF reports
- [ ] Mobile companion app
- [ ] Dark mode support
- [ ] Multi-language support (Ukrainian translation planned)
- [ ] Integration with healthcare providers
- [ ] Appointment reminders
- [ ] Emergency contact features
- [ ] More advanced analytics
- [ ] Custom symptom creation

## Disclaimer

MindDoc is a personal mental health tracking application and does NOT replace professional medical or psychological treatment. If you're in crisis or need immediate help, please contact your local emergency services or mental health crisis line.

## Support

For issues, feedback, or feature requests, please create an issue in the repository.

## License

[Add appropriate license]

## Version History

### 2.0.0 (2026-04-01)
- Complete redesign inspired by MindDoc
- Upgrade to Java 21
- New symptom tracking system
- CBT courses and exercises
- Enhanced analytics
- Professional UI theme

### 1.0.0 (Previous)
- Initial mood tracker version
