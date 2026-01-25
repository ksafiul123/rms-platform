-- ============================================
-- DATABASE SCHEMA FOR AUTHENTICATION MODULE
-- ============================================

-- Restaurants table (Tenant Master)
CREATE TABLE restaurants (
                             id BIGSERIAL PRIMARY KEY,
                             restaurant_code VARCHAR(20) UNIQUE NOT NULL,
                             name VARCHAR(100) NOT NULL,
                             email VARCHAR(100) UNIQUE NOT NULL,
                             phone_number VARCHAR(20),
                             address VARCHAR(500),
                             subscription_status VARCHAR(20) NOT NULL DEFAULT 'TRIAL',
                             subscription_expiry TIMESTAMP,
                             is_active BOOLEAN DEFAULT TRUE,
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_restaurant_code ON restaurants(restaurant_code);
CREATE INDEX idx_restaurant_email ON restaurants(email);

-- Roles table
CREATE TABLE roles (
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
                                          ('ROLE_CUSTOMER', 'End customer');

-- Users table
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       restaurant_id BIGINT REFERENCES restaurants(id) ON DELETE CASCADE,
                       full_name VARCHAR(100) NOT NULL,
                       email VARCHAR(100) UNIQUE NOT NULL,
                       phone_number VARCHAR(20),
                       password VARCHAR(255) NOT NULL,
                       is_email_verified BOOLEAN DEFAULT FALSE,
                       is_phone_verified BOOLEAN DEFAULT FALSE,
                       is_active BOOLEAN DEFAULT TRUE,
                       last_login TIMESTAMP,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_restaurant ON users(restaurant_id);
CREATE INDEX idx_user_phone ON users(phone_number);

-- User-Role junction table (Many-to-Many)
CREATE TABLE user_roles (
                            user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
                            PRIMARY KEY (user_id, role_id)
);

-- Refresh tokens table
CREATE TABLE refresh_tokens (
                                id BIGSERIAL PRIMARY KEY,
                                token VARCHAR(500) UNIQUE NOT NULL,
                                user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                expiry_date TIMESTAMP NOT NULL,
                                is_revoked BOOLEAN DEFAULT FALSE,
                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_refresh_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_user ON refresh_tokens(user_id);

-- ============================================
-- SAMPLE API REQUESTS & RESPONSES
-- ============================================

/*
========================================
1. CUSTOMER REGISTRATION
========================================

POST /api/v1/auth/register
Content-Type: application/json

{
  "fullName": "John Doe",
  "email": "john.doe@gmail.com",
  "phoneNumber": "+8801712345678",
  "password": "MyPass123",
  "restaurantId": null
}

RESPONSE (201 Created):
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwiZW1haWwiOiJqb2huLmRvZUBnbWFpbC5jb20iLCJyZXN0YXVyYW50SWQiOm51bGwsInJvbGVzIjoiUk9MRV9DVVNUT01FUiIsImlhdCI6MTcwNjE4MzQwMCwiZXhwIjoxNzA2MTg3MDAwfQ.abc123...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzA2MTgzNDAwLCJleHAiOjE3MDY3ODgyMDB9.xyz789...",
  "tokenType": "Bearer",
  "user": {
    "id": 1,
    "fullName": "John Doe",
    "email": "john.doe@gmail.com",
    "phoneNumber": "+8801712345678",
    "restaurantId": null,
    "restaurantName": null,
    "roles": ["ROLE_CUSTOMER"],
    "isEmailVerified": false,
    "isPhoneVerified": false,
    "lastLogin": null
  }
}

========================================
2. USER LOGIN
========================================

POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "admin@restaurant.com",
  "password": "Admin@123"
}

RESPONSE (200 OK):
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "user": {
    "id": 5,
    "fullName": "Jane Smith",
    "email": "admin@restaurant.com",
    "phoneNumber": "+8801887654321",
    "restaurantId": 2,
    "restaurantName": "Golden Fork Restaurant",
    "roles": ["ROLE_RESTAURANT_ADMIN"],
    "isEmailVerified": true,
    "isPhoneVerified": true,
    "lastLogin": "2026-01-22T10:30:00"
  }
}

ERROR (401 Unauthorized):
{
  "success": false,
  "message": "Invalid email or password",
  "data": null,
  "timestamp": "2026-01-22T10:35:00"
}

========================================
3. RESTAURANT ONBOARDING (Salesman)
========================================

POST /api/v1/auth/register-restaurant
Authorization: Bearer <salesman_token>
Content-Type: application/json

{
  "restaurantName": "Golden Fork Restaurant",
  "restaurantEmail": "contact@goldenfork.com",
  "restaurantPhone": "+8801712345678",
  "address": "123 Main Street, Gulshan, Dhaka",
  "adminName": "Jane Smith",
  "adminEmail": "jane@goldenfork.com",
  "adminPassword": "Admin@123"
}

RESPONSE (201 Created):
{
  "success": true,
  "message": "Restaurant registered successfully",
  "data": {
    "restaurantId": 2,
    "restaurantCode": "REST8A3F7B9C",
    "restaurantName": "Golden Fork Restaurant",
    "adminEmail": "jane@goldenfork.com",
    "subscriptionStatus": "TRIAL",
    "subscriptionExpiry": "2026-02-21T10:45:00"
  },
  "timestamp": "2026-01-22T10:45:00"
}

========================================
4. REFRESH TOKEN
========================================

POST /api/v1/auth/refresh-token
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzA2MTgzNDAwLCJleHAiOjE3MDY3ODgyMDB9.xyz789..."
}

RESPONSE (200 OK):
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...[new_token]",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...[new_refresh_token]",
  "tokenType": "Bearer"
}

ERROR (403 Forbidden):
{
  "success": false,
  "message": "Refresh token expired. Please login again",
  "data": null,
  "timestamp": "2026-01-22T10:50:00"
}

========================================
5. LOGOUT
========================================

POST /api/v1/auth/logout
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
}

RESPONSE (200 OK):
{
  "success": true,
  "message": "Logged out successfully",
  "data": null,
  "timestamp": "2026-01-22T11:00:00"
}

========================================
6. GET CURRENT USER
========================================

GET /api/v1/auth/me
Authorization: Bearer <access_token>

RESPONSE (200 OK):
{
  "success": true,
  "message": "User fetched successfully",
  "data": {
    "id": 1,
    "fullName": "John Doe",
    "email": "john.doe@gmail.com",
    "phoneNumber": "+8801712345678",
    "restaurantId": null,
    "restaurantName": null,
    "roles": ["ROLE_CUSTOMER"],
    "isEmailVerified": false,
    "isPhoneVerified": false,
    "lastLogin": "2026-01-22T11:05:00"
  },
  "timestamp": "2026-01-22T11:05:00"
}

========================================
VALIDATION ERRORS
========================================

POST /api/v1/auth/register
Content-Type: application/json

{
  "fullName": "J",
  "email": "invalid-email",
  "password": "weak"
}

RESPONSE (400 Bad Request):
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "fullName": "Name must be between 2 and 100 characters",
    "email": "Invalid email format",
    "password": "Password must be between 8 and 100 characters",
    "password": "Password must contain at least one uppercase letter, one lowercase letter, and one digit"
  },
  "timestamp": "2026-01-22T11:10:00"
}
*/

-- ============================================
-- INDEXES FOR PERFORMANCE
-- ============================================

-- Composite indexes for multi-tenant queries
CREATE INDEX idx_users_restaurant_active ON users(restaurant_id, is_active);
CREATE INDEX idx_users_email_active ON users(email, is_active);

-- Index for token cleanup job
CREATE INDEX idx_refresh_tokens_expiry ON refresh_tokens(expiry_date) WHERE is_revoked = false;

-- Full-text search on restaurant names (optional)
CREATE INDEX idx_restaurant_name_trgm ON restaurants USING gin(name gin_trgm_ops);