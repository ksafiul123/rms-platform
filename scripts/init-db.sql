-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Create roles table
CREATE TABLE IF NOT EXISTS roles (
                                     id BIGSERIAL PRIMARY KEY,
                                     name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- Insert default roles
INSERT INTO roles (name, description) VALUES
                                          ('ROLE_SUPER_ADMIN', 'Full system access'),
                                          ('ROLE_DEVELOPER', 'Ticket and system maintenance'),
                                          ('ROLE_SALESMAN', 'Restaurant onboarding'),
                                          ('ROLE_RESTAURANT_ADMIN', 'Restaurant owner/admin'),
                                          ('ROLE_MANAGER', 'Restaurant manager'),
                                          ('ROLE_CHEF', 'Kitchen staff'),
                                          ('ROLE_DELIVERY_MAN', 'Delivery personnel'),
                                          ('ROLE_CUSTOMER', 'End customer')
    ON CONFLICT (name) DO NOTHING;

-- Log initialization
DO $$
BEGIN
    RAISE NOTICE 'Database initialized successfully at %', NOW();
END $$;