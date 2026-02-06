-- =====================================================
-- RBAC MODULE - OPTIMIZED SCHEMA
-- =====================================================

-- ============== PERMISSIONS TABLE ==============
ALTER TABLE permissions
    ADD CONSTRAINT chk_permissions_name_format
        CHECK (name ~* '^[a-z_]+:[a-z_]+$');

CREATE UNIQUE INDEX IF NOT EXISTS idx_permissions_name
    ON permissions (name);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_permissions_category
    ON permissions (category);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_permissions_active
    ON permissions (is_active) WHERE is_active = true;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_permissions_system
    ON permissions (is_system) WHERE is_system = true;

-- Full-text search on permissions
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_permissions_search
    ON permissions USING gin (
                              to_tsvector('english', name || ' ' || display_name || ' ' || COALESCE(description, ''))
        );

-- ============== ROLE_PERMISSIONS TABLE ==============
ALTER TABLE role_permissions
    ADD CONSTRAINT fk_role_permissions_role
        FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_role_permissions_permission
        FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE;

CREATE UNIQUE INDEX IF NOT EXISTS idx_role_permissions_unique
    ON role_permissions (role_id, permission_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_role_permissions_permission
    ON role_permissions (permission_id);

-- Covering index for permission checks
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_role_permissions_covering
    ON role_permissions (role_id, permission_id)
    INCLUDE (created_at);

-- ============== CUSTOM_ROLES TABLE ==============
ALTER TABLE custom_roles
    ADD CONSTRAINT fk_custom_roles_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_custom_roles_based_on
        FOREIGN KEY (based_on_role_id) REFERENCES roles(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_custom_roles_created_by
        FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL;

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_custom_roles_restaurant
    ON custom_roles (restaurant_id, is_active);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_custom_roles_name
    ON custom_roles (restaurant_id, LOWER(name));
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_custom_roles_active
    ON custom_roles (is_active) WHERE is_active = true;

-- ============== CUSTOM_ROLE_PERMISSIONS TABLE ==============
ALTER TABLE custom_role_permissions
    ADD CONSTRAINT fk_custom_role_permissions_role
        FOREIGN KEY (custom_role_id) REFERENCES custom_roles(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_custom_role_permissions_permission
        FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE;

CREATE UNIQUE INDEX IF NOT EXISTS idx_custom_role_permissions_unique
    ON custom_role_permissions (custom_role_id, permission_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_custom_role_permissions_permission
    ON custom_role_permissions (permission_id);

-- ============== USER_CUSTOM_ROLES TABLE ==============
ALTER TABLE user_custom_roles
    ADD CONSTRAINT fk_user_custom_roles_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_user_custom_roles_role
        FOREIGN KEY (custom_role_id) REFERENCES custom_roles(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_user_custom_roles_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE;

CREATE UNIQUE INDEX IF NOT EXISTS idx_user_custom_roles_unique
    ON user_custom_roles (user_id, custom_role_id, restaurant_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_user_custom_roles_user
    ON user_custom_roles (user_id, restaurant_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_user_custom_roles_role
    ON user_custom_roles (custom_role_id);

-- ============== PERMISSION_OVERRIDES TABLE ==============
ALTER TABLE permission_overrides
    ADD CONSTRAINT fk_permission_overrides_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_permission_overrides_permission
        FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_permission_overrides_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_permission_overrides_granted_by
        FOREIGN KEY (granted_by) REFERENCES users(id) ON DELETE SET NULL,
    ADD CONSTRAINT chk_permission_overrides_expiry
        CHECK (expires_at IS NULL OR expires_at > granted_at);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_permission_overrides_user
    ON permission_overrides (user_id, restaurant_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_permission_overrides_active
    ON permission_overrides (user_id, permission_id)
    WHERE is_active = true AND (expires_at IS NULL OR expires_at > NOW());
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_permission_overrides_expiry
    ON permission_overrides (expires_at)
    WHERE expires_at IS NOT NULL AND expires_at > NOW();

-- ============== PERMISSION_AUDIT_LOG TABLE ==============
ALTER TABLE permission_audit_logs
    ADD CONSTRAINT fk_permission_audit_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_permission_audit_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE SET NULL;

-- Partition audit logs by month for better performance
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_permission_audit_user_time
    ON permission_audit_logs (user_id, timestamp DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_permission_audit_permission
    ON permission_audit_logs (permission_name, timestamp DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_permission_audit_denied
    ON permission_audit_logs (timestamp DESC)
    WHERE access_granted = false;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_permission_audit_restaurant_time
    ON permission_audit_logs (restaurant_id, timestamp DESC)
    WHERE restaurant_id IS NOT NULL;

-- Partitioning for audit logs (implement for high-volume systems)
-- CREATE TABLE permission_audit_logs_partitioned (
--     LIKE permission_audit_logs INCLUDING ALL
-- ) PARTITION BY RANGE (timestamp);

-- ============== PERFORMANCE VIEWS ==============

-- User effective permissions (cached)
CREATE MATERIALIZED VIEW IF NOT EXISTS mv_user_effective_permissions AS
WITH user_role_permissions AS (
    SELECT DISTINCT
        u.id as user_id,
        u.restaurant_id,
        p.id as permission_id,
        p.name as permission_name,
        'ROLE' as source
    FROM users u
             JOIN user_roles ur ON u.id = ur.user_id
             JOIN role_permissions rp ON ur.role_id = rp.role_id
             JOIN permissions p ON rp.permission_id = p.id
    WHERE p.is_active = true
),
     custom_role_permissions AS (
         SELECT DISTINCT
             ucr.user_id,
             ucr.restaurant_id,
             p.id as permission_id,
             p.name as permission_name,
             'CUSTOM_ROLE' as source
         FROM user_custom_roles ucr
                  JOIN custom_role_permissions crp ON ucr.custom_role_id = crp.custom_role_id
                  JOIN permissions p ON crp.permission_id = p.id
                  JOIN custom_roles cr ON ucr.custom_role_id = cr.id
         WHERE p.is_active = true AND cr.is_active = true
     ),
     override_permissions AS (
         SELECT DISTINCT
             po.user_id,
             po.restaurant_id,
             p.id as permission_id,
             p.name as permission_name,
             CASE
                 WHEN po.override_type = 'GRANT' THEN 'OVERRIDE_GRANT'
                 ELSE 'OVERRIDE_REVOKE'
                 END as source
         FROM permission_overrides po
                  JOIN permissions p ON po.permission_id = p.id
         WHERE po.is_active = true
           AND (po.expires_at IS NULL OR po.expires_at > NOW())
     )
SELECT
    user_id,
    restaurant_id,
    permission_id,
    permission_name,
    ARRAY_AGG(DISTINCT source) as sources
FROM (
         SELECT * FROM user_role_permissions
         UNION ALL
         SELECT * FROM custom_role_permissions
         UNION ALL
         SELECT * FROM override_permissions
     ) all_permissions
GROUP BY user_id, restaurant_id, permission_id, permission_name;

CREATE UNIQUE INDEX idx_mv_user_permissions_user_permission
    ON mv_user_effective_permissions (user_id, permission_id, restaurant_id);
CREATE INDEX idx_mv_user_permissions_user
    ON mv_user_effective_permissions (user_id);
CREATE INDEX idx_mv_user_permissions_permission
    ON mv_user_effective_permissions (permission_name);

-- Function to check permission (optimized)
CREATE OR REPLACE FUNCTION has_permission(
    p_user_id BIGINT,
    p_permission_name VARCHAR,
    p_restaurant_id BIGINT DEFAULT NULL
)
    RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1
        FROM mv_user_effective_permissions
        WHERE user_id = p_user_id
          AND permission_name = p_permission_name
          AND (restaurant_id = p_restaurant_id OR restaurant_id IS NULL)
    );
END;
$$ LANGUAGE plpgsql STABLE;

-- Index for function
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_mv_user_permissions_check
    ON mv_user_effective_permissions (user_id, permission_name, restaurant_id);