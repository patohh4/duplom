# 👨‍💻 Розробницька документація

Цей документ надає інформацію для розвитку та розширення проекту Mood Tracker.

## 📐 Архітектура проекту

### Шари архітектури

```
┌─────────────────────────────────────┐
│      JavaFX UI Layer                 │
│  (MainWindow, UserPanel, etc)        │
└────────────────┬────────────────────┘
                 │
┌─────────────────────────────────────┐
│     Service Layer                    │
│  (MoodTrackerService)               │
│  - Бізнес-логіка                     │
│  - Координація операцій              │
└────────────────┬────────────────────┘
                 │
┌─────────────────────────────────────┐
│     Repository Layer (DAO)           │
│  - UserRepository                    │
│  - MoodEntryRepository               │
│  - RecommendationRepository          │
│  - SQL операції                      │
└────────────────┬────────────────────┘
                 │
┌─────────────────────────────────────┐
│     Database Layer                   │
│  - DatabaseManager                   │
│  - SQLite Connection                 │
│  - Schema Management                 │
└─────────────────────────────────────┘
```

## 🔄 Потік даних

### Додавання запису настрою:

```
UI (MoodPanel)
    ↓
MoodTrackerService.saveMoodEntry()
    ↓
MoodEntryRepository.saveMoodEntry()
    ↓
DatabaseManager.getConnection()
    ↓
SQLite Database
```

## 📦 Основні компоненти

### 1. Model Classes (`model/`)

#### `User.java`
- Представляє користувача системи
- Поля: `id`, `username`
- Методи: getters/setters

#### `MoodEntry.java`
- Запис настрою користувача за день
- Поля: `id`, `userId`, `moodLevel`, `moodEmoji`, `note`, `entryDate`
- Методи: конструктори, getters/setters

#### `Recommendation.java`
- Рекомендація для певного діапазону настроєчок
- Поля: `id`, `moodRangeMin`, `moodRangeMax`, `recommendationText`, `emoji`
- Методи: конструктори, getters/setters

### 2. Database Layer (`database/`)

#### `DatabaseManager.java`
**Відповідальність:**
- Управління SQLite з'єднанням
- Ініціалізація схеми БД
- Вставка стандартних рекомендацій
- Логування операцій БД

**Ключові методи:**
```java
public DatabaseManager()                  // Конструктор, ініціалізація
private void initializeDatabase()         // Створення таблиць
private void insertDefaultRecommendations() // Вставка даних
public Connection getConnection()         // Отримати З'єднання
public void close()                       // Закриття з'єднання
```

### 3. Repository Layer (`repository/`)

Репозиторії реалізують DAO (Data Access Object) паттерн для кожної моделі.

#### `UserRepository.java`
```java
public User createUser(String username)           // Створення користувача
public User getUserByUsername(String username)    // Пошук по імені
public User getUserById(int userId)               // Пошук по ID
public List<User> getAllUsers()                   // Список користувачів
public boolean deleteUser(int userId)             // Видалення
```

#### `MoodEntryRepository.java`
```java
public void saveMoodEntry(MoodEntry entry)       // Додавання/оновлення
public MoodEntry getMoodEntryByDate(int userId, LocalDate date)
public List<MoodEntry> getMoodEntriesByUserId(int userId)
public List<MoodEntry> getMoodEntriesByDateRange(int userId, LocalDate start, LocalDate end)
public void deleteMoodEntry(int entryId)
public double getAverageMoodLevel(int userId)
public MoodStatistics getMoodStatistics(int userId)
```

**Вложений клас:**
```java
public static class MoodStatistics {
    public double averageMood;
    public int minMood;
    public int maxMood;
    public int totalEntries;
}
```

#### `RecommendationRepository.java`
```java
public List<Recommendation> getRecommendationsByMoodLevel(int moodLevel)
public List<Recommendation> getAllRecommendations()
public void addRecommendation(Recommendation recommendation)
public void updateRecommendation(Recommendation recommendation)
public void deleteRecommendation(int recommendationId)
```

### 4. Service Layer (`service/`)

#### `MoodTrackerService.java`
**Відповідальність:**
- Координація операцій між репозиторіями
- Реалізація бізнес-логіки
- Фасад для UI рівня

**Групування методів:**
- **Користувачі:** `createUser()`, `getUserByUsername()`, `getAllUsers()`
- **Записи настроєв:** `saveMoodEntry()`, `getTodayMoodEntry()`, `getAllMoodEntries()`, і т.д.
- **Рекомендації:** `getRecommendations()`, `getAllRecommendations()`, і т.д.

### 5. UI Layer (`ui/`)

#### `MainWindow.java`
**Відповідальність:**
- Основне вікно застосунку
- Управління менюбаром
- Координація вкладок (TabPane)
- Управління життєвим циклом застосунку

**Структура:**
```
MenuBar (Файл, Довідка)
    │
TabPane
    ├── Tab 1: UserPanel (управління користувачами)
    ├── Tab 2: MoodPanel (запис настроїв)
    └── Tab 3: StatisticsPanel (статистика)
```

#### `UserPanel.java`
**Компоненти:**
- TextField для вводу імені користувача
- ListView для списку користувачів
- Кнопки: Створити, Оновити

**Особливості:**
- Зберігає посилання на вибраного користувача (статичне поле `currentSelectedUser`)
- Метод `getCurrentSelectedUser()` для доступу з інших панелей

#### `MoodPanel.java`
**Компоненти:**
- Spinner для вибору рівня (1-10)
- ComboBox для вибору емодзі
- TextArea для нотатки
- TextArea для рекомендацій

**Логіка:**
1. Користувач вибирає рівень настрою та емодзі
2. Додає опціональну нотатку
3. Натискає "Зберегти"
4. Запис сохраняється в БД
5. Системи показує рекомендації на основі рівня

#### `StatisticsPanel.java`
**Компоненти:**
- Label для статистики (середній, мін, макс)
- BarChart для візуалізації даних

**Логіка:**
- Отримує останні 14 днів записів
- Будує графік из даних
- Показує загальну статистику

## 🔧 Розширення функціональності

### Приклад 1: Додавання нової функції "Експорт в CSV"

**Крок 1:** Додайте метод в `MoodEntryRepository`:
```java
public void exportToCSV(int userId, String filePath) throws IOException {
    List<MoodEntry> entries = getMoodEntriesByUserId(userId);
    try (FileWriter writer = new FileWriter(filePath)) {
        writer.write("Date,Mood,Emoji,Note\n");
        for (MoodEntry entry : entries) {
            writer.write(String.format("%s,%d,%s,%s\n",
                entry.getEntryDate(),
                entry.getMoodLevel(),
                entry.getMoodEmoji(),
                entry.getNote()
            ));
        }
    }
}
```

**Крок 2:** Додайте метод в `MoodTrackerService`:
```java
public void exportUserMoodToCSV(int userId, String filePath) throws IOException {
    moodEntryRepository.exportToCSV(userId, filePath);
}
```

**Крок 3:** Додайте кнопку в `MoodPanel`:
```java
Button exportButton = new Button("Експортувати в CSV");
exportButton.setOnAction(e -> {
    FileChooser fileChooser = new FileChooser();
    File file = fileChooser.showSaveDialog(null);
    if (file != null) {
        try {
            service.exportUserMoodToCSV(user.getId(), file.getAbsolutePath());
            showInfo("Успіх", "Файл експортований успішно");
        } catch (IOException ex) {
            showError("Помилка", "Помилка при експорті: " + ex.getMessage());
        }
    }
});
```

### Приклад 2: Додавання темної теми

В `MainWindow.java`:
```java
private void applyDarkTheme(Scene scene) {
    scene.getStylesheets().add(
        getClass().getResource("/styles/dark-theme.css").toExternalForm()
    );
}
```

Створіть файл `src/main/resources/styles/dark-theme.css`:
```css
.root {
    -fx-base: #1e1e1e;
    -fx-control-inner-background: #2d2d2d;
    -fx-text-fill: #ffffff;
}

.button {
    -fx-text-fill: #ffffff;
    -fx-background-color: #0d47a1;
}

.button:hover {
    -fx-background-color: #1565c0;
}
```

### Приклад 3: Додавання сповіщень

```java
public class NotificationService {
    public static void showNotification(String title, String message) {
        javafx.application.Platform.runLater(() -> {
            Notification notification = new Notification(title, message);
            notification.show();
        });
    }
}
```

## 🧪 Тестування

### Основні сценарії тестування:

1. **Тестування користувачів:**
   - Створення користувача з дублюючимся імʼям (має генерувати помилку)
   - Видалення користувача та его записів

2. **Тестування записів:**
   - Збереження декількох записів для однієї дати (має оновити існуючий)
   - Отримання записів за період
   - Видалення запису

3. **Тестування статистики:**
   - Обчислення середнього, мін, макс
   - Обработка пустой БД

### Приклад unit-тесту:
```java
@Test
public void testSaveMoodEntry() throws SQLException {
    DatabaseManager dbManager = new DatabaseManager();
    MoodTrackerService service = new MoodTrackerService(dbManager);
    
    User user = service.createUser("TestUser");
    MoodEntry entry = new MoodEntry(user.getId(), 7, "😊", "Test", LocalDate.now());
    
    service.saveMoodEntry(entry);
    MoodEntry retrieved = service.getTodayMoodEntry(user.getId());
    
    assertEquals(7, retrieved.getMoodLevel());
    assertEquals("😊", retrieved.getMoodEmoji());
}
```

## 🐛 Debugging

### Встроене логування:
Всі класи використовують `SLF4J` з `Logback`. Логи видно в консолі та файлі.

### Запис у фаил:
```java
private static final Logger logger = LoggerFactory.getLogger(MyClass.class);
logger.info("Інформаційна повідомлення");
logger.error("Помилка", exception);
```

## 📋 Чек-лист для розширення

- [ ] Написано нові моделі (якщо потрібні)
- [ ] Додані методи в репозиторії
- [ ] Додана бізнес-логіка в service
- [ ] Оновлений UI (панелі)
- [ ] Додано логування
- [ ] Написані unit-тесты
- [ ] Перевірена компіляція (`mvn clean compile`)
- [ ] Упакований JAR (`mvn package`)
- [ ] Оновлена документація

## 🚀 Deployment

### Створення виконавчого JAR:
```bash
mvn clean package
java -jar target/mood-tracker-app-1.0.0.jar
```

### Створення носителя для дистрибуції:
```bash
# Добавити в pom.xml maven-assembly-plugin для створення дистрибутиву
mvn assembly:assembly
```

---

**Остання оновлення:** 15 грудня 2025

Для питань або пропозицій щодо розширення - перевірте README.md та SQL_QUERIES.md
