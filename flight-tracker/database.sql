CREATE DATABASE IF NOT EXISTS flight_tracker_db;
USE flight_tracker_db;

CREATE TABLE IF NOT EXISTS search_configs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    departure_city VARCHAR(10) NOT NULL,
    arrival_city VARCHAR(10) NOT NULL,
    preferred_date DATE NOT NULL,
    budget_threshold INT NOT NULL,
    flight_type VARCHAR(20) DEFAULT 'ALL',
    active TINYINT(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS price_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    config_id INT NOT NULL,
    flight_date DATE NOT NULL,
    airline VARCHAR(100) NOT NULL,
    flight_number VARCHAR(20) NOT NULL,
    departure_time TIME NOT NULL,
    arrival_time TIME NOT NULL,
    price_inr DECIMAL(10, 2) NOT NULL,
    is_direct BOOLEAN DEFAULT TRUE,
    stops_info VARCHAR(50) DEFAULT 'Direct',
    source_url TEXT NULL,
    scraped_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (config_id) REFERENCES search_configs(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS price_access_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    config_id INT NOT NULL,
    flight_number VARCHAR(50) NOT NULL,
    flight_date DATE NOT NULL,
    airline VARCHAR(100) NOT NULL,
    source_name VARCHAR(100) DEFAULT 'Google Flights Stream',
    accessed_url TEXT NOT NULL,
    price_received DECIMAL(10, 2) NOT NULL,
    accessed_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (config_id) REFERENCES search_configs(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS alert_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    config_id INT NOT NULL,
    flight_id INT NOT NULL,
    alert_type VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    triggered_price DECIMAL(10, 2) NOT NULL,
    sent_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (config_id) REFERENCES search_configs(id) ON DELETE CASCADE,
    FOREIGN KEY (flight_id) REFERENCES price_history(id) ON DELETE CASCADE
);
