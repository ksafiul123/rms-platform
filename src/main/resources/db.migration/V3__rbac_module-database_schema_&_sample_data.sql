-- ============================================
-- RBAC MODULE - DATABASE SCHEMA
-- ============================================

-- Permissions Table
CREATE TABLE permissions (
                             id BIGSERIAL PRIMARY KEY,
                             name VARCHAR(100) UNIQUE NOT NULL,
                             display_name VARCHAR(100),
                             description VARCHAR(500),
                             resource VARCHAR(50) NOT NULL,
                             action VARCHAR(20) NOT NULL,
                             category VARCHAR(50),
                             is_system BOOLEAN DEFAULT FALSE,
                             is_active BOOLEAN DEFAULT TRUE,
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_permission_name ON permissions(name);
CREATE INDEX idx_permission_resource ON permissions(resource);
CREATE INDEX idx_permission_category ON permissions(category);

-- Role-Permission Junction Table
CREATE TABLE role_permissions (
                                  role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
                                  permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
                                  PRIMARY KEY (role_id, permission_id)
);

-- Update Roles Table (add new columns)
ALTER TABLE roles ADD COLUMN IF NOT EXISTS display_name VARCHAR(100);
ALTER TABLE roles ADD COLUMN IF NOT EXISTS role_level INTEGER;
ALTER TABLE roles ADD COLUMN IF NOT EXISTS is_system BOOLEAN DEFAULT TRUE;

-- Custom Roles Table
CREATE TABLE custom_roles (
                              id BIGSERIAL PRIMARY KEY,
                              restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
                              name VARCHAR(100) NOT NULL,
                              display_name VARCHAR(100),
                              description VARCHAR(500),
                              based_on_role_id BIGINT REFERENCES roles(id),
                              is_active BOOLEAN DEFAULT TRUE,
                              created_by BIGINT REFERENCES users(id),
                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              UNIQUE(restaurant_id, name)
);

CREATE INDEX idx_custom_role_restaurant ON custom_roles(restaurant_id);
CREATE INDEX idx_custom_role_name ON custom_roles(name, restaurant_id);

-- Custom Role-Permission Junction Table
CREATE TABLE custom_role_permissions (
                                         custom_role_id BIGINT NOT NULL REFERENCES custom_roles(id) ON DELETE CASCADE,
                                         permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
                                         PRIMARY KEY (custom_role_id, permission_id)
);

-- User Custom Role Assignment
CREATE TABLE user_custom_roles (
                                   id BIGSERIAL PRIMARY KEY,
                                   user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                   restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
                                   custom_role_id BIGINT NOT NULL REFERENCES custom_roles(id) ON DELETE CASCADE,
                                   assigned_by BIGINT REFERENCES users(id),
                                   assigned_at TIMESTAMP,
                                   is_active BOOLEAN DEFAULT TRUE,
                                   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_custom_role_user ON user_custom_roles(user_id);
CREATE INDEX idx_user_custom_role_restaurant ON user_custom_roles(restaurant_id);

-- Permission Overrides Table
CREATE TABLE permission_overrides (
                                      id BIGSERIAL PRIMARY KEY,
                                      user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                      restaurant_id BIGINT REFERENCES restaurants(id) ON DELETE CASCADE,
                                      permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
                                      override_type VARCHAR(20) NOT NULL,
                                      granted_by BIGINT REFERENCES users(id),
                                      expires_at TIMESTAMP,
                                      reason VARCHAR(500),
                                      is_active BOOLEAN DEFAULT TRUE,
                                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_permission_override_user ON permission_overrides(user_id);
CREATE INDEX idx_permission_override_restaurant ON permission_overrides(restaurant_id);
CREATE INDEX idx_permission_override_expiry ON permission_overrides(expires_at);

-- Permission Audit Logs Table
CREATE TABLE permission_audit_logs (
                                       id BIGSERIAL PRIMARY KEY,
                                       user_id BIGINT NOT NULL,
                                       restaurant_id BIGINT,
                                       action VARCHAR(50) NOT NULL,
                                       resource VARCHAR(100) NOT NULL,
                                       endpoint VARCHAR(100),
                                       permission_name VARCHAR(100),
                                       access_granted BOOLEAN,
                                       ip_address VARCHAR(50),
                                       user_agent VARCHAR(500),
                                       details TEXT,
                                       timestamp TIMESTAMP NOT NULL
);

CREATE INDEX idx_audit_user ON permission_audit_logs(user_id);
CREATE INDEX idx_audit_timestamp ON permission_audit_logs(timestamp);
CREATE INDEX idx_audit_action ON permission_audit_logs(action);
CREATE INDEX idx_audit_restaurant ON permission_audit_logs(restaurant_id);

-- ============================================
-- DEFAULT PERMISSIONS SEEDING
-- ============================================

-- MENU MANAGEMENT PERMISSIONS
INSERT INTO permissions (name, display_name, description, resource, action, category, is_system) VALUES
                                                                                                     ('menu:create', 'Create Menu Item', 'Create new menu items', 'menu', 'CREATE', 'MENU_MANAGEMENT', TRUE),
                                                                                                     ('menu:read', 'View Menu Items', 'View menu items', 'menu', 'READ', 'MENU_MANAGEMENT', TRUE),
                                                                                                     ('menu:update', 'Update Menu Item', 'Modify menu items', 'menu', 'UPDATE', 'MENU_MANAGEMENT', TRUE),
                                                                                                     ('menu:delete', 'Delete Menu Item', 'Remove menu items', 'menu', 'DELETE', 'MENU_MANAGEMENT', TRUE),
                                                                                                     ('menu:approve', 'Approve Menu Item', 'Approve menu items for publishing', 'menu', 'APPROVE', 'MENU_MANAGEMENT', TRUE),
                                                                                                     ('menu:manage', 'Manage Menu', 'Full menu management access', 'menu', 'MANAGE', 'MENU_MANAGEMENT', TRUE),

-- ORDER MANAGEMENT PERMISSIONS
                                                                                                     ('order:create', 'Create Order', 'Place new orders', 'order', 'CREATE', 'ORDER_MANAGEMENT', TRUE),
                                                                                                     ('order:read', 'View Orders', 'View order details', 'order', 'READ', 'ORDER_MANAGEMENT', TRUE),
                                                                                                     ('order:update', 'Update Order', 'Modify order details', 'order', 'UPDATE', 'ORDER_MANAGEMENT', TRUE),
                                                                                                     ('order:cancel', 'Cancel Order', 'Cancel orders', 'order', 'DELETE', 'ORDER_MANAGEMENT', TRUE),
                                                                                                     ('order:approve', 'Approve Order', 'Approve pending orders', 'order', 'APPROVE', 'ORDER_MANAGEMENT', TRUE),
                                                                                                     ('order:assign', 'Assign Order', 'Assign orders to delivery staff', 'order', 'ASSIGN', 'ORDER_MANAGEMENT', TRUE),
                                                                                                     ('order:manage', 'Manage Orders', 'Full order management access', 'order', 'MANAGE', 'ORDER_MANAGEMENT', TRUE),

-- INVENTORY MANAGEMENT PERMISSIONS
                                                                                                     ('inventory:create', 'Add Inventory', 'Add inventory items', 'inventory', 'CREATE', 'INVENTORY_MANAGEMENT', TRUE),
                                                                                                     ('inventory:read', 'View Inventory', 'View inventory levels', 'inventory', 'READ', 'INVENTORY_MANAGEMENT', TRUE),
                                                                                                     ('inventory:update', 'Update Inventory', 'Modify inventory items', 'inventory', 'UPDATE', 'INVENTORY_MANAGEMENT', TRUE),
                                                                                                     ('inventory:delete', 'Delete Inventory', 'Remove inventory items', 'inventory', 'DELETE', 'INVENTORY_MANAGEMENT', TRUE),
                                                                                                     ('inventory:manage', 'Manage Inventory', 'Full inventory management', 'inventory', 'MANAGE', 'INVENTORY_MANAGEMENT', TRUE),

-- USER MANAGEMENT PERMISSIONS
                                                                                                     ('user:create', 'Create User', 'Add new users', 'user', 'CREATE', 'USER_MANAGEMENT', TRUE),
                                                                                                     ('user:read', 'View Users', 'View user details', 'user', 'READ', 'USER_MANAGEMENT', TRUE),
                                                                                                     ('user:update', 'Update User', 'Modify user details', 'user', 'UPDATE', 'USER_MANAGEMENT', TRUE),
                                                                                                     ('user:delete', 'Delete User', 'Remove users', 'user', 'DELETE', 'USER_MANAGEMENT', TRUE),
                                                                                                     ('user:manage', 'Manage Users', 'Full user management', 'user', 'MANAGE', 'USER_MANAGEMENT', TRUE),

-- ROLE & PERMISSION MANAGEMENT
                                                                                                     ('permission:view', 'View Permissions', 'View all permissions', 'permission', 'READ', 'PERMISSION_MANAGEMENT', TRUE),
                                                                                                     ('permission:override', 'Override Permission', 'Grant/revoke permission overrides', 'permission', 'MANAGE', 'PERMISSION_MANAGEMENT', TRUE),
                                                                                                     ('role:create', 'Create Custom Role', 'Create custom roles', 'role', 'CREATE', 'PERMISSION_MANAGEMENT', TRUE),
                                                                                                     ('role:assign', 'Assign Role', 'Assign roles to users', 'role', 'ASSIGN', 'PERMISSION_MANAGEMENT', TRUE),
                                                                                                     ('role:manage', 'Manage Roles', 'Full role management', 'role', 'MANAGE', 'PERMISSION_MANAGEMENT', TRUE),

-- RESTAURANT SETTINGS PERMISSIONS
                                                                                                     ('restaurant:update', 'Update Settings', 'Modify restaurant settings', 'restaurant', 'UPDATE', 'RESTAURANT_MANAGEMENT', TRUE),
                                                                                                     ('restaurant:manage', 'Manage Restaurant', 'Full restaurant management', 'restaurant', 'MANAGE', 'RESTAURANT_MANAGEMENT', TRUE),
                                                                                                     ('feature:toggle', 'Toggle Features', 'Enable/disable restaurant features', 'feature', 'MANAGE', 'RESTAURANT_MANAGEMENT', TRUE),
                                                                                                     ('branch:create', 'Create Branch', 'Add new branches', 'branch', 'CREATE', 'RESTAURANT_MANAGEMENT', TRUE),
                                                                                                     ('branch:manage', 'Manage Branches', 'Full branch management', 'branch', 'MANAGE', 'RESTAURANT_MANAGEMENT', TRUE),

-- PAYMENT & FINANCIAL PERMISSIONS
                                                                                                     ('payment:process', 'Process Payment', 'Process customer payments', 'payment', 'CREATE', 'FINANCIAL_MANAGEMENT', TRUE),
                                                                                                     ('payment:refund', 'Refund Payment', 'Issue refunds', 'payment', 'UPDATE', 'FINANCIAL_MANAGEMENT', TRUE),
                                                                                                     ('payment:view', 'View Payments', 'View payment history', 'payment', 'READ', 'FINANCIAL_MANAGEMENT', TRUE),
                                                                                                     ('finance:report', 'View Financial Reports', 'Access financial reports', 'finance', 'READ', 'FINANCIAL_MANAGEMENT', TRUE),
                                                                                                     ('finance:manage', 'Manage Finances', 'Full financial management', 'finance', 'MANAGE', 'FINANCIAL_MANAGEMENT', TRUE),

-- ANALYTICS & REPORTING PERMISSIONS
                                                                                                     ('analytics:view', 'View Analytics', 'Access analytics dashboards', 'analytics', 'READ', 'ANALYTICS', TRUE),
                                                                                                     ('analytics:export', 'Export Reports', 'Export analytical reports', 'analytics', 'EXPORT', 'ANALYTICS', TRUE),

-- CUSTOMER MANAGEMENT PERMISSIONS
                                                                                                     ('customer:view', 'View Customers', 'View customer details', 'customer', 'READ', 'CUSTOMER_MANAGEMENT', TRUE),
                                                                                                     ('customer:manage', 'Manage Customers', 'Full customer management', 'customer', 'MANAGE', 'CUSTOMER_MANAGEMENT', TRUE),

-- TABLE MANAGEMENT PERMISSIONS
                                                                                                     ('table:view', 'View Tables', 'View table layout and status', 'table', 'READ', 'TABLE_MANAGEMENT', TRUE),
                                                                                                     ('table:manage', 'Manage Tables', 'Manage table configurations', 'table', 'MANAGE', 'TABLE_MANAGEMENT', TRUE);

-- ============================================
-- UPDATE ROLE DEFINITIONS
-- ============================================

-- Update existing roles with display names and levels
UPDATE roles SET
                 display_name = 'Super Administrator',
                 role_level = 1,
                 description = 'Full system access with all permissions'
WHERE name = 'ROLE_SUPER_ADMIN';

UPDATE roles SET
                 display_name = 'Developer',
                 role_level = 2,
                 description = 'System maintenance and debugging access'
WHERE name = 'ROLE_DEVELOPER';

UPDATE roles SET
                 display_name = 'Salesman',
                 role_level = 3,
                 description = 'Restaurant onboarding and sales management'
WHERE name = 'ROLE_SALESMAN';

UPDATE roles SET
                 display_name = 'Restaurant Administrator',
                 role_level = 4,
                 description = 'Full restaurant management access'
WHERE name = 'ROLE_RESTAURANT_ADMIN';

UPDATE roles SET
                 display_name = 'Manager',
                 role_level = 5,
                 description = 'Restaurant operations management'
WHERE name = 'ROLE_MANAGER';

UPDATE roles SET
                 display_name = 'Chef',
                 role_level = 6,
                 description = 'Kitchen operations and order preparation'
WHERE name = 'ROLE_CHEF';

UPDATE roles SET
                 display_name = 'Delivery Man',
                 role_level = 7,
                 description = 'Delivery operations'
WHERE name = 'ROLE_DELIVERY_MAN';

UPDATE roles SET
                 display_name = 'Customer',
                 role_level = 8,
                 description = 'Customer ordering and profile management'
WHERE name = 'ROLE_CUSTOMER';

-- ============================================
-- ASSIGN PERMISSIONS TO ROLES
-- ============================================

-- SUPER ADMIN - All permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         CROSS JOIN permissions p
WHERE r.name = 'ROLE_SUPER_ADMIN';

-- RESTAURANT ADMIN - Most permissions except system-level
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         CROSS JOIN permissions p
WHERE r.name = 'ROLE_RESTAURANT_ADMIN'
  AND p.category IN (
                     'MENU_MANAGEMENT',
                     'ORDER_MANAGEMENT',
                     'INVENTORY_MANAGEMENT',
                     'USER_MANAGEMENT',
                     'RESTAURANT_MANAGEMENT',
                     'FINANCIAL_MANAGEMENT',
                     'ANALYTICS',
                     'CUSTOMER_MANAGEMENT',
                     'TABLE_MANAGEMENT'
    );

-- MANAGER - Operations and monitoring
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         CROSS JOIN permissions p
WHERE r.name = 'ROLE_MANAGER'
  AND p.name IN (
                 'menu:read', 'menu:update',
                 'order:read', 'order:update', 'order:approve', 'order:assign', 'order:manage',
                 'inventory:read', 'inventory:update',
                 'user:read',
                 'payment:view', 'payment:process',
                 'analytics:view',
                 'customer:view',
                 'table:view', 'table:manage'
    );

-- CHEF - Kitchen operations
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         CROSS JOIN permissions p
WHERE r.name = 'ROLE_CHEF'
  AND p.name IN (
                 'menu:read',
                 'order:read', 'order:update',
                 'inventory:read'
    );

-- DELIVERY MAN - Delivery operations
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         CROSS JOIN permissions p
WHERE r.name = 'ROLE_DELIVERY_MAN'
  AND p.name IN (
                 'order:read', 'order:update'
    );

-- CUSTOMER - Self-service
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         CROSS JOIN permissions p
WHERE r.name = 'ROLE_CUSTOMER'
  AND p.name IN (
                 'menu:read',
                 'order:create', 'order:read',
                 'payment:view'
    );

-- DEVELOPER - System access
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         CROSS JOIN permissions p
WHERE r.name = 'ROLE_DEVELOPER'
  AND p.category IN ('PERMISSION_MANAGEMENT', 'ANALYTICS');

-- SALESMAN - Onboarding and configuration
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         CROSS JOIN permissions p
WHERE r.name = 'ROLE_SALESMAN'
  AND p.name IN (
                 'restaurant:update', 'restaurant:manage',
                 'feature:toggle',
                 'user:create',
                 'analytics:view'
    );

-- ============================================
-- USEFUL QUERIES
-- ============================================

-- Get all permissions for a role
SELECT p.*
FROM permissions p
         JOIN role_permissions rp ON p.id = rp.permission_id
         JOIN roles r ON r.id = rp.role_id
WHERE r.name = 'ROLE_RESTAURANT_ADMIN'
ORDER BY p.category, p.name;

-- Get all permissions for a user (including custom roles)
SELECT DISTINCT p.*
FROM permissions p
         LEFT JOIN role_permissions rp ON p.id = rp.permission_id
         LEFT JOIN user_roles ur ON ur.role_id = rp.role_id
         LEFT JOIN custom_role_permissions crp ON p.id = crp.permission_id
         LEFT JOIN user_custom_roles ucr ON ucr.custom_role_id = crp.custom_role_id
WHERE (ur.user_id = 5 OR ucr.user_id = 5)
  AND p.is_active = TRUE;

-- Check if user has specific permission
SELECT EXISTS(
    SELECT 1
    FROM permissions p
             LEFT JOIN role_permissions rp ON p.id = rp.permission_id
             LEFT JOIN user_roles ur ON ur.role_id = rp.role_id
    WHERE ur.user_id = 5
      AND p.name = 'menu:create'
      AND p.is_active = TRUE
);

-- Get recent access denials
SELECT pal.*, u.full_name
FROM permission_audit_logs pal
         JOIN users u ON pal.user_id = u.id
WHERE pal.access_granted = FALSE
  AND pal.timestamp > NOW() - INTERVAL '24 hours'
ORDER BY pal.timestamp DESC
LIMIT 50;