# SQL-запити для Mood Tracker

Цей файл містить приклади основних SQL-запитів, які використовуються в застосунку.

## 1. Управління користувачами

### Створення нового користувача
```sql
INSERT INTO users (username)
VALUES ('Іван');
```

### Отримання користувача за ім'ям
```sql
SELECT id, username, created_at
FROM users
WHERE username = 'Іван';
```

### Отримання всіх користувачів
```sql
SELECT id, username, created_at
FROM users
ORDER BY username;
```

### Видалення користувача
```sql
DELETE FROM users
WHERE id = 1;
-- Каскадно видаляться всі записи настроїв цього користувача
```

## 2. Управління записами настроїв

### Додавання нового запису настрою (на день)
```sql
INSERT INTO mood_entries (user_id, mood_level, mood_emoji, note, entry_date)
VALUES (1, 8, '😊', 'Хороший день, багато енергії', DATE('now'))
ON CONFLICT(user_id, entry_date) DO UPDATE SET
    mood_level = excluded.mood_level,
    mood_emoji = excluded.mood_emoji,
    note = excluded.note,
    created_at = CURRENT_TIMESTAMP;
```

### Отримання записів користувача за всю історію
```sql
SELECT id, mood_level, mood_emoji, note, entry_date, created_at
FROM mood_entries
WHERE user_id = 1
ORDER BY entry_date DESC;
```

### Отримання запису за конкретну дату
```sql
SELECT id, mood_level, mood_emoji, note, entry_date
FROM mood_entries
WHERE user_id = 1 AND entry_date = '2025-12-15';
```

### Отримання записів за період
```sql
SELECT id, mood_level, mood_emoji, note, entry_date
FROM mood_entries
WHERE user_id = 1 AND entry_date BETWEEN '2025-12-01' AND '2025-12-31'
ORDER BY entry_date DESC;
```

### Видалення запису
```sql
DELETE FROM mood_entries
WHERE id = 5;
```

## 3. Статистика

### Середній рівень настрою
```sql
SELECT AVG(mood_level) as avg_mood
FROM mood_entries
WHERE user_id = 1;
```

### Загальна статистика
```sql
SELECT 
    AVG(mood_level) as avg_mood,
    MIN(mood_level) as min_mood,
    MAX(mood_level) as max_mood,
    COUNT(*) as total_entries
FROM mood_entries
WHERE user_id = 1;
```

### Статистика по місяцях
```sql
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

### Найнижчий та найвищий настрій в місяці
```sql
SELECT 
    MIN(mood_level) as worst_mood,
    MAX(mood_level) as best_mood,
    DATE(entry_date) as best_day
FROM mood_entries
WHERE user_id = 1 AND STRFTIME('%Y-%m', entry_date) = '2025-12'
GROUP BY DATE(entry_date)
ORDER BY mood_level DESC;
```

### Кількість днів без запису
```sql
SELECT 
    JULIANDAY(DATE('now')) - JULIANDAY(MAX(entry_date)) as days_without_entry
FROM mood_entries
WHERE user_id = 1;
```

## 4. Рекомендації

### Отримання рекомендацій за рівнем настрою
```sql
SELECT id, recommendation_text, emoji
FROM recommendations
WHERE mood_range_min <= 8 AND mood_range_max >= 8;
```

### Всі рекомендації
```sql
SELECT id, mood_range_min, mood_range_max, recommendation_text, emoji
FROM recommendations
ORDER BY mood_range_min;
```

### Додавання нової рекомендації
```sql
INSERT INTO recommendations (mood_range_min, mood_range_max, recommendation_text, emoji)
VALUES (6, 7, 'Спробуй займатися спортом', '🏃');
```

### Оновлення рекомендації
```sql
UPDATE recommendations
SET recommendation_text = 'Позвони другу та поговори'
WHERE id = 3;
```

### Видалення рекомендації
```sql
DELETE FROM recommendations
WHERE id = 2;
```

## 5. Розширені аналітичні запити

### Тренд настрою за останні 30 днів
```sql
SELECT 
    entry_date,
    mood_level,
    AVG(mood_level) OVER (ORDER BY entry_date ROWS BETWEEN 7 PRECEDING AND CURRENT ROW) as moving_avg_7days
FROM mood_entries
WHERE user_id = 1 AND entry_date >= DATE('now', '-30 days')
ORDER BY entry_date;
```

### Дні з найнижчим та найвищим настроєм
```sql
SELECT 'Найнижчий' as type, mood_level, entry_date
FROM mood_entries
WHERE user_id = 1 AND mood_level = (
    SELECT MIN(mood_level) FROM mood_entries WHERE user_id = 1
)
UNION ALL
SELECT 'Найвищий' as type, mood_level, entry_date
FROM mood_entries
WHERE user_id = 1 AND mood_level = (
    SELECT MAX(mood_level) FROM mood_entries WHERE user_id = 1
);
```

### Розподіл записів по днях тижня
```sql
SELECT 
    CASE CAST(STRFTIME('%w', entry_date) AS INTEGER)
        WHEN 0 THEN 'Неділя'
        WHEN 1 THEN 'Понеділок'
        WHEN 2 THEN 'Вівторок'
        WHEN 3 THEN 'Середа'
        WHEN 4 THEN 'Четвер'
        WHEN 5 THEN 'П''ятниця'
        WHEN 6 THEN 'Субота'
    END AS day_of_week,
    COUNT(*) as entry_count,
    ROUND(AVG(mood_level), 2) as avg_mood
FROM mood_entries
WHERE user_id = 1
GROUP BY STRFTIME('%w', entry_date)
ORDER BY CAST(STRFTIME('%w', entry_date) AS INTEGER);
```

### Користувачі із найменшою кількістю записів
```sql
SELECT 
    u.id,
    u.username,
    COUNT(me.id) as entry_count,
    MAX(me.entry_date) as last_entry
FROM users u
LEFT JOIN mood_entries me ON u.id = me.user_id
GROUP BY u.id, u.username
HAVING COUNT(me.id) < 5
ORDER BY entry_count ASC;
```

### Активні користувачі за останній тиждень
```sql
SELECT 
    u.username,
    COUNT(me.id) as entries_this_week,
    ROUND(AVG(me.mood_level), 2) as avg_mood
FROM users u
JOIN mood_entries me ON u.id = me.user_id
WHERE me.entry_date >= DATE('now', '-7 days')
GROUP BY u.id, u.username
ORDER BY entries_this_week DESC;
```

## 6. Утилітарні запити

### Видалити всі записи для користувача
```sql
DELETE FROM mood_entries
WHERE user_id = 1;
```

### Скинути счетчик ID (для відновлення)
```sql
DELETE FROM sqlite_sequence
WHERE name = 'mood_entries';
```

### Перевірити цілісність бази даних
```sql
PRAGMA integrity_check;
```

### Отримати розмір бази даних
```sql
SELECT page_count * page_size as db_size_bytes
FROM pragma_page_count(), pragma_page_size();
```

---

## Примітки:

1. **SQLite функції дати:**
   - `DATE('now')` - поточна дата
   - `DATETIME('now')` - поточна дата й час
   - `DATE('now', '-7 days')` - 7 днів тому
   - `STRFTIME('%Y-%m', date)` - форматування дати

2. **Конфлікти при вставці:**
   - `ON CONFLICT ... DO UPDATE` - замінити при конфлікті унікальності
   - Використовується для оновлення запису на день

3. **Типи даних SQLite:**
   - `INTEGER` - цілі числа
   - `TEXT` - текст
   - `DATE` - дата
   - `TIMESTAMP` - дата й час

---

Остання оновлення: 15 грудня 2025
