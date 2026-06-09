package com.mindoc.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Minimal localization helper for English/Ukrainian.
 */
public final class I18n {
    private static String language = "English";
    private static final Map<String, String> UK = new HashMap<>();

    static {
        UK.put("dashboard", "Дашборд");
        UK.put("today_mood", "Сьогоднішній настрій");
        UK.put("no_entry_yet", "Ще немає запису");
        UK.put("this_week", "Цього тижня");
        UK.put("average_mood", "Середній настрій");
        UK.put("current_streak", "Поточна серія");
        UK.put("days", "днів");
        UK.put("quick_tips", "Швидкі поради");
        UK.put("tip_1", "💡 Відстежуйте настрій регулярно, щоб бачити патерни");
        UK.put("tip_2", "🎓 Спробуйте новий курс або вправу сьогодні");
        UK.put("tip_3", "🧘 Практикуйте щоденний майндфулнес для кращого самопочуття");
        UK.put("track_mood", "Відстеження настрою");
        UK.put("save_entry", "Зберегти запис");
        UK.put("how_feeling", "Як ви себе почуваєте?");
        UK.put("very_bad", "Дуже погано");
        UK.put("okay", "Нормально");
        UK.put("excellent", "Чудово");
        UK.put("trigger_mood", "Що спричинило цей настрій?");
        UK.put("symptoms_q", "Чи відчуваєте щось із цього?");
        UK.put("additional_notes", "Додаткові нотатки");
        UK.put("saved_success", "Запис настрою успішно збережено!");
        UK.put("success", "Успіх");
        UK.put("error", "Помилка");
        UK.put("profile", "Профіль користувача");
        UK.put("member_since", "Користувач з");
        UK.put("current_status", "Поточний статус:");
        UK.put("active", "✅ Активний");
        UK.put("personal_info", "📋 Особиста інформація");
        UK.put("first_name", "Ім'я");
        UK.put("last_name", "Прізвище");
        UK.put("date_of_birth", "Дата народження");
        UK.put("gender", "Стать");
        UK.put("bio", "Про себе (біо)");
        UK.put("edit_profile", "✏️ Редагувати профіль");
        UK.put("cancel", "❌ Скасувати");
        UK.put("save_changes", "💾 Зберегти зміни");
        UK.put("analytics", "📊 Аналітика та статистика");
        UK.put("best_mood", "🏆 Найкращий настрій");
        UK.put("tough_day", "📉 Важкий день");
        UK.put("total_entries", "📝 Всього записів");
        UK.put("trend_30", "Тренд настрою (останні 30 днів)");
        UK.put("weekly_summary", "Тижневий підсумок");
        UK.put("learn_courses", "📚 Курси КПТ");
        UK.put("available_courses", "Доступні курси");
        UK.put("category", "Категорія:");
        UK.put("course_details", "Деталі курсу");
        UK.put("select_course", "Оберіть курс для перегляду деталей");
        UK.put("description", "Опис:");
        UK.put("course_content", "Вміст курсу:");
        UK.put("courses_title", "Курси");
        UK.put("no_desc_course", "Опис цього курсу поки відсутній.");
        UK.put("no_content_course", "Вміст курсу поки недоступний. Використайте опис і за потреби зверніться до фахівця.");
        UK.put("start_course", "▶ Почати курс");
        UK.put("in_progress", "⏳ В процесі");
        UK.put("course_completed", "✓ Курс завершено");
        UK.put("course_progress", "Прогрес курсу");
        UK.put("marked_in_progress", "Позначено як в процесі.\nПозначте як завершений, коли закінчите.");
        UK.put("mark_completed", "Позначити завершеним");
        UK.put("keep_in_progress", "Залишити в процесі");
        UK.put("exercises", "💪 Вправи");
        UK.put("available_exercises", "Доступні вправи");
        UK.put("exercise_details", "Деталі вправи");
        UK.put("select_exercise", "Оберіть вправу для перегляду деталей");
        UK.put("instructions", "Покрокові інструкції:");
        UK.put("duration", "Тривалість");
        UK.put("start_exercise", "▶ Почати вправу");
        UK.put("exercise_completed", "✓ Вправу завершено");
        UK.put("exercise_progress", "Прогрес вправи");
        UK.put("no_desc_exercise", "Опис цієї вправи поки відсутній.");
        UK.put("no_instr_exercise", "Покрокові інструкції поки недоступні.\n\n");
        UK.put("failed_update_progress", "Не вдалося оновити прогрес.");
        UK.put("failed_save_completion", "Не вдалося зберегти статус завершення.");
        UK.put("settings", "⚙️ Налаштування");
        UK.put("theme", "Тема:");
        UK.put("language", "Мова:");
        UK.put("text_size", "Розмір шрифту:");
        UK.put("notifications", "Увімкнути сповіщення");
        UK.put("apply", "Застосувати");
        UK.put("reset", "Скинути");
        UK.put("about_title", "Про MindDoc");
        UK.put("about_header", "MindDoc - Підтримка ментального здоров'я");
        UK.put("about_body", "MindDoc — це професійний десктопний застосунок для відстеження ментального здоров'я.\n\nФункції:\n• Щоденне відстеження настрою\n• Моніторинг симптомів\n• Навчання через курси КПТ\n• Практика вправ і стратегій\n• Персоналізовані рекомендації\n\nВерсія 2.0.0\n© 2026 MindDoc Team");
        UK.put("login", "Увійти");
        UK.put("register", "Реєстрація");
        UK.put("username", "Логін");
        UK.put("password", "Пароль");
        UK.put("email", "Ел. пошта");
        UK.put("confirm_password", "Підтвердіть пароль");
        UK.put("remember_me", "Запам'ятати мене");
        UK.put("create_account", "Створити акаунт");
        UK.put("demo_creds", "📝 Демо-дані:\nЛогін: demo | Пароль: demo\nАбо створіть новий акаунт");
        UK.put("subtitle", "Ваш помічник ментального здоров'я");
        UK.put("please_fill", "Будь ласка, заповніть всі поля");
        UK.put("invalid_credentials", "Невірний логін або пароль");
        UK.put("profile_updated", "Профіль успішно оновлено!");
        UK.put("failed_save_profile", "Не вдалося зберегти профіль: ");
        UK.put("editing", "⏸️ Редагування...");
        UK.put("analytics_trend", "Тренд настрою (останні 30 днів)");
        UK.put("no_mood_data", "Ще немає даних настрою");
    }

    private I18n() {}

    public static void setLanguage(String value) {
        language = value == null ? "English" : value;
    }

    public static boolean isUkrainian() {
        return "Українська".equalsIgnoreCase(language);
    }

    public static String t(String key, String english) {
        if (isUkrainian()) {
            return UK.getOrDefault(key, english);
        }
        return english;
    }
}
