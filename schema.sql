-- SQL Migration Script for Flight Scanner Database
-- Run this script in phpMyAdmin, MySQL Workbench, or mysql CLI

USE flight_tracker_db;

-- Add missing columns safely using IF NOT EXISTS
ALTER TABLE search_configs ADD COLUMN IF NOT EXISTS alert_method VARCHAR(20) DEFAULT 'NONE';
ALTER TABLE search_configs ADD COLUMN IF NOT EXISTS smtp_server VARCHAR(100) NULL;
ALTER TABLE search_configs ADD COLUMN IF NOT EXISTS smtp_port INT DEFAULT 587;
ALTER TABLE search_configs ADD COLUMN IF NOT EXISTS smtp_user VARCHAR(100) NULL;
ALTER TABLE search_configs ADD COLUMN IF NOT EXISTS smtp_pass_encrypted VARCHAR(255) NULL;
ALTER TABLE search_configs ADD COLUMN IF NOT EXISTS alert_recipient VARCHAR(100) NULL;
ALTER TABLE search_configs ADD COLUMN IF NOT EXISTS telegram_bot_token_encrypted VARCHAR(255) NULL;
ALTER TABLE search_configs ADD COLUMN IF NOT EXISTS telegram_chat_id VARCHAR(50) NULL;
