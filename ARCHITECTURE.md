# MindDoc Architecture & Design

## System Architecture

```
┌─────────────────────────────────────────────────────────┐
│                   JavaFX UI Layer                        │
│  (MindDocApp, Panels, Components, Theme)                │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│               Service Layer                              │
│  (MindDocService, AnalyticsService, RecommendationEngine)
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│            Repository Layer (DAL)                        │
│  (UserRepository, MoodEntryRepository, SymptomRepository)
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│        DatabaseManager & JDBC Connection                │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│            SQLite Database (mindoc.db)                   │
└─────────────────────────────────────────────────────────┘
```

## Design Patterns Used

### 1. **Repository Pattern**
- Abstracts data access logic
- Each model has its own repository
- Easy to switch between database implementations

```java
UserRepository userRepo = new UserRepository(connection);
User user = userRepo.findById(1);
```

### 2. **Service Layer Pattern**
- Business logic separated from UI
- Manages interaction between repositories
- Provides high-level operations

```java
MindDocService service = new MindDocService(databaseManager);
List<MoodEntry> entries = service.getMoodEntryRepository().findByUserId(userId);
```

### 3. **MVC (Model-View-Controller)**
- **Model**: Data classes (User, MoodEntry, etc.)
- **View**: UI panels (DashboardPanel, MoodTrackingPanel)
- **Controller**: Service classes handle logic

### 4. **Singleton Pattern**
- DatabaseManager creates single database connection
- Theme provides static styling

### 5. **Observer Pattern**
- JavaFX Properties for reactive updates
- Automatic UI refresh on data changes

## Database Schema

### Core Tables

#### users
```sql
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    email TEXT UNIQUE NOT NULL,
    first_name TEXT,
    last_name TEXT,
    date_of_birth TEXT,
    gender TEXT,
    registration_date TEXT NOT NULL,
    last_login_date TEXT,
    notifications_enabled INTEGER DEFAULT 1
);
```

#### mood_entries
```sql
CREATE TABLE mood_entries (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    mood_level INTEGER NOT NULL (1-10),
    mood_emoji TEXT,
    note TEXT,
    context TEXT,
    symptoms TEXT (comma-separated),
    entry_date TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

#### symptoms
```sql
CREATE TABLE symptoms (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    description TEXT,
    category TEXT NOT NULL (depression, anxiety, stress),
    severity INTEGER DEFAULT 5 (1-10),
    icon TEXT
);
```

#### cbt_courses
```sql
CREATE TABLE cbt_courses (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL UNIQUE,
    description TEXT,
    category TEXT NOT NULL,
    duration INTEGER (minutes),
    difficulty INTEGER (1-5),
    content TEXT,
    icon TEXT
);
```

#### exercises
```sql
CREATE TABLE exercises (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL UNIQUE,
    description TEXT,
    instructions TEXT,
    category TEXT NOT NULL,
    duration INTEGER (minutes),
    difficulty TEXT (beginner, intermediate, advanced),
    icon TEXT
);
```

## Module Breakdown

### com.mindoc
**Main Application Entry Point**
- `MindDocApp.java` - JavaFX Application main class

### com.mindoc.model
**Data Models**
- `User.java` - User profile information
- `MoodEntry.java` - Daily mood tracking entry
- `Symptom.java` - Mental health symptom definition
- `SymptomLog.java` - User symptom tracking record
- `CBTCourse.java` - Cognitive Behavioral Therapy course
- `Exercise.java` - Exercise or coping strategy
- `Assessment.java` - Overall health assessment
- `Recommendation.java` - Personalized recommendation

### com.mindoc.database
**Database Management**
- `DatabaseManager.java` - Connection pooling and table creation

### com.mindoc.repository
**Data Access Layer**
- `UserRepository.java` - CRUD operations for users
- `MoodEntryRepository.java` - CRUD operations for mood entries
- `SymptomRepository.java` - CRUD operations for symptoms
- `CBTCourseRepository.java` - CRUD operations for courses
- `ExerciseRepository.java` - CRUD operations for exercises

### com.mindoc.service
**Business Logic**
- `MindDocService.java` - Service coordinator
- `AnalyticsService.java` (planned) - Analytics and statistics
- `RecommendationEngine.java` (planned) - AI-powered recommendations

### com.mindoc.ui
**User Interface**

#### com.mindoc.ui.theme
- `MindDocTheme.java` - Color scheme and CSS styling

#### com.mindoc.ui.common
- `BasePanel.java` - Base class for all UI panels

#### com.mindoc.ui.dashboard
- `DashboardPanel.java` - Home dashboard with overview

#### com.mindoc.ui.moodtracking
- `MoodTrackingPanel.java` - Mood entry form

#### com.mindoc.ui.symptoms
- `SymptomTrackerPanel.java` (planned)

#### com.mindoc.ui.learning
- `CoursesPanel.java` (planned) - CBT courses
- `ExercisesPanel.java` (planned) - Exercise library

#### com.mindoc.ui.analytics
- `AnalyticsPanel.java` (planned) - Statistics and graphs

### com.mindoc.util
**Utility Classes**
- `DateUtils.java` - Date formatting and calculations
- `ColorUtils.java` - Color conversions and mood colors

## Data Flow

### 1. Mood Entry Creation
```
User Input (MoodTrackingPanel)
    ↓
MoodEntry Object Creation
    ↓
MoodEntryRepository.create()
    ↓
DatabaseManager Connection
    ↓
SQLite Database Insert
    ↓
UI Refresh (Dashboard updated)
```

### 2. Data Retrieval
```
User Navigation to Dashboard
    ↓
DashboardPanel.refresh()
    ↓
MoodEntryRepository.findByUserId()
    ↓
Database Query
    ↓
ResultSet Mapping
    ↓
UI Display (Charts, Statistics)
```

### 3. Analytics Processing
```
User views Analytics Tab
    ↓
AnalyticsService.generateReport()
    ↓
Aggregate mood data
    ↓
Calculate statistics
    ↓
Generate recommendations
    ↓
Display charts and insights
```

## Class Relationships

### Repository Pattern
```
DatabaseManager
    │
    ├── UserRepository
    ├── MoodEntryRepository
    ├── SymptomRepository
    ├── CBTCourseRepository
    └── ExerciseRepository
```

### Service Coordination
```
MindDocService
    │
    ├── UserRepository
    ├── MoodEntryRepository
    ├── SymptomRepository
    ├── CBTCourseRepository
    └── ExerciseRepository
```

### UI Hierarchy
```
MindDocApp
    │
    ├── DashboardPanel
    ├── MoodTrackingPanel
    ├── SymptomTrackerPanel
    ├── CoursesPanel
    ├── ExercisesPanel
    └── AnalyticsPanel
```

## Styling System

### Color Palette
```java
PRIMARY:        #667eea (Indigo) - Main action color
PRIMARY_DARK:   #764ba2 (Purple) - Dark variant
ACCENT:         #f093fb (Pink) - Highlight color
SUCCESS:        #4caf50 (Green) - Positive actions
WARNING:        #ff9800 (Orange) - Warning state
DANGER:         #f44336 (Red) - Error state
BACKGROUND:     #f7fafc (Light Gray) - App background
```

### Mood Indicators
```
1-2: 😭 #f44336 (Red - Terrible)
3-4: 😔 #ff9800 (Orange - Bad)
5-6: 😐 #ffc107 (Amber - Neutral)
7-8: 😊 #8bc34a (Light Green - Good)
9-10: 😄 #4caf50 (Green - Excellent)
```

## Future Enhancements

### Short Term (v2.1)
- [ ] Complete SymptomTrackerPanel UI
- [ ] Implement CoursesPanel with content
- [ ] Build ExercisesPanel with step-by-step guides
- [ ] Add AnalyticsPanel with charts

### Medium Term (v2.5)
- [ ] Cloud synchronization (optional)
- [ ] PDF export functionality
- [ ] Dark mode support
- [ ] Multi-language support
- [ ] Advanced analytics engine

### Long Term (v3.0)
- [ ] Mobile companion app
- [ ] Healthcare provider integration
- [ ] Real-time notifications
- [ ] Emergency contact system
- [ ] Community features
- [ ] AI-powered insights

## Performance Considerations

### Database Optimization
- Indexes on frequently queried columns (user_id, entry_date)
- Connection pooling (future enhancement)
- Query optimization for large datasets

### UI Optimization
- Lazy loading of data
- Virtual scrolling for large lists
- Caching of computed values
- Efficient CSS styling

### Memory Management
- Limited cache size
- Regular garbage collection
- Proper resource cleanup
- Database connection pooling

## Security Measures

### Data Protection
- Local-only storage (no cloud transmission)
- SQL injection prevention via prepared statements
- Input validation on all user inputs
- Secure password hashing (future enhancement)

### Privacy
- GDPR compliant design
- Data retention policies
- Export/delete user data functionality
- No tracking or analytics

## Testing Strategy

### Unit Tests (Future)
- Repository tests
- Service tests
- Utility function tests

### Integration Tests (Future)
- Database integration
- Service layer integration
- UI component tests

### User Acceptance Tests (Future)
- End-to-end workflows
- Performance testing
- Compatibility testing

---

This architecture provides a solid foundation for a professional mental health tracking application with room for growth and enhancement.
