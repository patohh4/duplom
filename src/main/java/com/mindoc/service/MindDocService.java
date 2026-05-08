package com.mindoc.service;

import com.mindoc.database.DatabaseManager;
import com.mindoc.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main service class providing access to all repositories
 */
public class MindDocService {
    private static final Logger logger = LoggerFactory.getLogger(MindDocService.class);
    
    private DatabaseManager databaseManager;
    private UserRepository userRepository;
    private MoodEntryRepository moodEntryRepository;
    private SymptomRepository symptomRepository;
    private CBTCourseRepository cbtCourseRepository;
    private ExerciseRepository exerciseRepository;
    
    public MindDocService(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        initializeRepositories();
    }
    
    private void initializeRepositories() {
        try {
            userRepository = new UserRepository(databaseManager.getConnection());
            moodEntryRepository = new MoodEntryRepository(databaseManager.getConnection());
            symptomRepository = new SymptomRepository(databaseManager.getConnection());
            cbtCourseRepository = new CBTCourseRepository(databaseManager.getConnection());
            exerciseRepository = new ExerciseRepository(databaseManager.getConnection());
            
            logger.info("All repositories initialized successfully");
        } catch (Exception e) {
            logger.error("Error initializing repositories", e);
            throw new RuntimeException("Failed to initialize repositories", e);
        }
    }
    
    // Repository accessors
    public UserRepository getUserRepository() {
        return userRepository;
    }
    
    public MoodEntryRepository getMoodEntryRepository() {
        return moodEntryRepository;
    }
    
    public SymptomRepository getSymptomRepository() {
        return symptomRepository;
    }
    
    public CBTCourseRepository getCBTCourseRepository() {
        return cbtCourseRepository;
    }
    
    public ExerciseRepository getExerciseRepository() {
        return exerciseRepository;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}
