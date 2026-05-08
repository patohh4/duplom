# 📋 SQL Запити для Mood Tracker

## 🔍 Практичні Приклади

### ➕ Додавання Даних

#### Додати нового користувача
```sql
INSERT INTO users (username) VALUES ('john_doe');
```

#### Додати запис про настрій
```sql
INSERT INTO mood_entries (user_id, mood_level, mood_emoji, note, entry_date)
VALUES (1, 8, '😊', 'Чудовий день на роботі', DATE('2024-12-15'));
```

#### Оновити запис про настрій (той же день)
```sql
UPDATE mood_entries 
SET mood_level = 9, mood_emoji = '😄', note = 'Ще краще!'
WHERE user_id = 1 AND entry_date = DATE('2024-12-15');
```

#### Вставити з автоматичним оновленням (ON CONFLICT)
```sql
INSERT INTO mood_entries (user_id, mood_level, mood_emoji, note, entry_date)
VALUES (1, 8, '😊', 'Чудовий день', DATE('2024-12-15'))
ON CONFLICT(user_id, entry_date) DO UPDATE SET
    mood_level = excluded.mood_level,
    mood_emoji = excluded.mood_emoji,
    note = excluded.note,
    created_at = CURRENT_TIMESTAMP;
```

---

### 📖 Читання Даних

#### Отримати всіх користувачів
```sql
SELECT id, username, created_at FROM users ORDER BY created_at DESC;
```

#### Отримати користувача за імʽям
```sql
SELECT * FROM users WHERE username = 'john_doe';
```

#### Отримати запис про настрій за датою
```sql
SELECT id, mood_level, mood_emoji, note, entry_date, created_at
FROM mood_entries
WHERE user_id = 1 AND entry_date = DATE('2024-12-15');
```

#### Отримати останній запис про настрій
```sql
SELECT id, mood_level, mood_emoji, note, entry_date
FROM mood_entries
WHERE user_id = 1
ORDER BY entry_date DESC
LIMIT 1;
```

#### Отримати всі записи користувача за місяць
```sql
SELECT id, mood_level, mood_emoji, note, entry_date
FROM mood_entries
WHERE user_id = 1 
  AND STRFTIME('%Y-%m', entry_date) = '2024-12'
ORDER BY entry_date DESC;
```

#### Отримати всі записи користувача за період
```sql
SELECT id, mood_level, mood_emoji, note, entry_date
FROM mood_entries
WHERE user_id = 1 
  AND entry_date BETWEEN DATE('2024-12-01') AND DATE('2024-12-31')
ORDER BY entry_date DESC;
```

#### Отримати рекомендацію за настроєм
```sql
-- Для настрою рівня 8
SELECT id, mood_range_min, mood_range_max, recommendation_text, emoji
FROM recommendations
WHERE 8 BETWEEN mood_range_min AND mood_range_max;
```

#### Отримати всі рекомендації
```sql
SELECT * FROM recommendations ORDER BY mood_range_min ASC;
```

---

### 📊 Статистика

#### Середній настрій за місяць
```sql
SELECT 
    STRFTIME('%Y-%m', entry_date) AS month,
    AVG(mood_level) AS average_mood
FROM mood_entries
WHERE user_id = 1
GROUP BY STRFTIME('%Y-%m', entry_date)
ORDER BY month DESC;
```

#### Повна статистика за місяць
```sql
SELECT 
    STRFTIME('%Y-%m', entry_date) AS month,
    COUNT(*) AS total_days,
    AVG(mood_level) AS avg_mood,
    MIN(mood_level) AS min_mood,
    MAX(mood_level) AS max_mood,
    ROUND(AVG(mood_level), 2) AS avg_rounded
FROM mood_entries
WHERE user_id = 1
GROUP BY STRFTIME('%Y-%m', entry_date)
ORDER BY month DESC;
```

#### Кількість днів за рівнем настрою
```sql
SELECT 
    CASE 
        WHEN mood_level BETWEEN 1 AND 3 THEN 'Дуже поганий (1-3)'
        WHEN mood_level BETWEEN 4 AND 5 THEN 'Поганий (4-5)'
        WHEN mood_level BETWEEN 6 AND 7 THEN 'Нейтральний (6-7)'
        WHEN mood_level BETWEEN 8 AND 9 THEN 'Хороший (8-9)'
        WHEN mood_level = 10 THEN 'Відмінний (10)'
    END AS mood_category,
    COUNT(*) AS count
FROM mood_entries
WHERE user_id = 1
GROUP BY mood_category;
```

#### Тренд настрою за останні 7 днів
```sql
SELECT 
    entry_date,
    mood_level,
    mood_emoji,
    LAG(mood_level) OVER (ORDER BY entry_date) AS previous_mood
FROM mood_entries
WHERE user_id = 1 AND entry_date >= DATE('now', '-7 days')
ORDER BY entry_date DESC;
```

#### Найкращий день за настроєм
```sql
SELECT 
    entry_date,
    mood_level,
    mood_emoji,
    note
FROM mood_entries
WHERE user_id = 1
ORDER BY mood_level DESC
LIMIT 1;
```

#### Найгірший день за настроєм
```sql
SELECT 
    entry_date,
    mood_level,
    mood_emoji,
    note
FROM mood_entries
WHERE user_id = 1
ORDER BY mood_level ASC
LIMIT 1;
```

---

### 🔗 Запити з Об'єднаннями (JOIN)

#### Користувач з його записами про настрій
```sql
SELECT 
    u.username,
    COUNT(m.id) AS total_entries,
    AVG(m.mood_level) AS avg_mood,
    MAX(m.mood_level) AS best_mood,
    MIN(m.mood_level) AS worst_mood
FROM users u
LEFT JOIN mood_entries m ON u.id = m.user_id
GROUP BY u.id, u.username;
```

#### Запис з рекомендацією
```sql
SELECT 
    m.entry_date,
    m.mood_level,
    m.mood_emoji,
    m.note,
    r.recommendation_text,
    r.emoji AS recommendation_emoji
FROM mood_entries m
LEFT JOIN recommendations r ON m.mood_level BETWEEN r.mood_range_min AND r.mood_range_max
WHERE m.user_id = 1
ORDER BY m.entry_date DESC;
```

#### Користувачі з їх останніми записами
```sql
SELECT 
    u.username,
    m.entry_date,
    m.mood_level,
    m.mood_emoji,
    m.note
FROM users u
LEFT JOIN (
    SELECT * FROM mood_entries 
    WHERE entry_date = (SELECT MAX(entry_date) FROM mood_entries me WHERE me.user_id = mood_entries.user_id)
) m ON u.id = m.user_id
ORDER BY u.username;
```

---

### 🗑️ Видалення Даних

#### Видалити запис про настрій
```sql
DELETE FROM mood_entries WHERE id = 1;
```

#### Видалити запис конкретного користувача за датою
```sql
DELETE FROM mood_entries 
WHERE user_id = 1 AND entry_date = DATE('2024-12-15');
```

#### Видалити користувача (каскадне видалення записів)
```sql
DELETE FROM users WHERE id = 1;
-- Автоматично видалятимуться всі його mood_entries!
```

#### Видалити всі записи за місяцем
```sql
DELETE FROM mood_entries 
WHERE user_id = 1 
  AND STRFTIME('%Y-%m', entry_date) = '2024-12';
```

---

### ✏️ Редагування Даних

#### Змінити користувача
```sql
UPDATE users SET username = 'jane_doe' WHERE id = 1;
```

#### Збільшити настрій на 1
```sql
UPDATE mood_entries 
SET mood_level = mood_level + 1
WHERE mood_level < 10 AND user_id = 1;
```

#### Додати нотатку до запису
```sql
UPDATE mood_entries 
SET note = 'Чудовий день!'
WHERE user_id = 1 AND entry_date = DATE('2024-12-15');
```

#### Оновити множину записів
```sql
UPDATE mood_entries
SET mood_emoji = '😊'
WHERE user_id = 1 AND mood_level >= 8;
```

---

### 🔍 Інші Корисні Запити

#### Кількість записів на користувача
```sql
SELECT 
    u.id,
    u.username,
    COUNT(m.id) AS mood_entries_count
FROM users u
LEFT JOIN mood_entries m ON u.id = m.user_id
GROUP BY u.id, u.username
ORDER BY mood_entries_count DESC;
```

#### Дні без записів про настрій (пропуски)
```sql
SELECT DATE('2024-12-01', '+' || rowid || ' day') as missing_date
FROM (
    WITH RECURSIVE dates(date) AS (
        SELECT DATE('2024-12-01')
        UNION ALL
        SELECT DATE(date, '+1 day')
        FROM dates
        WHERE date < DATE('2024-12-31')
    )
    SELECT date FROM dates
    WHERE date NOT IN (SELECT entry_date FROM mood_entries WHERE user_id = 1)
);
```

#### Послідовні дні з одним рівнем настрою
```sql
SELECT 
    entry_date,
    mood_level,
    COUNT(*) OVER (
        PARTITION BY mood_level 
        ORDER BY entry_date
    ) AS consecutive_days
FROM mood_entries
WHERE user_id = 1
ORDER BY entry_date;
```

#### Порівняння настрою з попередніми днями
```sql
SELECT 
    entry_date,
    mood_level,
    LAG(mood_level) OVER (ORDER BY entry_date) AS previous_day_mood,
    mood_level - LAG(mood_level) OVER (ORDER BY entry_date) AS change
FROM mood_entries
WHERE user_id = 1
ORDER BY entry_date DESC;
```

---

## 🧪 Тестові Дані

### Скрипт для вставлення тестових даних

```sql
-- Додати тестових користувачів
INSERT INTO users (username) VALUES ('test_user_1');
INSERT INTO users (username) VALUES ('test_user_2');
INSERT INTO users (username) VALUES ('test_user_3');

-- Додати тестові записи про настрій
INSERT INTO mood_entries (user_id, mood_level, mood_emoji, note, entry_date) VALUES 
(1, 5, '😐', 'Середній день', DATE('2024-12-15')),
(1, 7, '🙂', 'Нормально', DATE('2024-12-14')),
(1, 9, '😄', 'Чудовий день', DATE('2024-12-13')),
(1, 3, '😞', 'Поганий день', DATE('2024-12-12')),
(1, 8, '😊', 'Добре', DATE('2024-12-11')),
(2, 6, '😐', 'Так собі', DATE('2024-12-15')),
(2, 4, '😕', 'Не дуже', DATE('2024-12-14')),
(3, 10, '😄', 'Найкращий день!', DATE('2024-12-15'));
```

---

## 📊 Експорт Даних

### Експортувати результати у CSV

```bash
# Через SQLite CLI
sqlite3 mood_tracker.db

sqlite> .mode csv
sqlite> .output mood_entries.csv
sqlite> SELECT * FROM mood_entries;
sqlite> .quit

# Переглянути CSV
cat mood_entries.csv
```

### Експортувати SQL дамп

```bash
# Створити SQL дамп всієї БД
sqlite3 mood_tracker.db .dump > backup.sql

# Видалити БД та відновити з дампу
rm mood_tracker.db
sqlite3 mood_tracker.db < backup.sql
```

---

## ⚡ Поради для Оптимізації

### Створення Індексів
```sql
-- Індекс для швидшого пошуку по user_id
CREATE INDEX idx_mood_entries_user_id ON mood_entries(user_id);

-- Індекс для швидшого пошуку по даті
CREATE INDEX idx_mood_entries_date ON mood_entries(entry_date);

-- Комбінований індекс
CREATE INDEX idx_mood_entries_user_date ON mood_entries(user_id, entry_date);
```

### Перевірка Індексів
```sql
-- Переглянути всі індекси
SELECT * FROM sqlite_master WHERE type = 'index';

-- Переглянути вибір плану запиту
EXPLAIN QUERY PLAN SELECT * FROM mood_entries WHERE user_id = 1;
```

---

## 🛠️ Утиліти для Роботи з БД

### Резервна копія БД
```bash
# Ручна резервна копія
cp mood_tracker.db mood_tracker_backup_$(date +%Y%m%d_%H%M%S).db

# Автоматична резервна копія
sqlite3 mood_tracker.db ".backup 'mood_tracker_auto.db'"
```

### Перевірка Цілісності БД
```bash
# Перевірити цілісність
sqlite3 mood_tracker.db "PRAGMA integrity_check;"

# Вивести статистику
sqlite3 mood_tracker.db "PRAGMA database_list;"
```

---

**Останнє оновлення:** 15 грудня 2024
