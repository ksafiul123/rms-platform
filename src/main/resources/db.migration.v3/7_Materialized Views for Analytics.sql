-- 1. Daily Sales Summary (Refresh every hour)
CREATE MATERIALIZED VIEW mv_daily_sales_summary AS
SELECT
    r.id as restaurant_id,
    r.name as restaurant_name,
    DATE(o.created_at) as sale_date,
    COUNT(*) as total_orders,
    COUNT(DISTINCT o.customer_id) as unique_customers,
    SUM(o.total_amount) as gross_revenue,
    SUM(o.discount_amount) as total_discounts,
    SUM(o.tax_amount) as total_taxes,
    SUM(o.total_amount - o.discount_amount) as net_revenue,
    AVG(o.total_amount) as average_order_value,
    COUNT(CASE WHEN o.order_type = 'DINE_IN' THEN 1 END) as dine_in_orders,
    COUNT(CASE WHEN o.order_type = 'TAKEAWAY' THEN 1 END) as takeaway_orders,
    COUNT(CASE WHEN o.order_type = 'DELIVERY' THEN 1 END) as delivery_orders,
    SUM(CASE WHEN o.order_type = 'DINE_IN' THEN o.total_amount ELSE 0 END) as dine_in_revenue,
    SUM(CASE WHEN o.order_type = 'TAKEAWAY' THEN o.total_amount ELSE 0 END) as takeaway_revenue,
    SUM(CASE WHEN o.order_type = 'DELIVERY' THEN o.total_amount ELSE 0 END) as delivery_revenue
FROM orders o
         JOIN restaurants r ON o.restaurant_id = r.id
WHERE o.status IN ('COMPLETED', 'DELIVERED')
GROUP BY r.id, r.name, DATE(o.created_at);

CREATE UNIQUE INDEX idx_mv_daily_sales_pk
    ON mv_daily_sales_summary(restaurant_id, sale_date);

CREATE INDEX idx_mv_daily_sales_date
    ON mv_daily_sales_summary(sale_date DESC);

-- Refresh schedule
CREATE OR REPLACE FUNCTION refresh_daily_sales_summary()
    RETURNS void AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY mv_daily_sales_summary;
END;
$$ LANGUAGE plpgsql;

-- 2. Popular Menu Items (Refresh every 30 minutes)
CREATE MATERIALIZED VIEW mv_popular_menu_items AS
SELECT
    mi.id as menu_item_id,
    mi.restaurant_id,
    mi.name as item_name,
    mc.name as category_name,
    COUNT(oi.id) as times_ordered,
    SUM(oi.quantity) as total_quantity_sold,
    SUM(oi.subtotal) as total_revenue,
    AVG(oi.price) as average_price,
    DATE_TRUNC('month', o.created_at) as month,
    DENSE_RANK() OVER (
        PARTITION BY mi.restaurant_id, DATE_TRUNC('month', o.created_at)
        ORDER BY SUM(oi.quantity) DESC
        ) as popularity_rank
FROM order_items oi
         JOIN menu_items mi ON oi.menu_item_id = mi.id
         JOIN menu_categories mc ON mi.category_id = mc.id
         JOIN orders o ON oi.order_id = o.id
WHERE o.status IN ('COMPLETED', 'DELIVERED')
  AND o.created_at >= CURRENT_DATE - INTERVAL '12 months'
GROUP BY mi.id, mi.restaurant_id, mi.name, mc.name, DATE_TRUNC('month', o.created_at);

CREATE INDEX idx_mv_popular_items_restaurant
    ON mv_popular_menu_items(restaurant_id, month, popularity_rank);

-- 3. Customer Lifetime Value (Refresh daily)
CREATE MATERIALIZED VIEW mv_customer_lifetime_value AS
SELECT
    c.id as customer_id,
    c.email,
    r.id as restaurant_id,
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
GROUP BY c.id, c.email, r.id;

CREATE UNIQUE INDEX idx_mv_clv_pk
    ON mv_customer_lifetime_value(customer_id, restaurant_id);

CREATE INDEX idx_mv_clv_segment
    ON mv_customer_lifetime_value(restaurant_id, customer_segment, lifetime_value DESC);

-- 4. Inventory Usage Summary (Refresh every 6 hours)
CREATE MATERIALIZED VIEW mv_inventory_usage_summary AS
SELECT
    ii.id as inventory_item_id,
    ii.restaurant_id,
    ii.name as item_name,
    ii.category,
    DATE_TRUNC('day', st.transaction_timestamp) as usage_date,
    SUM(CASE WHEN st.transaction_type = 'ORDER_DEDUCTION'
                 THEN ABS(st.quantity) ELSE 0 END) as consumed,
    SUM(CASE WHEN st.transaction_type = 'WASTAGE'
                 THEN ABS(st.quantity) ELSE 0 END) as wastage,
    SUM(CASE WHEN st.transaction_type = 'PURCHASE'
                 THEN st.quantity ELSE 0 END) as purchased,
    SUM(CASE WHEN st.transaction_type = 'ORDER_DEDUCTION'
                 THEN st.total_cost ELSE 0 END) as consumption_cost,
    ROUND(
            SUM(CASE WHEN st.transaction_type = 'WASTAGE' THEN st.quantity ELSE 0 END)::NUMERIC /
            NULLIF(SUM(CASE WHEN st.transaction_type = 'ORDER_DEDUCTION' THEN st.quantity ELSE 0 END), 0) * 100,
            2
    ) as wastage_percentage
FROM inventory_items ii
         JOIN stock_transactions st ON ii.id = st.inventory_item_id
WHERE st.transaction_timestamp >= CURRENT_DATE - INTERVAL '90 days'
GROUP BY ii.id, ii.restaurant_id, ii.name, ii.category, DATE_TRUNC('day', st.transaction_timestamp);

CREATE INDEX idx_mv_inventory_usage_restaurant
    ON mv_inventory_usage_summary(restaurant_id, usage_date DESC);

-- Automated refresh jobs
CREATE EXTENSION IF NOT EXISTS pg_cron;

-- Refresh every hour (daily sales)
SELECT cron.schedule(
               'refresh_daily_sales',
               '0 * * * *',
               'SELECT refresh_daily_sales_summary()'
       );

-- Refresh every 30 minutes (popular items)
SELECT cron.schedule(
               'refresh_popular_items',
               '*/30 * * * *',
               'REFRESH MATERIALIZED VIEW CONCURRENTLY mv_popular_menu_items'
       );

-- Refresh daily at 2 AM (customer LTV)
SELECT cron.schedule(
               'refresh_customer_ltv',
               '0 2 * * *',
               'REFRESH MATERIALIZED VIEW CONCURRENTLY mv_customer_lifetime_value'
       );

-- Refresh every 6 hours (inventory usage)
SELECT cron.schedule(
               'refresh_inventory_usage',
               '0 */6 * * *',
               'REFRESH MATERIALIZED VIEW CONCURRENTLY mv_inventory_usage_summary'
       );