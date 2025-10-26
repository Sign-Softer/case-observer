-- V3__create_notification_settings_table.sql
-- Create notification_settings table for storing per-case notification preferences

CREATE TABLE notification_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    case_id BIGINT NOT NULL UNIQUE,
    notification_interval_minutes INT NOT NULL DEFAULT 60,
    email_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    sms_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    notify_on_hearing_changes BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_status_changes BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_party_changes BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_procedural_stage_changes BOOLEAN NOT NULL DEFAULT TRUE,
    last_checked_at TIMESTAMP NULL,
    next_check_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_notification_settings_case_id 
        FOREIGN KEY (case_id) REFERENCES court_case(id) ON DELETE CASCADE,
    
    CONSTRAINT chk_notification_interval_positive 
        CHECK (notification_interval_minutes > 0)
);

-- Create index for efficient querying of cases ready for check
CREATE INDEX idx_notification_settings_next_check 
    ON notification_settings(next_check_at);

-- Create index for active monitoring queries (MySQL doesn't support partial indexes)
CREATE INDEX idx_notification_settings_case_id 
    ON notification_settings(case_id);
