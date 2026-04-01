CREATE TABLE members (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NULL,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    last_login_at DATETIME NULL,
    deleted_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_members_email (email),
    KEY idx_members_status (status),
    KEY idx_members_deleted_at (deleted_at)
);

CREATE TABLE auth_identities (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    provider VARCHAR(20) NOT NULL,
    provider_user_id VARCHAR(191) NOT NULL,
    provider_email VARCHAR(255) NULL,
    linked_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_auth_identities_provider_user (provider, provider_user_id),
    UNIQUE KEY uk_auth_identities_member_provider (member_id, provider),
    KEY idx_auth_identities_member_id (member_id),
    KEY idx_auth_identities_deleted_at (deleted_at)
);

CREATE TABLE email_verifications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    email VARCHAR(255) NOT NULL,
    purpose VARCHAR(20) NOT NULL,
    verification_code VARCHAR(64) NOT NULL,
    expires_at DATETIME NOT NULL,
    verified_at DATETIME NULL,
    consumed_at DATETIME NULL,
    requested_ip VARCHAR(45) NULL,
    deleted_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_email_verifications_member_id (member_id),
    KEY idx_email_verifications_email_purpose (email, purpose),
    KEY idx_email_verifications_expires_at (expires_at),
    KEY idx_email_verifications_deleted_at (deleted_at)
);

CREATE TABLE refresh_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    issued_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at DATETIME NOT NULL,
    revoked_at DATETIME NULL,
    revoked_reason VARCHAR(100) NULL,
    user_agent VARCHAR(255) NULL,
    ip_address VARCHAR(45) NULL,
    deleted_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_refresh_tokens_token_hash (token_hash),
    KEY idx_refresh_tokens_member_id (member_id),
    KEY idx_refresh_tokens_expires_at (expires_at),
    KEY idx_refresh_tokens_deleted_at (deleted_at)
);

CREATE TABLE venues (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    address VARCHAR(255) NOT NULL,
    timezone VARCHAR(50) NOT NULL DEFAULT 'Asia/Seoul',
    capacity INT NULL,
    deleted_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_venues_name (name),
    KEY idx_venues_deleted_at (deleted_at)
);

CREATE TABLE events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    venue_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NULL,
    category VARCHAR(30) NOT NULL,
    thumbnail_url VARCHAR(500) NULL,
    booking_open_at DATETIME NOT NULL,
    booking_status VARCHAR(20) NOT NULL,
    cancel_deadline_hours INT NOT NULL DEFAULT 24,
    status VARCHAR(20) NOT NULL,
    deleted_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_events_venue_id (venue_id),
    KEY idx_events_category (category),
    KEY idx_events_booking_open_at (booking_open_at),
    KEY idx_events_status (status),
    KEY idx_events_deleted_at (deleted_at)
);

CREATE TABLE event_sessions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_id BIGINT NOT NULL,
    starts_at DATETIME NOT NULL,
    ends_at DATETIME NOT NULL,
    booking_status VARCHAR(20) NOT NULL,
    seat_mode VARCHAR(20) NOT NULL,
    deleted_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_event_sessions_event_starts_at (event_id, starts_at),
    KEY idx_event_sessions_event_id (event_id),
    KEY idx_event_sessions_starts_at (starts_at),
    KEY idx_event_sessions_booking_status (booking_status),
    KEY idx_event_sessions_deleted_at (deleted_at)
);
