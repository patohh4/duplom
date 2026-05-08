# 📦 Деплойм та Дистрибуція Mood Tracker

Цей документ описує як упакувати, розповсюджувати та запустити Mood Tracker на різних платформах.

## 1. Упаковування проекту

### Варіант 1: Стандартний JAR файл
```bash
cd /Users/stasrusnak/Desktop/duplom
mvn clean package
```

Результат: `target/mood-tracker-app-1.0.0.jar`

### Варіант 2: Shaded JAR (з всіма залежностями)
```bash
mvn clean package -DskipTests
```

Результат: `target/mood-tracker-app-1.0.0.jar` (з усіма залежностями)

Переваги:
- Один файл з усім необхідним
- Легше розповсюджувати
- Не потребує встановлення залежностей

## 2. Запуск на різних платформах

### macOS
```bash
# Запуск JAR файлу
java -jar mood-tracker-app-1.0.0.jar

# Запуск з опціями JVM
java -Xmx512M -jar mood-tracker-app-1.0.0.jar
```

### Windows
```bash
# Запуск з командного рядка
java -jar mood-tracker-app-1.0.0.jar

# Або двічі натисніть на mood-tracker-app-1.0.0.jar (якщо встановлено Java)
```

### Linux
```bash
# Встановіть Java 17+ (якщо не встановлено)
sudo apt-get install default-jdk

# Запуск
java -jar mood-tracker-app-1.0.0.jar
```

## 3. Вимоги до системи

### Мінімум
- **Java:** JDK 17 або новіше
- **Оперативна пам'ять:** 256 MB
- **Місце на диску:** 150 MB (для JAR + БД)
- **ОС:** macOS 10.13+, Windows 7+, Linux (будь-яка)

### Рекомендовано
- **Java:** JDK 21 або новіше
- **Оперативна пам'ять:** 512 MB або більше
- **Місце на диску:** 500 MB

## 4. Встановлення Java

### macOS (з використанням Homebrew)
```bash
brew install openjdk@17
# або
brew install openjdk@21
```

### Windows (з використанням SDKMAN)
```powershell
# Завантажте інсталер з https://www.oracle.com/java/technologies/downloads/
# Або використайте Chocolatey
choco install openjdk17
```

### Linux (Ubuntu/Debian)
```bash
sudo apt-get update
sudo apt-get install openjdk-17-jdk
```

### Linux (RHEL/CentOS)
```bash
sudo yum install java-17-openjdk
```

## 5. Оптимізація для дистрибуції

### Створити виконавчий скрипт для Linux/macOS

Створіть файл `run.sh`:
```bash
#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
java -jar "$DIR/mood-tracker-app-1.0.0.jar"
```

Зробіть його виконавчим:
```bash
chmod +x run.sh
```

### Створити виконавчий скрипт для Windows

Створіть файл `run.bat`:
```batch
@echo off
cd /d %~dp0
java -jar mood-tracker-app-1.0.0.jar
pause
```

### Пакування з інструкціями

Структура дистрибутиву:
```
mood-tracker-1.0.0/
├── mood-tracker-app-1.0.0.jar
├── run.sh                          (для macOS/Linux)
├── run.bat                         (для Windows)
├── README.txt                      (коротка інструкція)
└── INSTALL.txt                     (деталі встановлення)
```

## 6. Створення Installer для Windows

### Використання Inno Setup

Створіть файл `installer.iss`:
```ini
[Setup]
AppName=Mood Tracker
AppVersion=1.0.0
DefaultDirName={pf}\Mood Tracker
DefaultGroupName=Mood Tracker
OutputDir=.
OutputBaseFilename=MoodTrackerInstaller

[Files]
Source: "mood-tracker-app-1.0.0.jar"; DestDir: "{app}"
Source: "run.bat"; DestDir: "{app}"
Source: "README.txt"; DestDir: "{app}"; Flags: isreadme

[Icons]
Name: "{group}\Mood Tracker"; Filename: "{app}\run.bat"
Name: "{desktop}\Mood Tracker"; Filename: "{app}\run.bat"
```

## 7. Контейнеризація (Docker)

### Dockerfile

```dockerfile
FROM openjdk:17-slim

WORKDIR /app

COPY mood-tracker-app-1.0.0.jar .

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "mood-tracker-app-1.0.0.jar"]
```

### Збірка та запуск Docker образу
```bash
# Збірка
docker build -t mood-tracker:1.0.0 .

# Запуск
docker run -v /path/to/data:/app/data mood-tracker:1.0.0
```

## 8. Оновлення до нової версії

### Процес оновлення

1. **Резервна копія БД:**
   ```bash
   cp mood_tracker.db mood_tracker.db.backup
   ```

2. **Заміна JAR файлу:**
   ```bash
   # Закрийте застосунок
   # Замініть mood-tracker-app-1.0.0.jar на новий версії
   ```

3. **Запуск оновленої версії:**
   ```bash
   java -jar mood-tracker-app-1.0.0.jar
   ```

4. **Перевірка даних:**
   - Перевірте, що ваші дані все ще там
   - Рекомендації можуть бути оновлені

## 9. Параметри JVM для оптимізації

### Для менших систем
```bash
java -Xmx256M -Xms128M -jar mood-tracker-app-1.0.0.jar
```

### Для більших систем
```bash
java -Xmx2G -Xms512M -jar mood-tracker-app-1.0.0.jar
```

### Для дебаґування
```bash
java -Xmx512M -Xms256M -Ddebug=true -jar mood-tracker-app-1.0.0.jar
```

## 10. Розповсюджування

### Варіант 1: GitHub Releases
1. Пушим кад на GitHub
2. Створюємо Release
3. Прикріплюємо mood-tracker-app-1.0.0.jar

### Варіант 2: Архіви
```bash
# Для macOS/Linux
tar -czf mood-tracker-1.0.0-macos.tar.gz mood-tracker-app-1.0.0.jar run.sh

# Для Windows
zip mood-tracker-1.0.0-windows.zip mood-tracker-app-1.0.0.jar run.bat
```

### Варіант 3: Maven Central Repository

Додайте в `pom.xml`:
```xml
<distributionManagement>
    <snapshotRepository>
        <id>sonatype-nexus-snapshots</id>
        <url>https://oss.sonatype.org/content/repositories/snapshots</url>
    </snapshotRepository>
    <repository>
        <id>sonatype-nexus-staging</id>
        <url>https://oss.sonatype.org/service/local/staging/deploy/maven2/</url>
    </repository>
</distributionManagement>
```

Потім:
```bash
mvn deploy
```

## 11. Метрики розповсюджування

### Розмір файлів

| Компонент | Розмір |
|-----------|--------|
| mood-tracker-app-1.0.0.jar | ~40 MB |
| mood_tracker.db (порожня) | 100 KB |
| З 1000 записами | 500 KB |
| З 100000 записами | 10 MB |

## 12. Помилки при деплойменту

### ❌ "java: command not found"
```bash
# Встановіть Java
# macOS: brew install openjdk@17
# Ubuntu: sudo apt-get install openjdk-17-jdk
```

### ❌ "Cannot allocate memory"
```bash
# Зменшіть розмір heap
java -Xmx256M -jar mood-tracker-app-1.0.0.jar
```

### ❌ "Permission denied"
```bash
# Linux/macOS
chmod +x run.sh
```

### ❌ Database locked
```bash
# Закрийте усі екземпляри застосунку
# Видаліть файл .db-wal якщо існує
rm mood_tracker.db-wal
```

## 13. Перевірка перед дистрибуцією

Чек-лист:
- [ ] Проект компілюється без помилок
- [ ] Усі тести проходять
- [ ] JAR файл має розмір ~40 MB
- [ ] БД створюється при першому запуску
- [ ] Основні функції працюють
- [ ] Логи записуються коректно
- [ ] Нема утікання пам'яті
- [ ] Застосунок закривається коректно

## 14. Поддержка користувачів

### Звичайні питання
1. Як запустити застосунок?
   → java -jar mood-tracker-app-1.0.0.jar

2. Де зберігаються мої дані?
   → У файлі mood_tracker.db у робочій директорії

3. Як оновити версію?
   → Замініть JAR файл та запустіть знову (дані збережуться)

4. Як видалити мої дані?
   → Видаліть файл mood_tracker.db

---

**Остання оновлення:** 15 грудня 2025

Проект готов до дистрибуції та деплойменту! 🚀
