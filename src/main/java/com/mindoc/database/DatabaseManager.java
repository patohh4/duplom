package com.mindoc.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * Manages database connections and initialization for MindDoc application
 */
public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static final String DATABASE_URL = "jdbc:sqlite:mindoc.db";
    private Connection connection;
    
    public DatabaseManager() {
        try {
            initializeDatabase();
            logger.info("Database initialized successfully");
        } catch (SQLException e) {
            logger.error("Failed to initialize database", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }
    
    private void initializeDatabase() throws SQLException {
        connection = DriverManager.getConnection(DATABASE_URL);
        createTables();
        insertDefaultData();
    }
    
    private void createTables() throws SQLException {
        // Users table
        executeSql("CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT UNIQUE NOT NULL," +
                "email TEXT UNIQUE NOT NULL," +
                "password TEXT NOT NULL," +
                "first_name TEXT," +
                "last_name TEXT," +
                "date_of_birth TEXT," +
                "gender TEXT," +
                "registration_date TEXT NOT NULL," +
                "last_login_date TEXT," +
                "notifications_enabled INTEGER DEFAULT 1" +
                ")");
        
        // Mood entries table
        executeSql("CREATE TABLE IF NOT EXISTS mood_entries (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "mood_level INTEGER NOT NULL," +
                "mood_emoji TEXT," +
                "note TEXT," +
                "context TEXT," +
                "symptoms TEXT," +
                "entry_date TEXT NOT NULL," +
                "FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE" +
                ")");
        
        // Symptoms table
        executeSql("CREATE TABLE IF NOT EXISTS symptoms (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL UNIQUE," +
                "description TEXT," +
                "category TEXT NOT NULL," +
                "severity INTEGER DEFAULT 5," +
                "icon TEXT" +
                ")");
        
        // Symptom logs table
        executeSql("CREATE TABLE IF NOT EXISTS symptom_logs (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "symptom_id INTEGER NOT NULL," +
                "severity INTEGER NOT NULL," +
                "date TEXT NOT NULL," +
                "notes TEXT," +
                "FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE," +
                "FOREIGN KEY (symptom_id) REFERENCES symptoms (id) ON DELETE CASCADE" +
                ")");
        
        // CBT Courses table
        executeSql("CREATE TABLE IF NOT EXISTS cbt_courses (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT NOT NULL UNIQUE," +
                "description TEXT," +
                "category TEXT NOT NULL," +
                "duration INTEGER," +
                "difficulty INTEGER," +
                "content TEXT," +
                "icon TEXT" +
                ")");
        
        // Exercises table
        executeSql("CREATE TABLE IF NOT EXISTS exercises (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT NOT NULL UNIQUE," +
                "description TEXT," +
                "instructions TEXT," +
                "category TEXT NOT NULL," +
                "duration INTEGER," +
                "difficulty TEXT," +
                "icon TEXT" +
                ")");
        
        // Assessments table
        executeSql("CREATE TABLE IF NOT EXISTS assessments (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "assessment_date TEXT NOT NULL," +
                "mood_score INTEGER," +
                "anxiety_score INTEGER," +
                "depression_score INTEGER," +
                "stress_score INTEGER," +
                "overall_wellbeing INTEGER," +
                "summary TEXT," +
                "recommendation TEXT," +
                "FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE" +
                ")");
        
        // Recommendations table
        executeSql("CREATE TABLE IF NOT EXISTS recommendations (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "title TEXT NOT NULL," +
                "description TEXT," +
                "type TEXT," +
                "target_id INTEGER," +
                "reason TEXT," +
                "priority INTEGER," +
                "date TEXT," +
                "FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE" +
                ")");

        // App settings table
        executeSql("CREATE TABLE IF NOT EXISTS app_settings (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL UNIQUE," +
                "theme TEXT DEFAULT 'Light'," +
                "language TEXT DEFAULT 'English'," +
                "text_size INTEGER DEFAULT 100," +
                "notifications_enabled INTEGER DEFAULT 1," +
                "updated_at TEXT," +
                "FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE" +
                ")");

        // Learning progress table
        executeSql("CREATE TABLE IF NOT EXISTS learning_progress (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "item_type TEXT NOT NULL," +
                "item_id INTEGER NOT NULL," +
                "status TEXT NOT NULL," +
                "updated_at TEXT," +
                "UNIQUE(user_id, item_type, item_id)," +
                "FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE" +
                ")");
        
        logger.info("All tables created/verified successfully");
    }
    
    private void insertDefaultUser() throws SQLException {
        String sql = "INSERT INTO users (username, email, password, registration_date) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "demo");
            pstmt.setString(2, "demo@mindoc.com");
            pstmt.setString(3, "demo");
            pstmt.setString(4, java.time.LocalDate.now().toString());
            pstmt.executeUpdate();
            logger.info("Default demo user created");
        }
    }
    
    private void insertDefaultData() throws SQLException {
        // Create demo user if not exists
        if (isTableEmpty("users")) {
            insertDefaultUser();
        }
        
        // Check if symptoms table is empty
        if (isTableEmpty("symptoms")) {
            insertDefaultSymptoms();
        }
        
        if (isTableEmpty("cbt_courses")) {
            insertDefaultCourses();
        }
        
        if (isTableEmpty("exercises")) {
            insertDefaultExercises();
        }

        insertDefaultSettingsForUsers();
        backfillLearningContent();
    }
    
    private void insertDefaultSymptoms() throws SQLException {
        String[][] symptoms = {
            // Depression
            {"Persistent sadness", "Feeling sad or empty most of the day", "depression", "😢"},
            {"Loss of interest", "Losing interest in activities you once enjoyed", "depression", "😔"},
            {"Fatigue", "Feeling tired and lacking energy", "depression", "😴"},
            {"Sleep problems", "Difficulty sleeping or sleeping too much", "depression", "🌙"},
            
            // Anxiety
            {"Excessive worry", "Worrying about many things constantly", "anxiety", "😰"},
            {"Racing thoughts", "Thoughts that come too fast to control", "anxiety", "💭"},
            {"Tension", "Feeling physically tense or stressed", "anxiety", "😟"},
            {"Panic", "Sudden fear or panic attacks", "anxiety", "😨"},
            
            // Stress
            {"Overwhelmed", "Feeling overwhelmed by tasks or responsibilities", "stress", "😩"},
            {"Irritability", "Being easily irritated or angry", "stress", "😠"},
            {"Concentration issues", "Difficulty focusing or concentrating", "stress", "🤔"},
            {"Physical tension", "Muscle tension or headaches from stress", "stress", "💪"}
        };
        
        String sql = "INSERT INTO symptoms (name, description, category, icon) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (String[] symptom : symptoms) {
                pstmt.setString(1, symptom[0]);
                pstmt.setString(2, symptom[1]);
                pstmt.setString(3, symptom[2]);
                pstmt.setString(4, symptom[3]);
                pstmt.executeUpdate();
            }
            logger.info("Default symptoms inserted");
        }
    }
    
    private void insertDefaultCourses() throws SQLException {
        String[][] courses = {
            {"Understanding Depression", "Learn about depression symptoms and causes", "depression", "30", "1"},
            {"Cognitive Behavioral Therapy Basics", "Introduction to CBT techniques", "general", "45", "2"},
            {"Mindfulness for Anxiety", "Reduce anxiety through mindfulness practices", "anxiety", "25", "2"},
            {"Sleep Hygiene", "Improve your sleep quality naturally", "sleep", "20", "1"},
            {"Stress Management Techniques", "Practical techniques to manage stress", "stress", "35", "2"}
        };
        
        String sql = "INSERT INTO cbt_courses (title, description, category, duration, difficulty) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (String[] course : courses) {
                pstmt.setString(1, course[0]);
                pstmt.setString(2, course[1]);
                pstmt.setString(3, course[2]);
                pstmt.setString(4, course[3]);
                pstmt.setString(5, course[4]);
                pstmt.executeUpdate();
            }
            logger.info("Default courses inserted");
        }
    }
    
    private void insertDefaultExercises() throws SQLException {
        String[][] exercises = {
            {"Box Breathing", "A simple breathing technique to calm your mind", "breathing", "5", "beginner"},
            {"5-4-3-2-1 Grounding", "Ground yourself using your five senses", "grounding", "10", "beginner"},
            {"Progressive Muscle Relaxation", "Relax your body by tensing and releasing muscles", "relaxation", "15", "intermediate"},
            {"Thought Record", "Identify and challenge negative thoughts", "cognitive", "10", "intermediate"},
            {"Gratitude Exercise", "Write down things you're grateful for", "mindfulness", "5", "beginner"},
            {"Meditation", "Practice mindfulness meditation", "meditation", "20", "intermediate"}
        };
        
        String sql = "INSERT INTO exercises (title, description, category, duration, difficulty) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (String[] exercise : exercises) {
                pstmt.setString(1, exercise[0]);
                pstmt.setString(2, exercise[1]);
                pstmt.setString(3, exercise[2]);
                pstmt.setString(4, exercise[3]);
                pstmt.setString(5, exercise[4]);
                pstmt.executeUpdate();
            }
            logger.info("Default exercises inserted");
        }
    }
    
    private boolean isTableEmpty(String tableName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.getInt(1) == 0;
        }
    }

    private void insertDefaultSettingsForUsers() throws SQLException {
        String sql =
            "INSERT INTO app_settings (user_id, theme, language, text_size, notifications_enabled, updated_at) " +
            "SELECT id, 'Light', 'English', 100, 1, ? FROM users " +
            "WHERE id NOT IN (SELECT user_id FROM app_settings)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, java.time.LocalDateTime.now().toString());
            pstmt.executeUpdate();
        }
    }

    private void backfillLearningContent() throws SQLException {
        executeSql(
            "UPDATE cbt_courses SET content = " +
            "'1. Read the description carefully.\\n' ||" +
            "'2. Practice one core idea from this topic today.\\n' ||" +
            "'3. Write down one observation in your journal.\\n' ||" +
            "'4. Repeat tomorrow and compare how you feel.' " +
            "WHERE content IS NULL OR TRIM(content) = ''"
        );

        executeSql(
            "UPDATE exercises SET instructions = " +
            "'1. Find a quiet place.\\n' ||" +
            "'2. Follow the exercise description step by step.\\n' ||" +
            "'3. Keep steady breathing during the full duration.\\n' ||" +
            "'4. After completion, note how you feel.' " +
            "WHERE instructions IS NULL OR TRIM(instructions) = ''"
        );
    }
    
    private void executeSql(String sql) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
    
    public Connection getConnection() {
        return connection;
    }
    
    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            logger.info("Database connection closed");
        }
    }
}
