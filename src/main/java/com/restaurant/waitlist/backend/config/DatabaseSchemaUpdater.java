package com.restaurant.waitlist.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;

@Component
public class DatabaseSchemaUpdater {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void updateUserSchema() {
        jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verified boolean NOT NULL DEFAULT false");
        jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS enabled boolean NOT NULL DEFAULT true");
        jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS is_active boolean NOT NULL DEFAULT true");
        jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS deleted_at timestamp NULL");
        jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN is_active SET DEFAULT true");
        jdbcTemplate.execute("UPDATE users SET is_active = true WHERE is_active IS NULL");
        jdbcTemplate.execute("UPDATE users SET enabled = true WHERE enabled IS NULL");
        jdbcTemplate.execute("UPDATE users SET email_verified = false WHERE email_verified IS NULL");
    }
}
