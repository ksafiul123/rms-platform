-- =====================================================
-- AUTHENTICATION MODULE - OPTIMIZED SCHEMA
-- =====================================================

-- ============== USERS TABLE ==============
ALTER TABLE users
    ADD CONSTRAINT chk_users_email_format
        CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}$'),
    ADD CONSTRAINT chk_users_phone_format
        CHECK (phone ~* '^\+?[1-9]\d{1,14}$'),
    ADD CONSTRAINT chk_users_phone_verified
        CHECK (phone_verified IN (true, false));

-- Performance indexes
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_email_lower
    ON users (LOWER(email));
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_phone
    ON users (phone) WHERE phone IS NOT NULL;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_restaurant_role
    ON users (restaurant_id, created_at DESC)
    WHERE restaurant_id IS NOT NULL;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_active
    ON users (is_active, created_at DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_email_verified
    ON users (email_verified) WHERE email_verified = false;

-- Partial indexes for performance
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_active_customers
    ON users (id, created_at DESC)
    WHERE is_active = true;

-- ============== ROLES TABLE ==============
ALTER TABLE roles
    ADD CONSTRAINT chk_roles_level_range
        CHECK (level >= 1 AND level <= 10);

CREATE UNIQUE INDEX IF NOT EXISTS idx_roles_name_unique
    ON roles (name);
CREATE INDEX IF NOT EXISTS idx_roles_level
    ON roles (level ASC);

-- ============== USER_ROLES TABLE ==============
ALTER TABLE user_roles
    ADD CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT;

CREATE UNIQUE INDEX IF NOT EXISTS idx_user_roles_unique
    ON user_roles (user_id, role_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role
    ON user_roles (role_id);

-- ============== REFRESH_TOKENS TABLE ==============
ALTER TABLE refresh_tokens
    ADD CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    ADD CONSTRAINT chk_refresh_tokens_expiry
        CHECK (expiry_date > created_at);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_refresh_tokens_user_active
    ON refresh_tokens (user_id, expiry_date DESC)
    WHERE revoked = false;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_refresh_tokens_token_hash
    ON refresh_tokens USING hash (token);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_refresh_tokens_expiry
    ON refresh_tokens (expiry_date)
    WHERE revoked = false AND expiry_date > NOW();

-- Cleanup expired tokens (scheduled job)
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_cleanup
    ON refresh_tokens (expiry_date)
    WHERE revoked = false AND expiry_date < NOW();

-- ============== PERFORMANCE VIEWS ==============

-- Active users with roles
CREATE OR REPLACE VIEW v_active_users_with_roles AS
SELECT
    u.id,
    u.full_name,
    u.email,
    u.phone,
    u.restaurant_id,
    u.is_active,
    ARRAY_AGG(r.name ORDER BY r.level) as roles,
    MIN(r.level) as highest_role_level,
    u.created_at,
    u.last_login_at
FROM users u
         LEFT JOIN user_roles ur ON u.id = ur.user_id
         LEFT JOIN roles r ON ur.role_id = r.id
WHERE u.is_active = true
GROUP BY u.id;

-- Create materialized view for frequently accessed data
CREATE MATERIALIZED VIEW IF NOT EXISTS mv_user_role_summary AS
SELECT
    u.id as user_id,
    u.restaurant_id,
    u.full_name,
    u.email,
    ARRAY_AGG(DISTINCT r.name) as role_names,
    ARRAY_AGG(DISTINCT r.id) as role_ids,
    MIN(r.level) as highest_level,
    u.is_active,
    u.created_at
FROM users u
         LEFT JOIN user_roles ur ON u.id = ur.user_id
         LEFT JOIN roles r ON ur.role_id = r.id
GROUP BY u.id, u.restaurant_id, u.full_name, u.email, u.is_active, u.created_at;

CREATE UNIQUE INDEX idx_mv_user_role_summary_user
    ON mv_user_role_summary (user_id);
CREATE INDEX idx_mv_user_role_summary_restaurant
    ON mv_user_role_summary (restaurant_id)
    WHERE restaurant_id IS NOT NULL;

-- Refresh materialized view (call from scheduled job)
-- REFRESH MATERIALIZED VIEW CONCURRENTLY mv_user_role_summary;

-- ============== PARTITIONING FOR SCALE ==============

-- Partition refresh_tokens by month for better performance
-- (Implement if table grows beyond 1M rows)

-- CREATE TABLE refresh_tokens_partitioned (
--     LIKE refresh_tokens INCLUDING ALL
-- ) PARTITION BY RANGE (created_at);
--
-- CREATE TABLE refresh_tokens_2024_01 PARTITION OF refresh_tokens_partitioned
--     FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');
