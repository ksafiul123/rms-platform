-- ============================================
-- RESTAURANT MANAGEMENT MODULE - DATABASE SCHEMA
-- ============================================

-- Restaurant Settings Table
CREATE TABLE restaurant_settings (
                                     id BIGSERIAL PRIMARY KEY,
                                     restaurant_id BIGINT NOT NULL UNIQUE REFERENCES restaurants(id) ON DELETE CASCADE,
                                     business_name VARCHAR(200),
                                     tax_registration_number VARCHAR(50),
                                     gst_number VARCHAR(50),
                                     business_type VARCHAR(50),
                                     currency VARCHAR(20) DEFAULT 'BDT',
                                     timezone VARCHAR(50) DEFAULT 'Asia/Dhaka',
                                     language VARCHAR(10) DEFAULT 'en',
                                     tax_percentage DECIMAL(5,2) DEFAULT 0.00,
                                     service_charge_percentage DECIMAL(5,2) DEFAULT 0.00,
                                     auto_accept_orders BOOLEAN DEFAULT FALSE,
                                     allow_online_payments BOOLEAN DEFAULT TRUE,
                                     allow_cash_payments BOOLEAN DEFAULT TRUE,
                                     minimum_order_amount DECIMAL(10,2) DEFAULT 0.00,
                                     delivery_radius_km DECIMAL(5,2),
                                     average_preparation_time_minutes INTEGER DEFAULT 30,
                                     logo_url VARCHAR(500),
                                     banner_url VARCHAR(500),
                                     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_restaurant_settings_restaurant ON restaurant_settings(restaurant_id);

-- Restaurant Features Table
CREATE TABLE restaurant_features (
                                     id BIGSERIAL PRIMARY KEY,
                                     restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
                                     feature_name VARCHAR(100) NOT NULL,
                                     is_enabled BOOLEAN NOT NULL DEFAULT FALSE,
                                     enabled_at TIMESTAMP,
                                     enabled_by BIGINT REFERENCES users(id),
                                     notes VARCHAR(500),
                                     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     UNIQUE(restaurant_id, feature_name)
);

CREATE INDEX idx_restaurant_features_restaurant ON restaurant_features(restaurant_id);
CREATE INDEX idx_restaurant_features_enabled ON restaurant_features(restaurant_id, is_enabled);

-- Subscription Plans Table
CREATE TABLE subscription_plans (
                                    id BIGSERIAL PRIMARY KEY,
                                    name VARCHAR(50) UNIQUE NOT NULL,
                                    description VARCHAR(500),
                                    monthly_price DECIMAL(10,2) NOT NULL,
                                    yearly_price DECIMAL(10,2),
                                    trial_days INTEGER DEFAULT 30,
                                    max_orders_per_month INTEGER,
                                    max_menu_items INTEGER,
                                    max_staff_users INTEGER,
                                    commission_percentage DECIMAL(5,2) DEFAULT 0.00,
                                    is_active BOOLEAN DEFAULT TRUE,
                                    display_order INTEGER,
                                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert default subscription plans
INSERT INTO subscription_plans (name, description, monthly_price, yearly_price, trial_days,
                                max_orders_per_month, max_menu_items, max_staff_users, commission_percentage, display_order) VALUES
                                                                                                                                 ('BASIC', 'Starter plan for small restaurants', 1500.00, 15000.00, 30, 500, 50, 5, 8.00, 1),
                                                                                                                                 ('STANDARD', 'Most popular plan for growing businesses', 2500.00, 25000.00, 30, 1000, 100, 10, 6.00, 2),
                                                                                                                                 ('PREMIUM', 'Advanced plan with all features', 5000.00, 50000.00, 30, 5000, 500, 50, 4.00, 3),
                                                                                                                                 ('ENTERPRISE', 'Custom solutions for large chains', 10000.00, 100000.00, 30, NULL, NULL, NULL, 2.00, 4);

-- Restaurant Subscriptions Table
CREATE TABLE restaurant_subscriptions (
                                          id BIGSERIAL PRIMARY KEY,
                                          restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
                                          plan_id BIGINT NOT NULL REFERENCES subscription_plans(id),
                                          status VARCHAR(20) NOT NULL,
                                          start_date TIMESTAMP NOT NULL,
                                          expiry_date TIMESTAMP NOT NULL,
                                          billing_cycle VARCHAR(20),
                                          amount_paid DECIMAL(10,2),
                                          payment_date TIMESTAMP,
                                          payment_method VARCHAR(50),
                                          transaction_id VARCHAR(100),
                                          is_auto_renew BOOLEAN DEFAULT FALSE,
                                          cancelled_at TIMESTAMP,
                                          cancelled_by BIGINT REFERENCES users(id),
                                          cancellation_reason VARCHAR(500),
                                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_restaurant_sub_restaurant ON restaurant_subscriptions(restaurant_id);
CREATE INDEX idx_restaurant_sub_status ON restaurant_subscriptions(status);
CREATE INDEX idx_restaurant_sub_expiry ON restaurant_subscriptions(expiry_date);

-- Restaurant Branches Table
CREATE TABLE restaurant_branches (
                                     id BIGSERIAL PRIMARY KEY,
                                     restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
                                     branch_code VARCHAR(20) UNIQUE NOT NULL,
                                     branch_name VARCHAR(100) NOT NULL,
                                     contact_email VARCHAR(100),
                                     contact_phone VARCHAR(20),
                                     address VARCHAR(500) NOT NULL,
                                     city VARCHAR(100),
                                     zip_code VARCHAR(20),
                                     state VARCHAR(100),
                                     country VARCHAR(100),
                                     latitude DECIMAL(10,7),
                                     longitude DECIMAL(10,7),
                                     is_main_branch BOOLEAN DEFAULT FALSE,
                                     opening_time VARCHAR(10),
                                     closing_time VARCHAR(10),
                                     is_active BOOLEAN DEFAULT TRUE,
                                     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_branch_restaurant ON restaurant_branches(restaurant_id);
CREATE INDEX idx_branch_code ON restaurant_branches(branch_code);
CREATE INDEX idx_branch_location ON restaurant_branches(latitude, longitude);

-- Salesmen Table
CREATE TABLE salesmen (
                          id BIGSERIAL PRIMARY KEY,
                          salesman_code VARCHAR(20) UNIQUE NOT NULL,
                          user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
                          full_name VARCHAR(100) NOT NULL,
                          email VARCHAR(100) NOT NULL,
                          phone_number VARCHAR(20),
                          territory VARCHAR(500),
                          commission_percentage DECIMAL(5,2) DEFAULT 0.00,
                          total_onboarded INTEGER DEFAULT 0,
                          total_active INTEGER DEFAULT 0,
                          is_active BOOLEAN DEFAULT TRUE,
                          joined_date TIMESTAMP,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_salesman_user ON salesmen(user_id);
CREATE INDEX idx_salesman_code ON salesmen(salesman_code);

-- Restaurant Onboarding Table
CREATE TABLE restaurant_onboarding (
                                       id BIGSERIAL PRIMARY KEY,
                                       restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
                                       salesman_id BIGINT REFERENCES salesmen(id),
                                       status VARCHAR(20) NOT NULL,
                                       step_completed INTEGER DEFAULT 0,
                                       total_steps INTEGER DEFAULT 5,
                                       completed_steps VARCHAR(500),
                                       onboarded_at TIMESTAMP,
                                       notes TEXT,
                                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_onboarding_restaurant ON restaurant_onboarding(restaurant_id);
CREATE INDEX idx_onboarding_salesman ON restaurant_onboarding(salesman_id);
CREATE INDEX idx_onboarding_status ON restaurant_onboarding(status);

-- ============================================
-- SAMPLE API REQUESTS & RESPONSES
-- ============================================

/*
========================================
1. GET RESTAURANT DETAILS
========================================

GET /api/v1/restaurant/2
Authorization: Bearer <admin_token>

RESPONSE (200 OK):
{
  "success": true,
  "message": "Restaurant details fetched successfully",
  "data": {
    "id": 2,
    "restaurantCode": "REST8A3F7B9C",
    "name": "Golden Fork Restaurant",
    "email": "contact@goldenfork.com",
    "phoneNumber": "+8801712345678",
    "address": "123 Main Street, Gulshan, Dhaka",
    "settings": {
      "businessName": "Golden Fork Restaurant Pvt. Ltd.",
      "businessType": "CASUAL_DINING",
      "currency": "BDT",
      "timezone": "Asia/Dhaka",
      "language": "en",
      "taxPercentage": 5.00,
      "serviceChargePercentage": 10.00,
      "autoAcceptOrders": false,
      "allowOnlinePayments": true,
      "allowCashPayments": true,
      "minimumOrderAmount": 100.00,
      "deliveryRadiusKm": 10.00,
      "averagePreparationTimeMinutes": 30,
      "logoUrl": "https://cdn.example.com/logos/goldenfork.png",
      "bannerUrl": "https://cdn.example.com/banners/goldenfork.jpg"
    },
    "subscription": {
      "id": 1,
      "restaurantId": 2,
      "plan": {
        "id": 2,
        "name": "STANDARD",
        "description": "Most popular plan for growing businesses",
        "monthlyPrice": 2500.00,
        "yearlyPrice": 25000.00,
        "trialDays": 30,
        "maxOrdersPerMonth": 1000,
        "maxMenuItems": 100,
        "maxStaffUsers": 10,
        "commissionPercentage": 6.00
      },
      "status": "TRIAL",
      "startDate": "2026-01-22T10:45:00",
      "expiryDate": "2026-02-21T10:45:00",
      "daysRemaining": 29,
      "billingCycle": "MONTHLY",
      "isAutoRenew": false
    },
    "enabledFeatures": [
      "DINE_IN",
      "TAKEAWAY",
      "QR_ORDERING"
    ],
    "isActive": true,
    "createdAt": "2026-01-22T10:45:00"
  },
  "timestamp": "2026-01-23T14:30:00"
}

========================================
2. UPDATE RESTAURANT SETTINGS
========================================

PUT /api/v1/restaurant/2/settings
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "businessName": "Golden Fork Restaurant Pvt. Ltd.",
  "taxPercentage": 5.00,
  "serviceChargePercentage": 10.00,
  "autoAcceptOrders": true,
  "minimumOrderAmount": 150.00,
  "deliveryRadiusKm": 15.00,
  "averagePreparationTimeMinutes": 25
}

RESPONSE (200 OK):
{
  "success": true,
  "message": "Settings updated successfully",
  "data": {
    "businessName": "Golden Fork Restaurant Pvt. Ltd.",
    "businessType": "CASUAL_DINING",
    "currency": "BDT",
    "taxPercentage": 5.00,
    "serviceChargePercentage": 10.00,
    "autoAcceptOrders": true,
    "minimumOrderAmount": 150.00,
    "deliveryRadiusKm": 15.00,
    "averagePreparationTimeMinutes": 25
  },
  "timestamp": "2026-01-23T14:35:00"
}

========================================
3. TOGGLE SINGLE FEATURE
========================================

POST /api/v1/restaurant/2/features/toggle
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "featureName": "DELIVERY",
  "isEnabled": true,
  "notes": "Enabled delivery feature after onboarding delivery partners"
}

RESPONSE (200 OK):
{
  "success": true,
  "message": "Feature enabled successfully",
  "data": {
    "featureName": "DELIVERY",
    "isEnabled": true,
    "enabledAt": "2026-01-23T14:40:00",
    "enabledBy": 5
  },
  "timestamp": "2026-01-23T14:40:00"
}

========================================
4. BULK TOGGLE FEATURES (Salesman)
========================================

POST /api/v1/restaurant/2/features/bulk-toggle
Authorization: Bearer <salesman_token>
Content-Type: application/json

{
  "features": {
    "DINE_IN": true,
    "TAKEAWAY": true,
    "QR_ORDERING": true,
    "ONLINE_PAYMENT": true,
    "INVENTORY_MANAGEMENT": true,
    "ANALYTICS": true
  },
  "notes": "Initial feature setup during onboarding"
}

RESPONSE (200 OK):
{
  "success": true,
  "message": "Features updated successfully",
  "data": [
    {
      "featureName": "DINE_IN",
      "isEnabled": true,
      "enabledAt": "2026-01-23T14:45:00",
      "enabledBy": 3
    },
    {
      "featureName": "TAKEAWAY",
      "isEnabled": true,
      "enabledAt": "2026-01-23T14:45:00",
      "enabledBy": 3
    }
    // ... more features
  ],
  "timestamp": "2026-01-23T14:45:00"
}

========================================
5. GET ALL FEATURES
========================================

GET /api/v1/restaurant/2/features
Authorization: Bearer <admin_token>

RESPONSE (200 OK):
{
  "success": true,
  "message": "Features fetched successfully",
  "data": [
    {
      "featureName": "DINE_IN",
      "isEnabled": true,
      "enabledAt": "2026-01-23T14:45:00",
      "enabledBy": 3
    },
    {
      "featureName": "DELIVERY",
      "isEnabled": true,
      "enabledAt": "2026-01-23T14:40:00",
      "enabledBy": 5
    },
    {
      "featureName": "TABLE_RESERVATION",
      "isEnabled": false,
      "enabledAt": null,
      "enabledBy": null
    }
    // ... all 19 features
  ],
  "timestamp": "2026-01-23T14:50:00"
}

========================================
6. CREATE BRANCH
========================================

POST /api/v1/restaurant/2/branches
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "branchName": "Downtown Branch",
  "contactEmail": "downtown@goldenfork.com",
  "contactPhone": "+8801712345679",
  "address": "456 Park Avenue, Banani",
  "city": "Dhaka",
  "zipCode": "1213",
  "state": "Dhaka Division",
  "country": "Bangladesh",
  "latitude": 23.7925,
  "longitude": 90.4078,
  "openingTime": "10:00",
  "closingTime": "23:00",
  "isMainBranch": false
}

RESPONSE (200 OK):
{
  "success": true,
  "message": "Branch created successfully",
  "data": {
    "id": 1,
    "branchCode": "REST8A3F7B9C-B001",
    "branchName": "Downtown Branch",
    "contactEmail": "downtown@goldenfork.com",
    "contactPhone": "+8801712345679",
    "address": "456 Park Avenue, Banani",
    "city": "Dhaka",
    "latitude": 23.7925,
    "longitude": 90.4078,
    "isMainBranch": false,
    "openingTime": "10:00",
    "closingTime": "23:00",
    "isActive": true,
    "createdAt": "2026-01-23T15:00:00"
  },
  "timestamp": "2026-01-23T15:00:00"
}

========================================
7. SALESMAN - INITIATE ONBOARDING
========================================

POST /api/v1/salesman/onboard/2?salesmanId=1
Authorization: Bearer <salesman_token>

RESPONSE (200 OK):
{
  "success": true,
  "message": "Onboarding initiated successfully",
  "data": {
    "restaurantId": 2,
    "status": "INITIATED",
    "stepCompleted": 0,
    "totalSteps": 5,
    "completionPercentage": 0,
    "completedSteps": [],
    "salesmanName": "John Sales",
    "notes": null,
    "createdAt": "2026-01-23T15:10:00"
  },
  "timestamp": "2026-01-23T15:10:00"
}

========================================
8. UPDATE ONBOARDING STATUS
========================================

PUT /api/v1/salesman/onboarding/2/status?status=DOCUMENTS_VERIFIED&notes=All documents verified
Authorization: Bearer <salesman_token>

RESPONSE (200 OK):
{
  "success": true,
  "message": "Onboarding status updated successfully",
  "data": {
    "restaurantId": 2,
    "status": "DOCUMENTS_VERIFIED",
    "stepCompleted": 2,
    "totalSteps": 5,
    "completionPercentage": 40,
    "completedSteps": [],
    "salesmanName": "John Sales",
    "notes": "All documents verified",
    "createdAt": "2026-01-23T15:10:00"
  },
  "timestamp": "2026-01-23T15:20:00"
}

========================================
9. ASSIGN SUBSCRIPTION PLAN
========================================

POST /api/v1/salesman/subscription/assign?restaurantId=2&planName=STANDARD&billingCycle=MONTHLY
Authorization: Bearer <salesman_token>

RESPONSE (200 OK):
{
  "success": true,
  "message": "Subscription assigned successfully",
  "data": {
    "id": 1,
    "restaurantId": 2,
    "plan": {
      "id": 2,
      "name": "STANDARD",
      "monthlyPrice": 2500.00,
      "trialDays": 30
    },
    "status": "TRIAL",
    "startDate": "2026-01-23T15:25:00",
    "expiryDate": "2026-02-22T15:25:00",
    "daysRemaining": 30,
    "billingCycle": "MONTHLY",
    "amountPaid": 0.00,
    "isAutoRenew": false
  },
  "timestamp": "2026-01-23T15:25:00"
}

========================================
10. GET SUBSCRIPTION PLANS
========================================

GET /api/v1/salesman/subscription/plans
Authorization: Bearer <admin_token>

RESPONSE (200 OK):
{
  "success": true,
  "message": "Subscription plans fetched successfully",
  "data": [
    {
      "id": 1,
      "name": "BASIC",
      "description": "Starter plan for small restaurants",
      "monthlyPrice": 1500.00,
      "yearlyPrice": 15000.00,
      "trialDays": 30,
      "maxOrdersPerMonth": 500,
      "maxMenuItems": 50,
      "maxStaffUsers": 5,
      "commissionPercentage": 8.00,
      "isActive": true
    },
    {
      "id": 2,
      "name": "STANDARD",
      "description": "Most popular plan for growing businesses",
      "monthlyPrice": 2500.00,
      "yearlyPrice": 25000.00,
      "trialDays": 30,
      "maxOrdersPerMonth": 1000,
      "maxMenuItems": 100,
      "maxStaffUsers": 10,
      "commissionPercentage": 6.00,
      "isActive": true
    }
    // ... more plans
  ],
  "timestamp": "2026-01-23T15:30:00"
}

========================================
11. GET SALESMAN PERFORMANCE
========================================

GET /api/v1/salesman/1/performance
Authorization: Bearer <salesman_token>

RESPONSE (200 OK):
{
  "success": true,
  "message": "Salesman performance fetched successfully",
  "data": {
    "id": 1,
    "salesmanCode": "SM8A3F7B9C",
    "fullName": "John Sales",
    "totalOnboarded": 15,
    "totalActive": 12,
    "conversionRate": 80.00,
    "territory": "Dhaka North",
    "commissionPercentage": 5.00
  },
  "timestamp": "2026-01-23T15:35:00"
}
*/

-- ============================================
-- USEFUL QUERIES FOR MULTI-TENANT ISOLATION
-- ============================================

-- Get all features for a restaurant
SELECT * FROM restaurant_features
WHERE restaurant_id = 2
ORDER BY feature_name;

-- Get enabled features only
SELECT feature_name FROM restaurant_features
WHERE restaurant_id = 2 AND is_enabled = TRUE;

-- Check if specific feature is enabled
SELECT EXISTS(
    SELECT 1 FROM restaurant_features
    WHERE restaurant_id = 2
      AND feature_name = 'DELIVERY'
      AND is_enabled = TRUE
);

-- Get active subscription for restaurant
SELECT s.*, p.*
FROM restaurant_subscriptions s
         JOIN subscription_plans p ON s.plan_id = p.id
WHERE s.restaurant_id = 2
  AND s.status = 'ACTIVE'
ORDER BY s.created_at DESC
    LIMIT 1;

-- Get all branches for restaurant
SELECT * FROM restaurant_branches
WHERE restaurant_id = 2
  AND is_active = TRUE;

-- Get restaurants by salesman
SELECT r.*
FROM restaurants r
         JOIN restaurant_onboarding o ON r.id = o.restaurant_id
WHERE o.salesman_id = 1
ORDER BY r.created_at DESC;