# 🔌 Підключення БД - Інструкція для Розробника

## 📦 Компоненти для роботи з БД

### 1. DatabaseManager
**Файл:** `src/main/java/com/moodtracker/database/DatabaseManager.java`

Головний клас для ініціалізації та управління підключенням до БД SQLite.

#### ✨ Функціональність:
- ✅ Ініціалізація SQLite драйвера
- ✅ Створення підключення до `mood_tracker.db`
- ✅ Автоматичне створення таблиць (CREATE TABLE IF NOT EXISTS)
- ✅ Заповнення таблиці рекомендацій за замовчуванням
- ✅ Управління життєвим циклом підключення

#### 🚀 Використання:
```java
// Створити DatabaseManager (БД буде ініціалізована при конструюванні)
DatabaseManager dbManager = new DatabaseManager();

// Отримати підключення для користувацьких запитів
Connection connection = dbManager.getConnection();

// Закрити підключення при завершенні роботи
dbManager.close();
```

#### 📍 Miejscо в проекті:
```
MoodTrackerApp (main)
    ↓
MainWindow (UI)
    ↓
DatabaseManager (ініціалізація БД)
    ↓
Repositories (робота з даними)
```

---

### 2. Repositories (Репозиторії)

Кожен репозиторій відповідає за операції з однією таблицею.

#### 📂 UserRepository
**Файл:** `src/main/java/com/moodtracker/repository/UserRepository.java`

Управління користувачами.

```java
// Створити користувача
User user = userRepository.createUser("john_doe");

// Отримати користувача за імʽям
User user = userRepository.getUserByUsername("john_doe");

// Отримати користувача за ID
User user = userRepository.getUserById(1);

// Отримати або створити користувача
User user = userRepository.createOrGetUser("john_doe");

// Видалити користувача
userRepository.deleteUser(1);

// Отримати всіх користувачів
List<User> users = userRepository.getAllUsers();
```

#### 📂 MoodEntryRepository
**Файл:** `src/main/java/com/moodtracker/repository/MoodEntryRepository.java`

Управління записами про настрій.

```java
// Зберегти запис настрою (новий або оновити)
MoodEntry entry = new MoodEntry(userId, 8, "😊", "Чудовий день", LocalDate.now());
moodEntryRepository.saveMoodEntry(entry);

// Отримати запис за датою
MoodEntry entry = moodEntryRepository.getMoodEntryByDate(1, LocalDate.now());

// Отримати всі записи користувача
List<MoodEntry> entries = moodEntryRepository.getMoodEntriesByUserId(1);

// Отримати записи за період
List<MoodEntry> entries = moodEntryRepository.getMoodEntriesByDateRange(
    1, 
    LocalDate.of(2024, 12, 1),
    LocalDate.of(2024, 12, 31)
);

// Видалити запис
moodEntryRepository.deleteMoodEntry(1);

// Отримати за місяць
List<MoodEntry> entries = moodEntryRepository.getMoodEntriesByMonth(1, 2024, 12);
```

#### 📂 RecommendationRepository
**Файл:** `src/main/java/com/moodtracker/repository/RecommendationRepository.java`

Управління рекомендаціями.

```java
// Отримати рекомендацію за рівнем настрою
Recommendation rec = recommendationRepository.getRecommendationByMoodLevel(8);

// Отримати всі рекомендації
List<Recommendation> recs = recommendationRepository.getAllRecommendations();
```

---

### 3. Service Layer (Сервісний шар)

**Файл:** `src/main/java/com/moodtracker/service/MoodTrackerService.java`

Бізнес-логіка застосунку, яка використовує репозиторії.

```java
// Ініціалізація сервісу
MoodTrackerService service = new MoodTrackerService(databaseManager);

// ===== Операції з користувачами =====
User user = service.createUser("john_doe");
User user = service.getUserByUsername("john_doe");
List<User> users = service.getAllUsers();

// ===== Операції з настроями =====
MoodEntry entry = new MoodEntry(userId, 8, "😊", "Чудовий день", LocalDate.now());
service.saveMoodEntry(entry);

MoodEntry today = service.getTodayMoodEntry(userId);
MoodEntry byDate = service.getMoodEntryByDate(userId, LocalDate.of(2024, 12, 15));
List<MoodEntry> all = service.getAllMoodEntries(userId);
List<MoodEntry> range = service.getMoodEntriesByDateRange(
    userId, 
    LocalDate.of(2024, 12, 1),
    LocalDate.of(2024, 12, 31)
);
service.deleteMoodEntry(1);

// ===== Операції з рекомендаціями =====
Recommendation rec = service.getRecommendationByMoodLevel(8);
List<Recommendation> allRecs = service.getAllRecommendations();

// ===== Статистика =====
double avgMood = service.getAverageMoodByMonth(userId, 2024, 12);
MoodStatistics stats = service.getMonthlyStatistics(userId, 2024, 12);
```

---

## 🔄 Архітектура Підключення

```
┌─────────────────────────────────────────────────────────┐
│          JavaFX UI (MainWindow, Panels)                │
└────────────────────┬──────────────────────────────────┘
                     │
┌────────────────────▼──────────────────────────────────┐
│       MoodTrackerService (Business Logic)             │
│  - createUser()                                        │
│  - saveMoodEntry()                                     │
│  - getRecommendation()                                 │
│  - getStatistics()                                     │
└────────────────────┬──────────────────────────────────┘
                     │
        ┌────────────┼────────────┐
        │            │            │
┌───────▼────┐  ┌────▼─────┐  ┌──▼────────┐
│   User     │  │  Mood    │  │Recommend  │
│Repository  │  │ Entry    │  │ation      │
│            │  │Repository│  │Repository │
└───────┬────┘  └────┬─────┘  └──┬────────┘
        │            │            │
└───────┴────────────┴────────────┘
         │
    ┌────▼──────────────────────────┐
    │   DatabaseManager (JDBC)      │
    │ - Load SQLite Driver          │
    │ - Manage Connection           │
    │ - Initialize Tables           │
    └────┬──────────────────────────┘
         │
    ┌────▼──────────────────────────┐
    │  SQLite Database              │
    │  (mood_tracker.db)            │
    │  - users                      │
    │  - mood_entries               │
    │  - recommendations            │
    └───────────────────────────────┘
```

---

## 🎯 Приклад Повного Робочого Процесу

```java
// 1. Ініціалізація БД
DatabaseManager dbManager = new DatabaseManager();

// 2. Ініціалізація сервісу
MoodTrackerService service = new MoodTrackerService(dbManager);

// 3. Создание користувача
User user = service.createUser("john_doe");
System.out.println("Created user: " + user.getId() + " - " + user.getUsername());

// 4. Додавання запису про настрій
MoodEntry entry = new MoodEntry(
    user.getId(),
    8,                          // Рівень настрою
    "😊",                        // Емодзі
    "Чудовий день на роботі",   // Нотатка
    LocalDate.now()             // Сьогоднішня дата
);
service.saveMoodEntry(entry);
System.out.println("Mood entry saved");

// 5. Отримання рекомендації
Recommendation recommendation = service.getRecommendationByMoodLevel(8);
System.out.println("Recommendation: " + recommendation.getText());
System.out.println("Emoji: " + recommendation.getEmoji());

// 6. Отримання статистики
MoodStatistics stats = service.getMonthlyStatistics(user.getId(), 2024, 12);
System.out.println("Average mood: " + stats.getAverageMood());
System.out.println("Best mood: " + stats.getMaxMood());
System.out.println("Worst mood: " + stats.getMinMood());

// 7. Закриття БД
dbManager.close();
```

---

## ⚠️ Обробка Помилок

```java
try {
    User user = service.createUser("john_doe");
    service.saveMoodEntry(entry);
} catch (SQLException e) {
    logger.error("Database error occurred", e);
    // Показати користувачу повідомлення про помилку
    showErrorDialog("Помилка бази даних: " + e.getMessage());
} catch (Exception e) {
    logger.error("Unexpected error", e);
    showErrorDialog("Непередбачена помилка");
}
```

---

## 🔧 Налаштування JDBC для SQLite

### Maven Залежність
```xml
<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
    <version>3.44.0.0</version>
</dependency>
```

### Рядок Підключення
```
jdbc:sqlite:mood_tracker.db
```

### Властивості Підключення
```java
// Автоматичний коміт (вимкнено для транзакцій)
connection.setAutoCommit(false);

// Виконати операції
// ...

// Коміт
connection.commit();

// На випадок помилки
catch (SQLException e) {
    connection.rollback();
}
```

---

## 🧪 Тестування БД Запитів

### Використання SQLite CLI

```bash
# Відкрити БД
sqlite3 mood_tracker.db

# Переглянути всі таблиці
.tables

# Переглянути схему таблиці
.schema mood_entries

# Виконати запит
SELECT * FROM users;

# Експортувати результат у CSV
.mode csv
.output users.csv
SELECT * FROM users;

# Вийти
.quit
```

### Використання Java для Тестування

```java
// Прямий запит через DatabaseManager
DatabaseManager dbManager = new DatabaseManager();
Connection conn = dbManager.getConnection();

try (Statement stmt = conn.createStatement();
     ResultSet rs = stmt.executeQuery("SELECT * FROM users")) {
    
    while (rs.next()) {
        System.out.println("ID: " + rs.getInt("id"));
        System.out.println("Username: " + rs.getString("username"));
    }
}
```

---

## 📝 Логування

Використовується **SLF4J** для логування операцій БД.

```java
private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);

logger.info("User created with id: {}", userId);
logger.error("Error creating user: {}", username, e);
logger.debug("Query executed successfully");
logger.warn("Connection not available");
```

**Перегляд логів:**
- Логи виводяться у консоль при запуску
- Можна налаштувати збереження у файл через `logback.xml`

---

## 🚀 Готові до Використання Методи

| Метод | Репозиторій | Опис |
|-------|-------------|------|
| `createUser()` | UserRepository | Створити нового користувача |
| `getUserByUsername()` | UserRepository | Отримати користувача за імʽям |
| `saveMoodEntry()` | MoodEntryRepository | Зберегти/оновити запис настрою |
| `getMoodEntryByDate()` | MoodEntryRepository | Отримати запис за датою |
| `getMoodEntriesByUserId()` | MoodEntryRepository | Отримати всі записи користувача |
| `getRecommendationByMoodLevel()` | RecommendationRepository | Отримати рекомендацію за настроєм |
| `getMonthlyStatistics()` | MoodEntryRepository | Отримати статистику за місяць |

---

**Остання оновлення:** 15 грудня 2024
