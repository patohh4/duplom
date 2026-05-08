# Mood Tracker - Схема Бази Даних

## 📊 Огляд

Застосунок використовує **SQLite** для зберігання даних про користувачів, їх настрої та рекомендації.

**Файл БД:** `mood_tracker.db`

---

## 📋 Таблиці

### 1️⃣ Таблиця `users` (Користувачі)

Зберігає інформацію про користувачів застосунку.

```sql
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)
```

| Поле | Тип | Опис |
|------|-----|------|
| `id` | INTEGER PRIMARY KEY | Унікальний ідентифікатор користувача |
| `username` | TEXT UNIQUE NOT NULL | Імʽя користувача (унікальне) |
| `created_at` | TIMESTAMP | Дата реєстрації користувача |

**Приклад:**
```
id | username | created_at
---|----------|---------------------------
1  | john_doe | 2024-12-15 16:44:48
2  | jane_smith | 2024-12-15 16:45:00
```

---

### 2️⃣ Таблиця `mood_entries` (Записи Настрою)

Зберігає щоденні записи про настрій користувача.

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
)
```

| Поле | Тип | Опис |
|------|-----|------|
| `id` | INTEGER PRIMARY KEY | Унікальний ідентифікатор запису |
| `user_id` | INTEGER NOT NULL | Посилання на користувача (FK) |
| `mood_level` | INTEGER (1-10) | Рівень настрою від 1 (найгірший) до 10 (найкращий) |
| `mood_emoji` | TEXT | Емодзі для представлення настрою |
| `note` | TEXT | Коротка текстова нотатка (опціонально) |
| `entry_date` | DATE | Дата запису (унікальна на користувача) |
| `created_at` | TIMESTAMP | Час створення/редагування запису |

**Обмеження:**
- ✅ Кожен користувач може мати максимум один запис на день (UNIQUE constraint)
- ✅ Настрій має бути між 1 і 10 (CHECK constraint)
- ✅ При видаленні користувача його записи також видаляються (ON DELETE CASCADE)

**Приклад:**
```
id | user_id | mood_level | mood_emoji | note | entry_date | created_at
---|---------|------------|------------|------|------------|---------------------------
1  | 1       | 8          | 😊        | Чудовий день | 2024-12-15 | 2024-12-15 16:45:00
2  | 1       | 6          | 😐        | Так собі | 2024-12-14 | 2024-12-14 18:30:00
```

---

### 3️⃣ Таблиця `recommendations` (Рекомендації)

Довідникова таблиця з рекомендаціями залежно від рівня настрою.

```sql
CREATE TABLE recommendations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    mood_range_min INTEGER NOT NULL,
    mood_range_max INTEGER NOT NULL,
    recommendation_text TEXT NOT NULL,
    emoji TEXT
)
```

| Поле | Тип | Опис |
|------|-----|------|
| `id` | INTEGER PRIMARY KEY | Унікальний ідентифікатор |
| `mood_range_min` | INTEGER | Мінімальний рівень настрою |
| `mood_range_max` | INTEGER | Максимальний рівень настрою |
| `recommendation_text` | TEXT | Текст рекомендації |
| `emoji` | TEXT | Емодзі рекомендації |

**Вбудовані рекомендації:**

| Рівень | Діапазон | Рекомендація | Емодзі |
|--------|----------|------|--------|
| Дуже поганий | 1-3 | Спробуй прогулятися на свіжому повітрі | 🚶 |
| Поганий | 4-5 | Послухай музику, яка тобі подобається | 🎵 |
| Нейтральний | 6-7 | Спробуй займатися улюбленою діяльністю | 🎯 |
| Хороший | 8-9 | Поділись позитивом з іншими людьми | 🤝 |
| Відмінний | 10 | Продовжуй так! Цей момент чудовий 🌟 | ⭐ |

**Приклад:**
```
id | mood_range_min | mood_range_max | recommendation_text | emoji
---|----------------|----------------|-----|--------|
1  | 1              | 3              | Спробуй прогулятися на свіжому повітрі | 🚶
2  | 4              | 5              | Послухай музику | 🎵
3  | 6              | 7              | Займайся улюбленою діяльністю | 🎯
4  | 8              | 9              | Поділись позитивом | 🤝
5  | 10             | 10             | Продовжуй так! 🌟 | ⭐
```

---

## 🔗 Зв'язки

```
┌──────────────────────────────────────────┐
│           USERS (користувачі)            │
│ ┌──────────────────────────────────────┐ │
│ │ id (PRIMARY KEY)                     │ │
│ │ username (UNIQUE)                    │ │
│ │ created_at                           │ │
│ └──────────────────────────────────────┘ │
└─────────────────────┬────────────────────┘
                      │
                      │ 1:N (One-to-Many)
                      │
┌─────────────────────▼────────────────────┐
│      MOOD_ENTRIES (записи настрою)      │
│ ┌──────────────────────────────────────┐ │
│ │ id (PRIMARY KEY)                     │ │
│ │ user_id (FOREIGN KEY → users.id)     │ │
│ │ mood_level (1-10)                    │ │
│ │ mood_emoji                           │ │
│ │ note                                 │ │
│ │ entry_date                           │ │
│ │ created_at                           │ │
│ │ UNIQUE(user_id, entry_date)          │ │
│ └──────────────────────────────────────┘ │
└──────────────────────────────────────────┘

┌──────────────────────────────────────────┐
│    RECOMMENDATIONS (рекомендації)        │
│ ┌──────────────────────────────────────┐ │
│ │ id (PRIMARY KEY)                     │ │
│ │ mood_range_min                       │ │
│ │ mood_range_max                       │ │
│ │ recommendation_text                  │ │
│ │ emoji                                │ │
│ └──────────────────────────────────────┘ │
│ (Довідникова таблиця - використовується │
│  для вибору рекомендацій в логіці APP)  │
└──────────────────────────────────────────┘
```

---

## 📝 Основні SQL Запити

### ✅ Додавання запису настрою

```sql
-- Вставити новий запис (або оновити існуючий на той же день)
INSERT INTO mood_entries (user_id, mood_level, mood_emoji, note, entry_date)
VALUES (1, 8, '😊', 'Чудовий день!', DATE('2024-12-15'))
ON CONFLICT(user_id, entry_date) DO UPDATE SET
    mood_level = excluded.mood_level,
    mood_emoji = excluded.mood_emoji,
    note = excluded.note,
    created_at = CURRENT_TIMESTAMP;
```

### 📊 Отримання історії настроїв користувача

```sql
-- Отримати всі записи користувача за датою
SELECT 
    id, mood_level, mood_emoji, note, entry_date
FROM mood_entries
WHERE user_id = 1
ORDER BY entry_date DESC;
```

### 🎯 Отримання рекомендацій на основі настрою

```sql
-- Отримати рекомендації для настрою рівня 8
SELECT recommendation_text, emoji
FROM recommendations
WHERE 8 BETWEEN mood_range_min AND mood_range_max;
```

### 📈 Статистика за період

```sql
-- Статистика по місяцях
SELECT 
    STRFTIME('%Y-%m', entry_date) AS month,
    AVG(mood_level) AS avg_mood,
    MIN(mood_level) AS min_mood,
    MAX(mood_level) AS max_mood,
    COUNT(*) AS days_recorded
FROM mood_entries
WHERE user_id = 1
GROUP BY STRFTIME('%Y-%m', entry_date)
ORDER BY month DESC;
```

### 👤 Отримання даних користувача

```sql
-- Отримати користувача за імʽям
SELECT * FROM users WHERE username = 'john_doe';

-- Отримати всіх користувачів
SELECT * FROM users ORDER BY created_at DESC;
```

---

## 🔐 Цілісність Даних

### Обмеження Цілісності

1. **PRIMARY KEY** - Гарантує унікальність кожного запису
2. **FOREIGN KEY** - Гарантує зв'язок між users і mood_entries
3. **UNIQUE** - На користувача може бути лише один запис на день
4. **CHECK** - Настрій може бути тільки від 1 до 10
5. **NOT NULL** - Обов'язкові поля не можуть бути порожніми
6. **ON DELETE CASCADE** - При видаленні користувача видаляються його записи

### Каскадне видалення

```sql
-- При видаленні користувача:
DELETE FROM users WHERE id = 1;
-- Автоматично видалятимуться всі його mood_entries!
```

---

## 💾 Експорт/Імпорт Даних

### Експорт у CSV

```bash
sqlite3 mood_tracker.db
sqlite> .mode csv
sqlite> .output mood_entries.csv
sqlite> SELECT * FROM mood_entries;
sqlite> .quit
```

### Резервна копія БД

```bash
# Створити резервну копію
cp mood_tracker.db mood_tracker.db.backup

# Відновити з резервної копії
cp mood_tracker.db.backup mood_tracker.db
```

---

## 📊 Розширення БД (Майбутні Можливості)

Можна розширити БД додаванням нових таблиць:

```sql
-- Таблиця для цілей (goals)
CREATE TABLE goals (
    id INTEGER PRIMARY KEY,
    user_id INTEGER NOT NULL,
    goal_text TEXT NOT NULL,
    target_mood_level INTEGER,
    created_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Таблиця для активностей
CREATE TABLE activities (
    id INTEGER PRIMARY KEY,
    mood_entry_id INTEGER NOT NULL,
    activity_name TEXT,
    FOREIGN KEY (mood_entry_id) REFERENCES mood_entries(id)
);

-- Таблиця для тегів
CREATE TABLE tags (
    id INTEGER PRIMARY KEY,
    mood_entry_id INTEGER NOT NULL,
    tag_name TEXT,
    FOREIGN KEY (mood_entry_id) REFERENCES mood_entries(id)
);
```

---

## ⚙️ Технічні Деталі

- **Тип БД:** SQLite 3
- **Драйвер:** org.xerial:sqlite-jdbc:3.44.0.0
- **Кодування:** UTF-8
- **Дата-час:** ISO 8601 формат (YYYY-MM-DD HH:MM:SS)
- **Розмір файлу:** ~28 KB (з даними)

---

**Остання оновлення:** 15 грудня 2024
