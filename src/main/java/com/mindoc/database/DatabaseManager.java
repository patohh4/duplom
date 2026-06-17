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
            // Депресія
            {"Постійний смуток", "Відчуття смутку або порожнечі більшу частину дня", "depression", "😢"},
            {"Втрата інтересу", "Зникнення інтересу до занять, які раніше подобались", "depression", "😔"},
            {"Втома", "Відчуття втоми та браку енергії без видимої причини", "depression", "😴"},
            {"Проблеми зі сном", "Труднощі з засинанням або надмірна сонливість", "depression", "🌙"},

            // Тривога
            {"Надмірне хвилювання", "Постійне хвилювання через різні речі без вагомих причин", "anxiety", "😰"},
            {"Нав'язливі думки", "Думки, які виникають занадто швидко і важко контролювати", "anxiety", "💭"},
            {"Напруженість", "Відчуття фізичної напруги або внутрішнього неспокою", "anxiety", "😟"},
            {"Панічні атаки", "Раптовий сильний страх або напади паніки", "anxiety", "😨"},

            // Стрес
            {"Перевантаження", "Відчуття, що завдань або відповідальності занадто багато", "stress", "😩"},
            {"Дратівливість", "Легка збудливість, роздратованість або спалахи гніву", "stress", "😠"},
            {"Труднощі з концентрацією", "Складно зосередитись або утримувати увагу на завданні", "stress", "🤔"},
            {"Фізична напруга", "М'язова напруга, головний біль або біль у шиї від стресу", "stress", "💪"}
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
            {"Розуміння депресії", "Дізнайтесь про симптоми та причини депресії", "depression", "30", "1"},
            {"Основи когнітивно-поведінкової терапії", "Вступ до технік КПТ для роботи з негативними думками", "general", "45", "2"},
            {"Майндфулнес при тривозі", "Зменшіть тривогу за допомогою практик усвідомленості", "anxiety", "25", "2"},
            {"Гігієна сну", "Покращіть якість свого сну природними методами", "sleep", "20", "1"},
            {"Техніки управління стресом", "Практичні методи для зниження рівня стресу у повсякденному житті", "stress", "35", "2"}
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
            {"Дихання по квадрату", "Проста дихальна техніка для заспокоєння розуму та зниження тривоги", "breathing", "5", "beginner"},
            {"Заземлення 5-4-3-2-1", "Повернись у момент «тут і зараз» за допомогою п'яти органів чуття", "grounding", "10", "beginner"},
            {"Прогресивна м'язова релаксація", "Зніми напругу в тілі, послідовно напружуючи та розслабляючи м'язи", "relaxation", "15", "intermediate"},
            {"Щоденник думок", "Визнач та перевір негативні автоматичні думки за допомогою КПТ-підходу", "cognitive", "10", "intermediate"},
            {"Вправа вдячності", "Запиши три речі, за які ти вдячний сьогодні, і відчуй зміну настрою", "mindfulness", "5", "beginner"},
            {"Медитація усвідомленості", "Практикуй медитацію з фокусом на диханні для зниження стресу", "meditation", "20", "intermediate"}
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
        // Unique content for each course
        String[][] courseContents = {
            {"Розуміння депресії",
                "1. Навчись розпізнавати ранні ознаки депресії у власній поведінці та настрої.\n" +
                "2. Запиши 3 ситуації цього тижня, коли ти відчував брак енергії або безнадію.\n" +
                "3. Ознайомся з біологічними та психологічними причинами виникнення депресії.\n" +
                "4. Обговори свої спостереження з близькою людиною або занось їх у щоденник."},
            {"Основи когнітивно-поведінкової терапії",
                "1. Прочитай про зв'язок між думками, емоціями та поведінкою людини.\n" +
                "2. Обери одну негативну думку, яка виникла сьогодні, і запиши її.\n" +
                "3. Визнач, яка емоція та яка поведінка виникли внаслідок цієї думки.\n" +
                "4. Попрактикуйся замінювати негативну думку збалансованою та реалістичною."},
            {"Майндфулнес при тривозі",
                "1. Виділи 10 хвилин у тихому місці без відволікань та сповіщень.\n" +
                "2. Зосередься на диханні — вдихай 4 рахунки, видихай 6 рахунків.\n" +
                "3. Коли виникають тривожні думки — спостерігай за ними без осуду і відпускай.\n" +
                "4. Після сесії запиши, як змінився твій рівень тривоги порівняно з початком."},
            {"Гігієна сну",
                "1. Встанови постійний час відходу до сну та підйому, навіть у вихідні дні.\n" +
                "2. Уникай екранів і яскравого освітлення щонайменше за 1 годину до сну.\n" +
                "3. Створи заспокійливий ритуал перед сном: читання, розтяжка або тепла кружка чаю.\n" +
                "4. Відстежуй якість сну в застосунку протягом 7 днів і проаналізуй закономірності."},
            {"Техніки управління стресом",
                "1. Визнач 3 головні джерела стресу цього тижня і запиши їх.\n" +
                "2. Для кожного стресора сформулюй одну конкретну дію, яка може його зменшити.\n" +
                "3. Практикуй техніку дихання по квадрату протягом 5 хвилин у піковий момент стресу.\n" +
                "4. Наприкінці дня оціни рівень стресу і занотуй, що допомогло найбільше."}
        };

        for (String[] course : courseContents) {
            String sql = "UPDATE cbt_courses SET content = ? WHERE title = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, course[1]);
                pstmt.setString(2, course[0]);
                pstmt.executeUpdate();
            }
        }

        // Unique instructions for each exercise
        String[][] exerciseInstructions = {
            {"Дихання по квадрату",
                "1. Сядь рівно на зручний стілець, поклади ступні рівно на підлогу.\n" +
                "2. Повністю видихни, щоб звільнити легені від повітря.\n" +
                "3. Повільно вдихни через ніс протягом 4 рахунків.\n" +
                "4. Затримай дихання на 4 рахунки, не напружуючись.\n" +
                "5. Повільно видихни через рот протягом 4 рахунків.\n" +
                "6. Затримай порожній видих на 4 рахунки. Повтори цикл 4-6 разів."},
            {"Заземлення 5-4-3-2-1",
                "1. Зупинись і зроби один повільний глибокий вдих та видих.\n" +
                "2. Назви 5 речей, які ти можеш зараз побачити навколо себе.\n" +
                "3. Назви 4 речі, які ти фізично відчуваєш (стілець, підлога, температура повітря).\n" +
                "4. Назви 3 звуки, які ти чуєш у своєму оточенні прямо зараз.\n" +
                "5. Name 2 things you can smell, or recall 2 favourite scents.\n" +
                "6. Name 1 thing you can taste. Notice how your body feels calmer now."},
            {"Progressive Muscle Relaxation",
                "1. Lie down or sit in a comfortable position and close your eyes.\n" +
                "2. Починаючи зі ступнів, сильно напруж м'язи на 5 секунд.\n" +
                "3. Різко відпусти напругу і відчуй хвилю розслаблення.\n" +
                "4. Рухайся вгору — литки, стегна, живіт, кисті, руки, плечі, обличчя.\n" +
                "5. Приділи 10 секунд розслабленню кожної групи м'язів перед переходом далі.\n" +
                "6. Завершіть 3 глибокими вдихами і повільно відкрий очі."},
            {"Щоденник думок",
                "1. Визнач ситуацію, яка викликала сильну негативну емоцію сьогодні.\n" +
                "2. Запиши автоматичну думку, яка виникла в тій ситуації.\n" +
                "3. Оціни інтенсивність емоції від 0 до 100%.\n" +
                "4. Запиши докази, які підтверджують цю думку.\n" +
                "5. Запиши докази, які спростовують або ставлять під сумнів цю думку.\n" +
                "6. Сформулюй збалансовану альтернативну думку і знову оціни емоцію."},
            {"Вправа вдячності",
                "1. Знайди тихий момент — вранці або ввечері це працює найкраще.\n" +
                "2. Відкрий нотатки або щоденник і приготуйся писати.\n" +
                "3. Запиши 3 конкретні речі, за які ти вдячний сьогодні.\n" +
                "4. До кожного пункту додай одне речення — чому це важливо для тебе.\n" +
                "5. Перечитай список вголос повільно і дозволь кожному пункту дійти до серця.\n" +
                "6. Зверни увагу на будь-яку зміну настрою після виконання вправи."},
            {"Медитація усвідомленості",
                "1. Обери тихе місце і встанови таймер на 20 хвилин.\n" +
                "2. Сядь зручно з рівною спиною, поклади руки на коліна долонями вгору.\n" +
                "3. Заплющ очі і направ увагу на своє природне дихання.\n" +
                "4. Коли розум відволікається — м'яко поверни фокус назад до дихання.\n" +
                "5. Не осуджуй свої думки — просто спостерігай за ними і відпускай.\n" +
                "6. Коли таймер спрацює — відкрий очі повільно і посидь нерухомо ще хвилину."}
        };

        for (String[] exercise : exerciseInstructions) {
            String sql = "UPDATE exercises SET instructions = ? WHERE title = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, exercise[1]);
                pstmt.setString(2, exercise[0]);
                pstmt.executeUpdate();
            }
        }
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
