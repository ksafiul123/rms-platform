-- V16__create_analytics_reporting_system.sql
-- Analytics & Reporting System
-- Comprehensive analytics with 8 tables, 6 views, and automated report generation

-- ============================================
-- 1. Analytics Snapshots Table
-- Pre-calculated analytics for fast loading
-- ============================================

CREATE TABLE analytics_snapshots (
                                     id BIGSERIAL PRIMARY KEY,
                                     restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
                                     report_type VARCHAR(50) NOT NULL,
                                     time_period VARCHAR(30) NOT NULL,
                                     snapshot_date DATE NOT NULL,
                                     data_json TEXT NOT NULL,
                                     generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     is_cached BOOLEAN NOT NULL DEFAULT TRUE,
                                     cache_expires_at TIMESTAMP,
                                     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_snapshot_restaurant_date ON analytics_snapshots(restaurant_id, snapshot_date, report_type);
CREATE INDEX idx_snapshot_date ON analytics_snapshots(snapshot_date);

-- ============================================
-- 2. Sales Reports Table
-- Detailed sales analytics
-- ============================================

CREATE TABLE sales_reports (
                               id BIGSERIAL PRIMARY KEY,
                               restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
                               report_date DATE NOT NULL,
                               period_type VARCHAR(30) NOT NULL,

    -- Sales Metrics
                               total_orders INTEGER NOT NULL DEFAULT 0,
                               total_revenue DECIMAL(12, 2) DEFAULT 0,
                               total_discounts DECIMAL(12, 2) DEFAULT 0,
                               total_taxes DECIMAL(12, 2) DEFAULT 0,
                               net_revenue DECIMAL(12, 2) DEFAULT 0,
                               total_tips DECIMAL(12, 2) DEFAULT 0,

    -- Order Breakdown
                               dine_in_orders INTEGER NOT NULL DEFAULT 0,
                               dine_in_revenue DECIMAL(12, 2) DEFAULT 0,
                               takeaway_orders INTEGER NOT NULL DEFAULT 0,
                               takeaway_revenue DECIMAL(12, 2) DEFAULT 0,
                               delivery_orders INTEGER NOT NULL DEFAULT 0,
                               delivery_revenue DECIMAL(12, 2) DEFAULT 0,

    -- Customer Metrics
                               new_customers INTEGER NOT NULL DEFAULT 0,
                               returning_customers INTEGER NOT NULL DEFAULT 0,
                               unique_customers INTEGER NOT NULL DEFAULT 0,

    -- Performance Metrics
                               average_order_value DECIMAL(10, 2),
                               average_items_per_order DECIMAL(5, 2),
                               order_completion_rate DECIMAL(5, 2),
                               cancelled_orders INTEGER NOT NULL DEFAULT 0,

    -- Payment Methods
                               cash_payments DECIMAL(12, 2) DEFAULT 0,
                               card_payments DECIMAL(12, 2) DEFAULT 0,
                               online_payments DECIMAL(12, 2) DEFAULT 0,
                               wallet_payments DECIMAL(12, 2) DEFAULT 0,

    -- Time Analysis
                               peak_hour VARCHAR(5),
                               peak_hour_revenue DECIMAL(12, 2),
                               hourly_data_json TEXT,

                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               UNIQUE(restaurant_id, report_date, period_type)
);

CREATE INDEX idx_sales_restaurant_date ON sales_reports(restaurant_id, report_date);
CREATE INDEX idx_sales_date ON sales_reports(report_date);

-- ============================================
-- 3. Menu Performance Reports Table
-- ============================================

CREATE TABLE menu_performance_reports (
                                          id BIGSERIAL PRIMARY KEY,
                                          restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
                                          menu_item_id BIGINT NOT NULL REFERENCES menu_items(id) ON DELETE CASCADE,
                                          category_id BIGINT REFERENCES menu_categories(id) ON DELETE SET NULL,
                                          report_date DATE NOT NULL,
                                          period_type VARCHAR(30) NOT NULL,

    -- Sales Metrics
                                          quantity_sold INTEGER NOT NULL DEFAULT 0,
                                          total_revenue DECIMAL(12, 2) DEFAULT 0,
                                          total_cost DECIMAL(12, 2) DEFAULT 0,
                                          gross_profit DECIMAL(12, 2) DEFAULT 0,
                                          profit_margin DECIMAL(5, 2),

    -- Rankings
                                          popularity_rank INTEGER,
                                          revenue_rank INTEGER,
                                          profit_rank INTEGER,
                                          average_price DECIMAL(10, 2),
                                          discount_percentage DECIMAL(5, 2),

    -- Customer Behavior
                                          unique_customers INTEGER NOT NULL DEFAULT 0,
                                          repeat_customer_percentage DECIMAL(5, 2),
                                          average_rating DECIMAL(3, 2),
                                          total_reviews INTEGER NOT NULL DEFAULT 0,

    -- Operations
                                          average_prep_time_minutes INTEGER,
                                          stock_out_incidents INTEGER NOT NULL DEFAULT 0,
                                          refund_count INTEGER NOT NULL DEFAULT 0,
                                          peak_time_sales VARCHAR(50),
                                          slow_time_sales VARCHAR(50),

                                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_menu_perf_restaurant_date ON menu_performance_reports(restaurant_id, report_date);
CREATE INDEX idx_menu_perf_item ON menu_performance_reports(menu_item_id, report_date);

-- ============================================
-- 4. Customer Behavior Reports Table
-- ============================================

CREATE TABLE customer_behavior_reports (
                                           id BIGSERIAL PRIMARY KEY,
                                           restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
                                           customer_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
                                           report_date DATE NOT NULL,
                                           period_type VARCHAR(30) NOT NULL,

    -- Order Metrics
                                           total_orders INTEGER NOT NULL DEFAULT 0,
                                           total_spent DECIMAL(12, 2) DEFAULT 0,
                                           average_order_value DECIMAL(10, 2),
                                           largest_order_value DECIMAL(10, 2),

    -- Frequency
                                           visit_frequency_days INTEGER,
                                           days_since_last_order INTEGER,
                                           first_order_date DATE,
                                           last_order_date DATE,

    -- Lifetime Value
                                           lifetime_value DECIMAL(12, 2),
                                           predicted_ltv DECIMAL(12, 2),

    -- Segmentation
                                           customer_segment VARCHAR(30),
                                           rfm_segment VARCHAR(30),

    -- Preferences
                                           favorite_category VARCHAR(100),
                                           favorite_item VARCHAR(200),
                                           preferred_order_type VARCHAR(30),
                                           preferred_time_slot VARCHAR(50),

    -- Engagement
                                           discount_usage_count INTEGER NOT NULL DEFAULT 0,
                                           loyalty_points_earned INTEGER NOT NULL DEFAULT 0,
                                           reviews_written INTEGER NOT NULL DEFAULT 0,
                                           average_rating_given DECIMAL(3, 2),

    -- Churn Risk
                                           churn_risk_score DECIMAL(5, 2),
                                           is_at_risk BOOLEAN NOT NULL DEFAULT FALSE,

                                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                           updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_customer_behavior_restaurant_date ON customer_behavior_reports(restaurant_id, report_date);
CREATE INDEX idx_customer_behavior_customer ON customer_behavior_reports(customer_id, report_date);

-- ============================================
-- 5. Inventory Usage Reports Table
-- ============================================

CREATE TABLE inventory_usage_reports (
                                         id BIGSERIAL PRIMARY KEY,
                                         restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
                                         inventory_item_id BIGINT NOT NULL REFERENCES inventory_items(id) ON DELETE CASCADE,
                                         report_date DATE NOT NULL,
                                         period_type VARCHAR(30) NOT NULL,

    -- Stock Metrics
                                         opening_stock DECIMAL(10, 3),
                                         purchases DECIMAL(10, 3) DEFAULT 0,
                                         total_consumed DECIMAL(10, 3) DEFAULT 0,
                                         wastage DECIMAL(10, 3) DEFAULT 0,
                                         closing_stock DECIMAL(10, 3),

    -- Cost Metrics
                                         total_cost DECIMAL(12, 2) DEFAULT 0,
                                         cost_per_unit DECIMAL(10, 2),
                                         wastage_cost DECIMAL(12, 2) DEFAULT 0,

    -- Efficiency
                                         usage_efficiency DECIMAL(5, 2),
                                         wastage_percentage DECIMAL(5, 2),
                                         stock_turnover_ratio DECIMAL(5, 2),
                                         days_to_consume INTEGER,

    -- Menu Impact
                                         dishes_prepared INTEGER NOT NULL DEFAULT 0,
                                         average_per_dish DECIMAL(10, 3),

    -- Stock Status
                                         stock_out_incidents INTEGER NOT NULL DEFAULT 0,
                                         low_stock_alerts INTEGER NOT NULL DEFAULT 0,
                                         reorder_triggered INTEGER NOT NULL DEFAULT 0,

    -- Supplier
                                         supplier_name VARCHAR(200),
                                         purchase_frequency INTEGER,
                                         average_delivery_time_days INTEGER,

                                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_inventory_usage_restaurant_date ON inventory_usage_reports(restaurant_id, report_date);
CREATE INDEX idx_inventory_usage_item ON inventory_usage_reports(inventory_item_id, report_date);

-- ============================================
-- 6. Revenue Reports Table
-- ============================================

CREATE TABLE revenue_reports (
                                 id BIGSERIAL PRIMARY KEY,
                                 restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
                                 report_date DATE NOT NULL,
                                 period_type VARCHAR(30) NOT NULL,

    -- Revenue Breakdown
                                 gross_revenue DECIMAL(12, 2) NOT NULL DEFAULT 0,
                                 food_revenue DECIMAL(12, 2) DEFAULT 0,
                                 beverage_revenue DECIMAL(12, 2) DEFAULT 0,
                                 delivery_charges DECIMAL(12, 2) DEFAULT 0,
                                 service_charges DECIMAL(12, 2) DEFAULT 0,

    -- Deductions
                                 total_discounts DECIMAL(12, 2) DEFAULT 0,
                                 total_refunds DECIMAL(12, 2) DEFAULT 0,
                                 platform_commission DECIMAL(12, 2) DEFAULT 0,
                                 payment_gateway_fees DECIMAL(12, 2) DEFAULT 0,

    -- Net Revenue
                                 net_revenue DECIMAL(12, 2) NOT NULL DEFAULT 0,

    -- Costs
                                 cost_of_goods_sold DECIMAL(12, 2) DEFAULT 0,
                                 operating_expenses DECIMAL(12, 2) DEFAULT 0,
                                 staff_costs DECIMAL(12, 2) DEFAULT 0,

    -- Profitability
                                 gross_profit DECIMAL(12, 2),
                                 net_profit DECIMAL(12, 2),
                                 gross_profit_margin DECIMAL(5, 2),
                                 net_profit_margin DECIMAL(5, 2),

    -- Taxes
                                 total_taxes DECIMAL(12, 2) DEFAULT 0,
                                 vat_collected DECIMAL(12, 2) DEFAULT 0,
                                 service_tax DECIMAL(12, 2) DEFAULT 0,

    -- Comparison
                                 previous_period_revenue DECIMAL(12, 2),
                                 revenue_growth_percentage DECIMAL(5, 2),
                                 target_revenue DECIMAL(12, 2),
                                 target_achievement_percentage DECIMAL(5, 2),

                                 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 UNIQUE(restaurant_id, report_date, period_type)
);

CREATE INDEX idx_revenue_restaurant_date ON revenue_reports(restaurant_id, report_date);

-- ============================================
-- 7. Scheduled Reports Table
-- ============================================

CREATE TABLE scheduled_reports (
                                   id BIGSERIAL PRIMARY KEY,
                                   restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
                                   report_name VARCHAR(200) NOT NULL,
                                   report_type VARCHAR(50) NOT NULL,
                                   frequency VARCHAR(30) NOT NULL,
                                   schedule_time VARCHAR(5),
                                   schedule_day_of_week INTEGER,
                                   schedule_day_of_month INTEGER,
                                   export_format VARCHAR(20) NOT NULL,
                                   recipients TEXT,
                                   filters_json TEXT,
                                   is_active BOOLEAN NOT NULL DEFAULT TRUE,
                                   last_run_at TIMESTAMP,
                                   next_run_at TIMESTAMP,
                                   execution_count INTEGER NOT NULL DEFAULT 0,
                                   last_status VARCHAR(30),
                                   last_error TEXT,
                                   created_by BIGINT REFERENCES users(id),
                                   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_scheduled_restaurant ON scheduled_reports(restaurant_id);
CREATE INDEX idx_scheduled_active ON scheduled_reports(is_active, next_run_at);

-- ============================================
-- 8. Report Exports Table
-- ============================================

CREATE TABLE report_exports (
                                id BIGSERIAL PRIMARY KEY,
                                restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
                                user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                scheduled_report_id BIGINT REFERENCES scheduled_reports(id) ON DELETE SET NULL,
                                report_type VARCHAR(50) NOT NULL,
                                report_name VARCHAR(200) NOT NULL,
                                start_date DATE,
                                end_date DATE,
                                export_format VARCHAR(20) NOT NULL,
                                file_path VARCHAR(500),
                                file_size_bytes BIGINT,
                                download_url VARCHAR(500),
                                expires_at TIMESTAMP,
                                status VARCHAR(30) NOT NULL,
                                generated_at TIMESTAMP,
                                download_count INTEGER NOT NULL DEFAULT 0,
                                last_downloaded_at TIMESTAMP,
                                error_message TEXT,
                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_export_restaurant ON report_exports(restaurant_id);
CREATE INDEX idx_export_user ON report_exports(user_id);
CREATE INDEX idx_export_created ON report_exports(created_at);

-- ============================================
-- ANALYTICAL VIEWS
-- Pre-calculated analytics for fast reporting
-- ============================================

-- View 1: Daily Sales Summary
CREATE OR REPLACE VIEW daily_sales_summary AS
SELECT
    r.id as restaurant_id,
    r.name as restaurant_name,
    DATE(o.created_at) as sale_date,
    COUNT(*) as total_orders,
    SUM(o.total_amount) as gross_revenue,
    SUM(o.discount_amount) as total_discounts,
    SUM(o.tax_amount) as total_taxes,
    SUM(o.total_amount - o.discount_amount) as net_revenue,
    AVG(o.total_amount) as average_order_value,
    COUNT(DISTINCT o.customer_id) as unique_customers,
    COUNT(CASE WHEN o.order_type = 'DINE_IN' THEN 1 END) as dine_in_orders,
    COUNT(CASE WHEN o.order_type = 'TAKEAWAY' THEN 1 END) as takeaway_orders,
    COUNT(CASE WHEN o.order_type = 'DELIVERY' THEN 1 END) as delivery_orders,
    COUNT(CASE WHEN o.status = 'CANCELLED' THEN 1 END) as cancelled_orders
FROM orders o
         JOIN restaurants r ON o.restaurant_id = r.id
WHERE o.status IN ('COMPLETED', 'DELIVERED')
GROUP BY r.id, r.name, DATE(o.created_at);

-- View 2: Popular Menu Items
CREATE OR REPLACE VIEW popular_menu_items AS
SELECT
    mi.id as menu_item_id,
    mi.name as item_name,
    mc.name as category_name,
    r.id as restaurant_id,
    r.name as restaurant_name,
    COUNT(oi.id) as times_ordered,
    SUM(oi.quantity) as total_quantity_sold,
    SUM(oi.subtotal) as total_revenue,
    AVG(oi.price) as average_price,
    COUNT(DISTINCT oi.order_id) as unique_orders,
    DENSE_RANK() OVER (
        PARTITION BY r.id
        ORDER BY SUM(oi.quantity) DESC
        ) as popularity_rank
FROM order_items oi
         JOIN menu_items mi ON oi.menu_item_id = mi.id
         JOIN menu_categories mc ON mi.category_id = mc.id
         JOIN restaurants r ON mi.restaurant_id = r.id
         JOIN orders o ON oi.order_id = o.id
WHERE o.status IN ('COMPLETED', 'DELIVERED')
  AND o.created_at >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY mi.id, mi.name, mc.name, r.id, r.name;

-- View 3: Customer Lifetime Value
CREATE OR REPLACE VIEW customer_lifetime_value AS
SELECT
    c.id as customer_id,
    c.full_name as customer_name,
    c.email,
    r.id as restaurant_id,
    r.name as restaurant_name,
    COUNT(o.id) as total_orders,
    SUM(o.total_amount) as lifetime_value,
    AVG(o.total_amount) as average_order_value,
    MIN(o.created_at) as first_order_date,
    MAX(o.created_at) as last_order_date,
    EXTRACT(DAYS FROM (MAX(o.created_at) - MIN(o.created_at))) as customer_age_days,
    EXTRACT(DAYS FROM (CURRENT_DATE - MAX(o.created_at)::DATE)) as days_since_last_order,
    CASE
        WHEN COUNT(o.id) >= 10 THEN 'VIP'
        WHEN COUNT(o.id) >= 5 THEN 'REGULAR'
        WHEN COUNT(o.id) >= 2 THEN 'RETURNING'
        ELSE 'NEW'
        END as customer_segment
FROM users c
         JOIN orders o ON c.id = o.customer_id
         JOIN restaurants r ON o.restaurant_id = r.id
WHERE o.status IN ('COMPLETED', 'DELIVERED')
GROUP BY c.id, c.full_name, c.email, r.id, r.name;

-- View 4: Inventory Usage Summary
CREATE OR REPLACE VIEW inventory_usage_summary AS
SELECT
    ii.id as inventory_item_id,
    ii.name as item_name,
    ii.category,
    r.id as restaurant_id,
    r.name as restaurant_name,
    DATE_TRUNC('day', st.transaction_timestamp) as usage_date,
    SUM(CASE WHEN st.transaction_type = 'ORDER_DEDUCTION'
                 THEN ABS(st.quantity) ELSE 0 END) as consumed,
    SUM(CASE WHEN st.transaction_type = 'WASTAGE'
                 THEN ABS(st.quantity) ELSE 0 END) as wastage,
    SUM(CASE WHEN st.transaction_type = 'PURCHASE'
                 THEN st.quantity ELSE 0 END) as purchased,
    SUM(CASE WHEN st.transaction_type = 'ORDER_DEDUCTION'
                 THEN st.total_cost ELSE 0 END) as consumption_cost,
    SUM(CASE WHEN st.transaction_type = 'WASTAGE'
                 THEN st.total_cost ELSE 0 END) as wastage_cost
FROM inventory_items ii
         JOIN stock_transactions st ON ii.id = st.inventory_item_id
         JOIN restaurants r ON ii.restaurant_id = r.id
GROUP BY ii.id, ii.name, ii.category, r.id, r.name,
         DATE_TRUNC('day', st.transaction_timestamp);

-- View 5: Revenue Breakdown
CREATE OR REPLACE VIEW revenue_breakdown AS
SELECT
    r.id as restaurant_id,
    r.name as restaurant_name,
    DATE_TRUNC('month', o.created_at) as month,
    SUM(o.total_amount) as gross_revenue,
    SUM(o.discount_amount) as total_discounts,
    SUM(o.tax_amount) as total_taxes,
    SUM(o.total_amount - o.discount_amount) as net_revenue,
    SUM(o.delivery_fee) as delivery_charges,
    SUM(COALESCE(os.commission_amount, 0)) as platform_commission,
    SUM(o.total_amount - o.discount_amount - COALESCE(os.commission_amount, 0)) as restaurant_revenue
FROM orders o
         JOIN restaurants r ON o.restaurant_id = r.id
         LEFT JOIN order_settlements os ON o.id = os.order_id
WHERE o.status IN ('COMPLETED', 'DELIVERED')
GROUP BY r.id, r.name, DATE_TRUNC('month', o.created_at);

-- View 6: Peak Hours Analysis
CREATE OR REPLACE VIEW peak_hours_analysis AS
SELECT
    r.id as restaurant_id,
    r.name as restaurant_name,
    EXTRACT(HOUR FROM o.created_at) as hour_of_day,
    EXTRACT(DOW FROM o.created_at) as day_of_week,
    COUNT(*) as order_count,
    SUM(o.total_amount) as revenue,
    AVG(o.total_amount) as avg_order_value,
    DENSE_RANK() OVER (
        PARTITION BY r.id, EXTRACT(DOW FROM o.created_at)
        ORDER BY COUNT(*) DESC
        ) as busy_rank
FROM orders o
         JOIN restaurants r ON o.restaurant_id = r.id
WHERE o.status IN ('COMPLETED', 'DELIVERED')
  AND o.created_at >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY r.id, r.name,
         EXTRACT(HOUR FROM o.created_at),
         EXTRACT(DOW FROM o.created_at);

-- ============================================
-- TRIGGERS & FUNCTIONS
-- ============================================

-- Function to clean expired snapshots
CREATE OR REPLACE FUNCTION cleanup_expired_snapshots()
    RETURNS void AS $$
BEGIN
    DELETE FROM analytics_snapshots
    WHERE cache_expires_at < CURRENT_TIMESTAMP;

    DELETE FROM report_exports
    WHERE expires_at < CURRENT_TIMESTAMP
      AND status = 'COMPLETED';
END;
$$ LANGUAGE plpgsql;

-- Function to update scheduled report next run time
CREATE OR REPLACE FUNCTION update_next_run_time()
    RETURNS TRIGGER AS $$
BEGIN
    IF NEW.frequency = 'DAILY' THEN
        NEW.next_run_at := CURRENT_DATE + INTERVAL '1 day' +
                           (NEW.schedule_time || ':00')::TIME;
    ELSIF NEW.frequency = 'WEEKLY' THEN
        NEW.next_run_at := CURRENT_DATE +
                           ((NEW.schedule_day_of_week - EXTRACT(DOW FROM CURRENT_DATE))::INTEGER % 7) * INTERVAL '1 day' +
                           (NEW.schedule_time || ':00')::TIME;
    ELSIF NEW.frequency = 'MONTHLY' THEN
        NEW.next_run_at := DATE_TRUNC('month', CURRENT_DATE) +
                           (NEW.schedule_day_of_month - 1) * INTERVAL '1 day' +
                           (NEW.schedule_time || ':00')::TIME;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_next_run_time
    BEFORE INSERT OR UPDATE ON scheduled_reports
    FOR EACH ROW
    WHEN (NEW.is_active = TRUE)
EXECUTE FUNCTION update_next_run_time();

-- ============================================
-- SCHEDULED CLEANUP JOB
-- Run daily to clean up expired data
-- ============================================

COMMENT ON TABLE analytics_snapshots IS 'Pre-calculated analytics snapshots for fast reporting';
COMMENT ON TABLE sales_reports IS 'Daily/weekly/monthly sales analytics';
COMMENT ON TABLE menu_performance_reports IS 'Menu item performance and profitability';
COMMENT ON TABLE customer_behavior_reports IS 'Customer segmentation and behavior analysis';
COMMENT ON TABLE inventory_usage_reports IS 'Inventory consumption and wastage tracking';
COMMENT ON TABLE revenue_reports IS 'Comprehensive revenue and profitability reports';
COMMENT ON TABLE scheduled_reports IS 'Automated report scheduling';
COMMENT ON TABLE report_exports IS 'Exported report tracking and download management';

COMMENT ON VIEW daily_sales_summary IS 'Daily sales summary with order breakdown';
COMMENT ON VIEW popular_menu_items IS 'Most popular menu items ranked by sales';
COMMENT ON VIEW customer_lifetime_value IS 'Customer lifetime value and segmentation';
COMMENT ON VIEW inventory_usage_summary IS 'Daily inventory usage and wastage';
COMMENT ON VIEW revenue_breakdown IS 'Monthly revenue breakdown with commissions';
COMMENT ON VIEW peak_hours_analysis IS 'Peak hours and busy times analysis';