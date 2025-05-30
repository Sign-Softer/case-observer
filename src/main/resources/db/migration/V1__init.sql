-- V1__init.sql

-- User Table
CREATE TABLE user
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(255) NOT NULL UNIQUE,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL, -- Ensure this is hashed (e.g., bcrypt)
    created_at TIMESTAMP              DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_active  BOOLEAN                DEFAULT TRUE,
    role       ENUM ('ADMIN', 'USER') DEFAULT 'USER',
    INDEX idx_username (username),
    INDEX idx_email (email)
);

-- Court Case Table
CREATE TABLE court_case
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    number             VARCHAR(255) NOT NULL UNIQUE, -- Unique identifier for the case (e.g., id_dosar)
    imposed_name       VARCHAR(255),                 -- Optional human-readable name
    court_name         VARCHAR(255),                 -- Name of the court
    department         VARCHAR(255),
    procedural_stage   VARCHAR(255),
    category           VARCHAR(255),
    subject            VARCHAR(255),
    status             VARCHAR(255),                 -- Current status of the case
    last_updated       TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    monitoring_enabled BOOLEAN   DEFAULT TRUE,       -- Whether the case is being monitored
    INDEX idx_case_id (number)
);

-- User-Case Relationship Table
CREATE TABLE user_case
(
    user_id               BIGINT NOT NULL,
    case_id               BIGINT NOT NULL,
    custom_title          VARCHAR(255),
    notes                 TEXT,
    monitoring_started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, case_id),
    FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE,
    FOREIGN KEY (case_id) REFERENCES court_case (id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_case_id (case_id)
);

-- Hearing Table
CREATE TABLE hearing
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    case_id            BIGINT    NOT NULL,
    judicial_panel     VARCHAR(255),
    solution           VARCHAR(255),
    description        TEXT,
    hearing_date       TIMESTAMP NOT NULL,
    pronouncement_date TIMESTAMP NOT NULL,
    FOREIGN KEY (case_id) REFERENCES court_case (id) ON DELETE CASCADE,
    INDEX idx_case_id (case_id)
);

CREATE TABLE party
(
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    name    VARCHAR(255) NOT NULL,
    role    VARCHAR(100) NOT NULL,
    case_id BIGINT       NOT NULL,
    CONSTRAINT fk_party_case FOREIGN KEY (case_id) REFERENCES court_case (id)
);

-- Notification Table
CREATE TABLE notification
(
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    case_id BIGINT NOT NULL,
    message TEXT   NOT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE,
    FOREIGN KEY (case_id) REFERENCES court_case (id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_case_id (case_id)
);
