# MindDoc - Getting Started Guide

## Installation & Setup

### Prerequisites
- **Java 21 LTS** (Download from [openjdk.java.net](https://openjdk.java.net/))
- **Maven 3.6+** (Download from [maven.apache.org](https://maven.apache.org/))
- **Git** (Optional, for cloning the repository)

### Verify Installation

Check Java version:
```bash
java -version
```

Should output Java 21.x.x

Check Maven version:
```bash
mvn -version
```

## Building MindDoc

### Option 1: Using Maven CLI

1. Navigate to project directory:
```bash
cd /path/to/duplom
```

2. Build the project:
```bash
mvn clean package -DskipTests
```

3. Run the application:
```bash
java -jar target/mood-tracker-app-1.0.0.jar
```

Or use JavaFX Maven plugin:
```bash
mvn javafx:run
```

### Option 2: Using the Run Script (macOS/Linux)

1. Make the script executable:
```bash
chmod +x run.sh
```

2. Run the script:
```bash
./run.sh
```

### Option 3: Using IDE (IntelliJ IDEA or Eclipse)

1. Open the project folder in your IDE
2. Right-click on `MindDocApp.java`
3. Select "Run 'MindDocApp.main()'"

## First Time Setup

### Creating a Default User

When you first launch MindDoc, you'll need to create or select a user. The default setup includes:

1. **Dashboard** - Overview of your mental health
2. **Track Mood** - Log your daily mood
3. **Symptoms** - Monitor mental health symptoms
4. **Learn** - Access CBT courses
5. **Exercises** - Practice coping strategies
6. **Analytics** - View your progress

### Pre-loaded Content

The application comes with:
- 12 common mental health symptoms
- 5 foundational CBT courses
- 6 guided exercises
- Database initialized and ready to use

## Daily Usage

### Tracking Your Mood

1. Click the **"Track Mood"** tab
2. Use the slider to select your mood (1-10)
3. Watch the emoji change based on your mood level
4. Select the **context** (what triggered this mood)
5. Check any **symptoms** you're experiencing
6. Add **notes** about your feelings
7. Click **"Save Entry"**

### Viewing Your Dashboard

1. Open the **"Dashboard"** tab
2. See today's mood summary
3. View weekly statistics
4. Check your current streak

### Learning & Practice

1. Go to the **"Learn"** tab for CBT courses
2. Choose courses by difficulty or category
3. Visit **"Exercises"** for practical coping strategies
4. Track your progress in **"Analytics"**

## Troubleshooting

### Issue: "Java command not found"
**Solution**: Ensure Java 21 is installed and added to PATH
```bash
export PATH="/path/to/java21/bin:$PATH"
```

### Issue: "Maven command not found"
**Solution**: Ensure Maven is installed and added to PATH
```bash
export PATH="/path/to/maven/bin:$PATH"
```

### Issue: "JavaFX not found"
**Solution**: Maven should automatically download JavaFX. If not, run:
```bash
mvn clean install
```

### Issue: Application won't start
**Solution**: Check the logs in the console. Common causes:
- Database file corruption: Delete `mindoc.db` and restart
- Port already in use: Change port in configuration
- Missing dependencies: Run `mvn clean install`

### Issue: Database Error
**Solution**: 
1. Stop the application
2. Delete `mindoc.db` file
3. Restart the application (database will be recreated)

## File Locations

### Database
- **Location**: `mindoc.db` (same directory as application)
- **Type**: SQLite 3
- **Size**: Grows with usage (typically < 10MB)

### Configuration
- **Location**: `application.properties` (will be created)
- **Format**: Java Properties

### Logs
- **Location**: Console output (stored in target/logs)
- **Format**: SLF4J with Logback

## Advanced Configuration

### Changing Database Location

Edit the `DatabaseManager.java`:
```java
private static final String DATABASE_URL = "jdbc:sqlite:/custom/path/mindoc.db";
```

### Enabling Debug Logging

Set environment variable:
```bash
export LOG_LEVEL=DEBUG
```

### Custom Theme

Modify `MindDocTheme.java`:
```java
public static final String PRIMARY = "#your-color";
```

## Performance Tips

1. **Clear old entries**: Archives entries older than 1 year
2. **Export data**: Regular backups prevent data loss
3. **Update regularly**: Keep the application and Java updated
4. **Monitor database**: Check file size periodically

## Data Export

To export your data:

1. Your data is stored in `mindoc.db`
2. Copy this file to backup location
3. Use any SQLite viewer to access data

## Privacy & Security

- All data is stored **locally on your device**
- No data is sent to external servers
- Database can be encrypted (future enhancement)
- Backup regularly for safety

## Getting Help

### Check the Logs
```bash
tail -f target/logs/application.log
```

### Common Error Messages

| Error | Solution |
|-------|----------|
| "No suitable driver found" | Update JDBC drivers in pom.xml |
| "Database locked" | Close other instances of the app |
| "OutOfMemoryError" | Increase JVM heap: `java -Xmx1024m -jar ...` |

## System Information

Display system info for debugging:
```bash
mvn clean compile
java -XshowSettings:vm -version
```

## Next Steps

1. **Explore Features**: Spend time in each tab
2. **Add Daily Entry**: Start tracking your mood
3. **Try an Exercise**: Practice from the exercises tab
4. **Review Analytics**: See your patterns over time
5. **Customize**: Adjust settings to your preferences

## Additional Resources

- [JavaFX Documentation](https://openjfx.io/)
- [SQLite Documentation](https://www.sqlite.org/docs.html)
- [Maven Handbook](https://maven.apache.org/guides/)
- [Mental Health Resources](https://www.mentalhealth.gov/)

## Version Information

- **Current Version**: 2.0.0
- **Java Version**: 21 LTS
- **JavaFX Version**: 21.0.1
- **Last Updated**: 2026-04-01

---

**Remember**: MindDoc is a tracking and learning tool. If you're experiencing mental health crisis, please contact a healthcare professional or crisis hotline immediately.

For support or questions, please create an issue in the project repository.
