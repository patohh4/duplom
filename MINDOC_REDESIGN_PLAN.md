# MindDoc Redesign Plan

## Фаза 1: Розширення моделей даних

### Нові сутності:
1. **Symptom** - симптоми психічного здоров'я
2. **CBTCourse** - курси когнітивно-поведінкової терапії
3. **Exercise** - вправи та стратегії
4. **Assessment** - оцінки емоційного здоров'я
5. **SymptomLog** - реєстрація симптомів

### Розширення MoodEntry:
- Додати список симптомів
- Додати контекст (причину настрою)
- Додати інтенсивність симптомів

## Фаза 2: Функціональність

### Основні модулі:
1. **Mood Tracking** - розширена реєстрація настрою з контекстом
2. **Symptom Assessment** - відстеження симптомів депресії, тривоги тощо
3. **CBT Learning** - бібліотека курсів та вправ
4. **Analytics & Reports** - детальна аналітика з експортом
5. **Recommendations** - персоналізовані рекомендації

## Фаза 3: UI/UX Дизайн

### Палітра кольорів:
- Primary: #667eea (фіолетовий)
- Secondary: #764ba2 (темний фіолетовий)
- Accent: #f093fb (рожевий)
- Background: #f7fafc (світло-сірий)
- Text: #2d3748 (темний)

### Нові екрани:
1. **Home Dashboard** - огляд здоров'я за день/тиждень
2. **Mood Tracking** - детальна реєстрація з симптомами
3. **Symptom Tracker** - монітор специфічних симптомів
4. **Courses** - навчальні матеріали КПТ
5. **Exercises** - практичні вправи
6. **Analytics** - графіки та звіти
7. **Settings** - налаштування та профіль

## Фаза 4: Структура проекту

```
src/main/java/com/mindoc/
├── MindDocApp.java
├── database/
│   ├── DatabaseManager.java
│   └── DatabaseInitializer.java
├── model/
│   ├── User.java
│   ├── MoodEntry.java
│   ├── Symptom.java
│   ├── SymptomLog.java
│   ├── CBTCourse.java
│   ├── Exercise.java
│   ├── Assessment.java
│   └── Recommendation.java
├── repository/
│   ├── UserRepository.java
│   ├── MoodEntryRepository.java
│   ├── SymptomRepository.java
│   ├── CBTCourseRepository.java
│   ├── ExerciseRepository.java
│   └── AssessmentRepository.java
├── service/
│   ├── MindDocService.java
│   ├── AnalyticsService.java
│   ├── RecommendationEngine.java
│   └── AssessmentService.java
├── ui/
│   ├── MainApplication.java
│   ├── themes/
│   │   └── MindDocTheme.java
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
│   │   ├── AnalyticsPanel.java
│   │   └── ReportsPanel.java
│   └── common/
│       ├── BasePanel.java
│       ├── MoodChart.java
│       └── ProgressIndicator.java
└── util/
    ├── DateUtils.java
    └── ColorUtils.java
```

## Фаза 5: Прогрес

- [ ] Оновлення pom.xml (Java 21, нові залежності)
- [ ] Розширення моделей даних
- [ ] Оновлення DatabaseManager
- [ ] Реалізація нових сервісів
- [ ] Оновлення UI компонентів
- [ ] Імплементація аналітики
- [ ] Тестування
