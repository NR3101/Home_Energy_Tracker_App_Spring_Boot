CREATE TABLE  IF NOT EXISTS `user` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255),
    email VARCHAR(255) NOT NULL UNIQUE,
    address TEXT,
    alert_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    energy_alert_threshold DOUBLE NOT NULL DEFAULT 0.0
);