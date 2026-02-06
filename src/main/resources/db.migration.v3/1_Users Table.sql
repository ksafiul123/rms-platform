-- Drop existing if recreating
DROP TABLE IF EXISTS users CASCADE;

CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       email VARCHAR(100) NOT NULL,
                       phone VARCHAR(20),
                       password_hash VARCHAR(255) NOT NULL,
                       full_name VARCHAR(200) NOT NULL,
                       profile_image_url VARCHAR(500),

    -- Multi-tenant isolation
                       restaurant_id BIGINT,

    -- Status fields
                       is_active BOOLEAN NOT NULL DEFAULT true,
                       is_email_verified BOOLEAN NOT NULL DEFAULT false,
                       is_phone_verified BOOLEAN NOT NULL DEFAULT false,
                       email_verified_at TIMESTAMP,
                       phone_verified_at TIMESTAMP,

    -- Security
                       last_login_at TIMESTAMP,
                       last_login_ip VARCHAR(45),
                       failed_login_attempts INTEGER NOT NULL DEFAULT 0,
                       account_locked_until TIMESTAMP,
                       password_changed_at TIMESTAMP,

    -- Audit fields
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       created_by BIGINT,
                       updated_by BIGINT,

    -- Soft delete
                       deleted_at TIMESTAMP,

    -- Constraints
                       CONSTRAINT users_email_unique UNIQUE (email),
                       CONSTRAINT users_phone_unique UNIQUE (phone),
                       CONSTRAINT users_restaurant_fk FOREIGN KEY (restaurant_id)
                           REFERENCES restaurants(id) ON DELETE SET NULL,
                       CONSTRAINT users_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
                       CONSTRAINT users_phone_format CHECK (phone ~ '^\+?[0-9]{10,15}$')
);

-- Performance Indexes
CREATE INDEX CONCURRENTLY idx_users_email_active
    ON users(email) WHERE is_active = true AND deleted_at IS NULL;

CREATE INDEX CONCURRENTLY idx_users_phone_active
    ON users(phone) WHERE is_active = true AND deleted_at IS NULL;

CREATE INDEX CONCURRENTLY idx_users_restaurant
    ON users(restaurant_id) WHERE deleted_at IS NULL;

CREATE INDEX CONCURRENTLY idx_users_created_at
    ON users(created_at DESC);

CREATE INDEX CONCURRENTLY idx_users_last_login
    ON users(last_login_at DESC) WHERE is_active = true;

-- Partial index for locked accounts
CREATE INDEX CONCURRENTLY idx_users_locked
    ON users(account_locked_until)
    WHERE account_locked_until IS NOT NULL AND account_locked_until > CURRENT_TIMESTAMP;

-- Composite index for authentication queries
CREATE INDEX CONCURRENTLY idx_users_auth
    ON users(email, is_active, deleted_at);

-- GIN index for full-text search (if needed)
CREATE INDEX CONCURRENTLY idx_users_fulltext
    ON users USING gin(to_tsvector('english', full_name || ' ' || email));

-- Table statistics
ALTER TABLE users SET (
    autovacuum_vacuum_scale_factor = 0.1,
    autovacuum_analyze_scale_factor = 0.05
    );

COMMENT ON TABLE users IS 'Core user accounts for customers, staff, and admins';
COMMENT ON COLUMN users.restaurant_id IS 'NULL for customers, SET for restaurant staff';