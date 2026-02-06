-- =====================================================
-- RESTAURANT MODULE - OPTIMIZED SCHEMA
-- =====================================================

-- ============== RESTAURANTS TABLE ==============
ALTER TABLE restaurants
    ADD CONSTRAINT chk_restaurants_email_format
        CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}$'),
    ADD CONSTRAINT chk_restaurants_phone_format
        CHECK (phone ~* '^\+?[1-9]\d{1,14}$'),
    ADD CONSTRAINT chk_restaurants_rating_range
        CHECK (rating >= 0 AND rating <= 5);

CREATE UNIQUE INDEX IF NOT EXISTS idx_restaurants_code
    ON restaurants (restaurant_code);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_restaurants_active
    ON restaurants (is_active, created_at DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_restaurants_location
    ON restaurants USING gist (
                               ll_to_earth(latitude, longitude)
        ) WHERE latitude IS NOT NULL AND longitude IS NOT NULL;
-- Note: Requires earthdistance extension
-- CREATE EXTENSION IF NOT EXISTS earthdistance CASCADE;

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_restaurants_search
    ON restaurants USING gin (
                              to_tsvector('english', name || ' ' || COALESCE(description, ''))
        );

-- ============== RESTAURANT_SETTINGS TABLE ==============
ALTER TABLE restaurant_settings
    ADD CONSTRAINT fk_restaurant_settings_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE,
    ADD CONSTRAINT chk_settings_tax_rate
        CHECK (tax_rate >= 0 AND tax_rate <= 100),
    ADD CONSTRAINT chk_settings_service_charge
        CHECK (service_charge_percentage >= 0 AND service_charge_percentage <= 100);

CREATE UNIQUE INDEX IF NOT EXISTS idx_restaurant_settings_restaurant
    ON restaurant_settings (restaurant_id);

-- ============== RESTAURANT_FEATURES TABLE ==============
ALTER TABLE restaurant_features
    ADD CONSTRAINT fk_restaurant_features_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_restaurant_features_feature
        FOREIGN KEY (feature_id) REFERENCES features(id) ON DELETE RESTRICT;

CREATE UNIQUE INDEX IF NOT EXISTS idx_restaurant_features_unique
    ON restaurant_features (restaurant_id, feature_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_restaurant_features_enabled
    ON restaurant_features (restaurant_id, is_enabled)
    WHERE is_enabled = true;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_restaurant_features_feature
    ON restaurant_features (feature_id, is_enabled);

-- ============== SUBSCRIPTION_PLANS TABLE ==============
ALTER TABLE subscription_plans
    ADD CONSTRAINT chk_plans_price_positive
        CHECK (price >= 0),
    ADD CONSTRAINT chk_plans_commission_range
        CHECK (commission_percentage >= 0 AND commission_percentage <= 100),
    ADD CONSTRAINT chk_plans_limits_positive
        CHECK (
            max_orders_per_month >= 0 AND
            max_menu_items >= 0 AND
            max_staff_users >= 0
            );

CREATE UNIQUE INDEX IF NOT EXISTS idx_subscription_plans_name
    ON subscription_plans (name);
CREATE INDEX IF NOT EXISTS idx_subscription_plans_active
    ON subscription_plans (is_active)
    WHERE is_active = true;

-- ============== RESTAURANT_SUBSCRIPTIONS TABLE ==============
ALTER TABLE restaurant_subscriptions
    ADD CONSTRAINT fk_restaurant_subscriptions_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_restaurant_subscriptions_plan
        FOREIGN KEY (plan_id) REFERENCES subscription_plans(id) ON DELETE RESTRICT,
    ADD CONSTRAINT chk_subscriptions_dates
        CHECK (expiry_date > start_date);

CREATE UNIQUE INDEX IF NOT EXISTS idx_restaurant_subscriptions_restaurant_active
    ON restaurant_subscriptions (restaurant_id)
    WHERE status = 'ACTIVE';
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_restaurant_subscriptions_expiry
    ON restaurant_subscriptions (expiry_date)
    WHERE status = 'ACTIVE';
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_restaurant_subscriptions_status
    ON restaurant_subscriptions (status, expiry_date);

-- ============== RESTAURANT_BRANCHES TABLE ==============
ALTER TABLE restaurant_branches
    ADD CONSTRAINT fk_restaurant_branches_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE,
    ADD CONSTRAINT chk_branches_phone_format
        CHECK (phone ~* '^\+?[1-9]\d{1,14}$');

CREATE UNIQUE INDEX IF NOT EXISTS idx_restaurant_branches_code
    ON restaurant_branches (branch_code);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_restaurant_branches_restaurant
    ON restaurant_branches (restaurant_id, is_active);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_restaurant_branches_location
    ON restaurant_branches USING gist (
                                       ll_to_earth(latitude, longitude)
        ) WHERE latitude IS NOT NULL AND longitude IS NOT NULL;

-- ============== SALESMEN TABLE ==============
ALTER TABLE salesmen
    ADD CONSTRAINT fk_salesmen_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    ADD CONSTRAINT chk_salesmen_phone_format
        CHECK (phone ~* '^\+?[1-9]\d{1,14}$'),
    ADD CONSTRAINT chk_salesmen_commission_range
        CHECK (commission_percentage >= 0 AND commission_percentage <= 100);

CREATE UNIQUE INDEX IF NOT EXISTS idx_salesmen_user
    ON salesmen (user_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_salesmen_code
    ON salesmen (salesman_code);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_salesmen_active
    ON salesmen (is_active, created_at DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_salesmen_territory
    ON salesmen (territory) WHERE territory IS NOT NULL;

-- ============== RESTAURANT_ONBOARDING TABLE ==============
ALTER TABLE restaurant_onboarding
    ADD CONSTRAINT fk_restaurant_onboarding_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_restaurant_onboarding_salesman
        FOREIGN KEY (salesman_id) REFERENCES salesmen(id) ON DELETE SET NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_restaurant_onboarding_restaurant
    ON restaurant_onboarding (restaurant_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_restaurant_onboarding_salesman
    ON restaurant_onboarding (salesman_id, current_step);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_restaurant_onboarding_status
    ON restaurant_onboarding (current_step, updated_at DESC);

-- ============== PERFORMANCE OPTIMIZATION ==============

-- Materialized view for subscription analytics
CREATE MATERIALIZED VIEW IF NOT EXISTS mv_restaurant_subscription_summary AS
SELECT
    r.id as restaurant_id,
    r.name as restaurant_name,
    r.restaurant_code,
    rs.status as subscription_status,
    sp.name as plan_name,
    sp.price as monthly_price,
    rs.start_date,
    rs.expiry_date,
    EXTRACT(DAY FROM (rs.expiry_date - CURRENT_DATE)) as days_until_expiry,
    rs.trial_end_date,
    CASE
        WHEN rs.trial_end_date > CURRENT_DATE THEN true
        ELSE false
        END as is_in_trial,
    COUNT(DISTINCT rf.id) FILTER (WHERE rf.is_enabled = true) as enabled_features_count
FROM restaurants r
         JOIN restaurant_subscriptions rs ON r.id = rs.restaurant_id
         JOIN subscription_plans sp ON rs.plan_id = sp.id
         LEFT JOIN restaurant_features rf ON r.id = rf.restaurant_id
WHERE rs.status = 'ACTIVE'
GROUP BY r.id, r.name, r.restaurant_code, rs.status, sp.name, sp.price,
         rs.start_date, rs.expiry_date, rs.trial_end_date;

CREATE UNIQUE INDEX idx_mv_restaurant_subscription_restaurant
    ON mv_restaurant_subscription_summary (restaurant_id);
CREATE INDEX idx_mv_restaurant_subscription_expiry
    ON mv_restaurant_subscription_summary (days_until_expiry);

-- Function to auto-refresh materialized views
CREATE OR REPLACE FUNCTION refresh_restaurant_views()
    RETURNS void AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY mv_restaurant_subscription_summary;
END;
$$ LANGUAGE plpgsql;