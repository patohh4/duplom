# 📱 Mood Tracker Application

> A desktop application for tracking user mood with intelligent recommendations for improvement

![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)
![Java](https://img.shields.io/badge/java-17+-green.svg)
![License](https://img.shields.io/badge/license-Diploma%20Project-orange.svg)

---

## 🎯 About This Project

**Mood Tracker** is a diploma thesis project for tracking daily mood with personalized recommendations. The application helps users:
- Track daily mood on a 1-10 scale
- Add text notes to mood entries
- View mood history by date
- Receive personalized recommendations
- Analyze mood statistics and trends

## 🎯 Функціональні вимоги

✅ **Управління користувачами:**
- Створення нових користувачів
- Вибір поточного користувача
- Список усіх зареєстрованих користувачів

✅ **Запис настроїв:**
- Вибір рівня настрою (1-10 або емодзі)
- Додавання текстової нотатки
- Оновлення запису за день

✅ **Рекомендації:**
- Автоматичні рекомендації на основі рівня настрою
- Умовна логіка без машинного навчання
- Розділенні рекомендації для різних діапазонів настроїв

✅ **Статистика:**
- Історія всіх записів
- Середній рівень настрою
- Мінімальний та максимальний рівні
- Графік настроїв за останні дні

## 🔧 Технічні вимоги

- **Мова:** Java 17+
- **UI Framework:** JavaFX 21.0.1
- **База даних:** SQLite 3
- **Build Tool:** Apache Maven
- **Logging:** SLF4J + Logback

## 📁 Структура проекту

```
duplom/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── moodtracker/
│                   ├── MoodTrackerApp.java          # Точка входу
│                   ├── database/
│                   │   └── DatabaseManager.java     # Управління БД
│                   ├── model/
│                   │   ├── User.java                # Модель користувача
│                   │   ├── MoodEntry.java           # Модель запису настрою
│                   │   └── Recommendation.java      # Модель рекомендації
│                   ├── repository/
│                   │   ├── UserRepository.java      # DAO для користувачів
│                   │   ├── MoodEntryRepository.java # DAO для записів
│                   │   └── RecommendationRepository.java # DAO для рекомендацій
│                   ├── service/
│                   │   └── MoodTrackerService.java  # Бізнес-логіка
│                   └── ui/
│                       ├── MainWindow.java          # Головне вікно
│                       ├── UserPanel.java           # Панель користувачів
│                       ├── MoodPanel.java           # Панель запису настроїв
│                       └── StatisticsPanel.java     # Панель статистики
├── pom.xml                                           # Maven конфігурація
└── README.md                                          # Цей файл
```

## 📊 Структура бази даних

### Таблиця `users`
```sql
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Таблиця `mood_entries`
```sql
CREATE TABLE mood_entries (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    mood_level INTEGER NOT NULL CHECK (mood_level >= 1 AND mood_level <= 10),
    mood_emoji TEXT,
    note TEXT,
    entry_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE(user_id, entry_date)
);
```

### Таблиця `recommendations`
```sql
CREATE TABLE recommendations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    mood_range_min INTEGER NOT NULL,
    mood_range_max INTEGER NOT NULL,
    recommendation_text TEXT NOT NULL,
    emoji TEXT
);
```

## 🚀 Запуск застосунку

### Вимоги:
- Java JDK 17 або вище
- Maven 3.6+

### Компіляція та запуск:

```bash
# Перейти до папки проекту
cd /path/to/duplom

# Очистити та скомпілювати
mvn clean compile

# Упакувати в JAR
mvn package

# Запустити застосунок
java -jar target/mood-tracker-app-1.0.0.jar
```

Або запустити безпосередньо з Maven:
```bash
mvn clean javafx:run
```

## 📱 Інтерфейс користувача

### Вкладка "Користувачі"
- Створення нового користувача
- Список усіх користувачів
- Вибір поточного користувача

### Вкладка "Мій настрій"
- Вибір рівня настрою (1-10)
- Вибір емодзі
- Додання нотатки
- Збереження запису
- Перегляд історії

### Вкладка "Статистика"
- Загальна статистика (середній, мін, макс)
- Графік настроїв за останні 14 днів
- Оновлення даних

## 🎨 Рекомендації за рівнем настрою

| Рівень | Діапазон | Рекомендація | Емодзі |
|--------|----------|--------------|--------|
| Дуже поганий | 1-3 | Спробуй прогулятися на свіжому повітрі | 🚶 |
| Поганий | 4-5 | Послухай музику, яка тобі подобається | 🎵 |
| Нейтральний | 6-7 | Спробуй займатися улюбленою діяльністю | 🎯 |
| Хороший | 8-9 | Поділись позитивом з іншими людьми | 🤝 |
| Відмінний | 10 | Продовжуй так! Цей момент чудовий | ⭐ |

## 🔐 Безпека даних

- Усі дані зберігаються локально в SQLite БД
- Використовуються підготовлені вирази (Prepared Statements) для захисту від SQL-ін'єкцій
- Каскадне видалення записів при видаленні користувача

## 📝 Логування

Застосунок використовує SLF4J + Logback для логування:
- Логи записуються в консоль та файл
- Рівень логування: INFO за замовчуванням
- Лог-файли зберігаються у робочій директорії

## 🛠️ Розширення проекту

Можливі напрями розширення:
1. **Експорт даних** - CSV/Excel
2. **Графіки** - більш детальні аналітичні графіки
3. **Теми** - темна/світла тема інтерфейсу
4. **Синхронізація** - хмарне резервне копіювання
5. **Повідомлення** - нагадування про запис настрою
6. **Аналітика ML** - пропозиції рекомендацій на основі історії

## 👨‍💻 Автор

Розроблено як дипломний проект.

## 📄 Ліцензія

MIT License - вільно використовуйте та модифікуйте код.

---

## 🐛 Розв'язання проблем

### Помилка "Database file not found"
- Базу даних буде автоматично створено при першому запуску
- Перевірте права доступу до папки проекту

### JavaFX не запускається
- Переконайтеся, що встановлено Java 17+
- Для Linux можуть потребуватися додаткові залежності: `sudo apt install libgl1-mesa-glx`

### Помилка при компіляції
```bash
mvn clean install -U  # Оновити залежності
mvn dependency:tree   # Перевірити дерево залежностей
```

---

**Статус проекту:** ✅ Готово до використання

Остання оновлення: 15 грудня 2025
