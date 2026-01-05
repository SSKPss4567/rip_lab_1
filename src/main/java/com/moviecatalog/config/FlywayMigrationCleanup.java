package com.moviecatalog.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@ConditionalOnProperty(name = "spring.flyway.enabled", havingValue = "true", matchIfMissing = false)
public class FlywayMigrationCleanup {
    
    private static final Logger logger = LoggerFactory.getLogger(FlywayMigrationCleanup.class);
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @PostConstruct
    public void cleanupOldMigrationHistory() {
        try {
            logger.info("Cleaning up old Liquibase migration history...");
            
            jdbcTemplate.execute("DROP TABLE IF EXISTS databasechangelog CASCADE");
            jdbcTemplate.execute("DROP TABLE IF EXISTS databasechangeloglock CASCADE");
            
            logger.info("Old migration history cleaned successfully");
        } catch (Exception e) {
            logger.warn("Could not clean up old migration history: {}", e.getMessage());
        }
    }
}

