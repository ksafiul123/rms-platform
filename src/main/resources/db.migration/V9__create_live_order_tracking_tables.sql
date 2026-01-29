-- V10__create_live_order_tracking_tables.sql

-- Order Status History
CREATE TABLE order_status_history (
                                      id BIGSERIAL PRIMARY KEY,
                                      order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
                                      status VARCHAR(50) NOT NULL,
                                      previous_status VARCHAR(50),
                                      updated_by BIGINT REFERENCES users(id),
                                      notes TEXT,
                                      estimated_time_minutes INTEGER,
                                      timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      ip_address VARCHAR(45),
                                      device_info VARCHAR(255),
                                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_status_history_order ON order_status_history(order_id);
CREATE INDEX idx_status_history_timestamp ON order_status_history(timestamp DESC);
CREATE INDEX idx_status_history_status ON order_status_history(status);

COMMENT ON TABLE order_status_history IS 'Complete audit trail of order status changes';

-- Kitchen Order Items
CREATE TABLE kitchen_order_items (
                                     id BIGSERIAL PRIMARY KEY,
                                     order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
                                     order_item_id BIGINT NOT NULL REFERENCES order_items(id) ON DELETE CASCADE,
                                     assigned_to BIGINT REFERENCES users(id),
                                     status VARCHAR(50) NOT NULL DEFAULT 'NOT_STARTED',
                                     started_at TIMESTAMP,
                                     completed_at TIMESTAMP,
                                     preparation_notes TEXT,
                                     priority INTEGER NOT NULL DEFAULT 0,
                                     station VARCHAR(100),
                                     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                     CONSTRAINT chk_kitchen_item_status CHECK (
                                         status IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED', 'ON_HOLD', 'CANCELLED')
                                         )
);

CREATE INDEX idx_kitchen_items_order ON kitchen_order_items(order_id);
CREATE INDEX idx_kitchen_items_status ON kitchen_order_items(status);
CREATE INDEX idx_kitchen_items_assigned ON kitchen_order_items(assigned_to);
CREATE INDEX idx_kitchen_items_station ON kitchen_order_items(station);

COMMENT ON TABLE kitchen_order_items IS 'Individual item tracking for kitchen workflow';

-- Delivery Assignments
CREATE TABLE delivery_assignments (
                                      id BIGSERIAL PRIMARY KEY,
                                      order_id BIGINT NOT NULL UNIQUE REFERENCES orders(id) ON DELETE CASCADE,
                                      delivery_partner_id BIGINT NOT NULL REFERENCES users(id),
                                      status VARCHAR(50) NOT NULL DEFAULT 'ASSIGNED',
                                      assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      accepted_at TIMESTAMP,
                                      picked_up_at TIMESTAMP,
                                      delivered_at TIMESTAMP,
                                      estimated_pickup_time TIMESTAMP,
                                      estimated_delivery_time TIMESTAMP,
                                      current_latitude DECIMAL(10, 8),
                                      current_longitude DECIMAL(11, 8),
                                      last_location_update TIMESTAMP,
                                      distance_remaining_km DECIMAL(8, 2),
                                      delivery_notes TEXT,
                                      rating INTEGER CHECK (rating >= 1 AND rating <= 5),
                                      customer_feedback TEXT,
                                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                      CONSTRAINT chk_delivery_status CHECK (
                                          status IN ('ASSIGNED', 'ACCEPTED', 'REJECTED', 'PICKED_UP',
                                                     'IN_TRANSIT', 'ARRIVED', 'DELIVERED', 'FAILED')
                                          )
);

CREATE INDEX idx_delivery_order ON delivery_assignments(order_id);
CREATE INDEX idx_delivery_partner ON delivery_assignments(delivery_partner_id);
CREATE INDEX idx_delivery_status ON delivery_assignments(status);
CREATE INDEX idx_delivery_assigned_at ON delivery_assignments(assigned_at);

COMMENT ON TABLE delivery_assignments IS 'Delivery partner assignments and tracking';

-- Order Timeline
CREATE TABLE order_timeline (
                                id BIGSERIAL PRIMARY KEY,
                                order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
                                event_type VARCHAR(50) NOT NULL,
                                title VARCHAR(100) NOT NULL,
                                description TEXT,
                                timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                icon VARCHAR(50),
                                is_milestone BOOLEAN NOT NULL DEFAULT FALSE,
                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                CONSTRAINT chk_event_type CHECK (
                                    event_type IN ('ORDER_PLACED', 'ORDER_CONFIRMED', 'PAYMENT_RECEIVED',
                                                   'KITCHEN_STARTED', 'FOOD_PREPARING', 'FOOD_READY',
                                                   'DELIVERY_ASSIGNED', 'DELIVERY_PICKED_UP', 'OUT_FOR_DELIVERY',
                                                   'DELIVERED', 'CANCELLED', 'ISSUE_REPORTED', 'REFUND_PROCESSED')
                                    )
);

CREATE INDEX idx_timeline_order ON order_timeline(order_id);
CREATE INDEX idx_timeline_timestamp ON order_timeline(timestamp DESC);
CREATE INDEX idx_timeline_event_type ON order_timeline(event_type);

COMMENT ON TABLE order_timeline IS 'Customer-facing order progress timeline';

-- Order Preparation Metrics
CREATE TABLE order_preparation_metrics (
                                           id BIGSERIAL PRIMARY KEY,
                                           order_id BIGINT NOT NULL UNIQUE REFERENCES orders(id) ON DELETE CASCADE,

    -- Timestamps
                                           confirmed_at TIMESTAMP,
                                           kitchen_started_at TIMESTAMP,
                                           kitchen_completed_at TIMESTAMP,
                                           ready_at TIMESTAMP,
                                           delivered_at TIMESTAMP,

    -- Duration calculations (in minutes)
                                           time_to_confirm INTEGER,
                                           time_to_start_preparing INTEGER,
                                           actual_preparation_time INTEGER,
                                           time_to_ready INTEGER,
                                           total_time_to_delivery INTEGER,

    -- Target vs Actual
                                           target_preparation_time INTEGER,
                                           was_on_time BOOLEAN NOT NULL DEFAULT TRUE,
                                           delay_minutes INTEGER,

    -- Kitchen performance
                                           total_items INTEGER,
                                           items_completed_on_time INTEGER,
                                           complexity_score INTEGER,

                                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                           updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_metrics_order ON order_preparation_metrics(order_id);
CREATE INDEX idx_metrics_performance ON order_preparation_metrics(was_on_time, delay_minutes);
CREATE INDEX idx_metrics_confirmed_at ON order_preparation_metrics(confirmed_at);

COMMENT ON TABLE order_preparation_metrics IS 'Kitchen performance and timing analytics';

-- Add new columns to existing orders table
ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS estimated_preparation_time_minutes INTEGER,
    ADD COLUMN IF NOT EXISTS estimated_ready_time TIMESTAMP,
    ADD COLUMN IF NOT EXISTS actual_ready_time TIMESTAMP,
    ADD COLUMN IF NOT EXISTS priority INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS kitchen_notes TEXT,
    ADD COLUMN IF NOT EXISTS is_rush_order BOOLEAN NOT NULL DEFAULT FALSE;

-- Update existing orders to have default priority
UPDATE orders SET priority = 0 WHERE priority IS NULL;
UPDATE orders SET is_rush_order = FALSE WHERE is_rush_order IS NULL;

-- Trigger: Auto-update order metrics on status change
CREATE OR REPLACE FUNCTION update_order_metrics()
    RETURNS TRIGGER AS $$
BEGIN
    -- Auto-create metrics record if not exists
    IF NOT EXISTS (SELECT 1 FROM order_preparation_metrics WHERE order_id = NEW.id) THEN
        INSERT INTO order_preparation_metrics (
            order_id,
            target_preparation_time,
            total_items
        )
        VALUES (
                   NEW.id,
                   NEW.estimated_preparation_time_minutes,
                   (SELECT COUNT(*) FROM order_items WHERE order_id = NEW.id)
               );
    END IF;

    -- Update timestamps based on status changes
    IF NEW.status = 'CONFIRMED' AND OLD.status != 'CONFIRMED' THEN
        UPDATE order_preparation_metrics
        SET confirmed_at = CURRENT_TIMESTAMP,
            updated_at = CURRENT_TIMESTAMP
        WHERE order_id = NEW.id AND confirmed_at IS NULL;

    ELSIF NEW.status = 'PREPARING' AND OLD.status != 'PREPARING' THEN
        UPDATE order_preparation_metrics
        SET kitchen_started_at = CURRENT_TIMESTAMP,
            updated_at = CURRENT_TIMESTAMP
        WHERE order_id = NEW.id AND kitchen_started_at IS NULL;

    ELSIF NEW.status = 'READY' AND OLD.status != 'READY' THEN
        UPDATE order_preparation_metrics
        SET kitchen_completed_at = CURRENT_TIMESTAMP,
            ready_at = CURRENT_TIMESTAMP,
            updated_at = CURRENT_TIMESTAMP
        WHERE order_id = NEW.id AND ready_at IS NULL;

    ELSIF NEW.status = 'DELIVERED' AND OLD.status != 'DELIVERED' THEN
        UPDATE order_preparation_metrics
        SET delivered_at = CURRENT_TIMESTAMP,
            updated_at = CURRENT_TIMESTAMP
        WHERE order_id = NEW.id AND delivered_at IS NULL;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_order_metrics
    AFTER UPDATE ON orders
    FOR EACH ROW
    WHEN (OLD.status IS DISTINCT FROM NEW.status)
EXECUTE FUNCTION update_order_metrics();

-- Trigger: Calculate preparation times
CREATE OR REPLACE FUNCTION calculate_preparation_times()
    RETURNS TRIGGER AS $$
BEGIN
    -- Calculate time to ready
    IF NEW.ready_at IS NOT NULL AND NEW.confirmed_at IS NOT NULL THEN
        NEW.time_to_ready = EXTRACT(EPOCH FROM (NEW.ready_at - NEW.confirmed_at)) / 60;
    END IF;

    -- Calculate time to start preparing
    IF NEW.kitchen_started_at IS NOT NULL AND NEW.confirmed_at IS NOT NULL THEN
        NEW.time_to_start_preparing = EXTRACT(EPOCH FROM (NEW.kitchen_started_at - NEW.confirmed_at)) / 60;
    END IF;

    -- Calculate actual preparation time
    IF NEW.kitchen_completed_at IS NOT NULL AND NEW.kitchen_started_at IS NOT NULL THEN
        NEW.actual_preparation_time = EXTRACT(EPOCH FROM (NEW.kitchen_completed_at - NEW.kitchen_started_at)) / 60;
    END IF;

    -- Calculate total time to delivery
    IF NEW.delivered_at IS NOT NULL AND NEW.confirmed_at IS NOT NULL THEN
        NEW.total_time_to_delivery = EXTRACT(EPOCH FROM (NEW.delivered_at - NEW.confirmed_at)) / 60;
    END IF;

    -- Check if on time
    IF NEW.target_preparation_time IS NOT NULL AND NEW.actual_preparation_time IS NOT NULL THEN
        IF NEW.actual_preparation_time > NEW.target_preparation_time THEN
            NEW.was_on_time = FALSE;
            NEW.delay_minutes = NEW.actual_preparation_time - NEW.target_preparation_time;
        ELSE
            NEW.was_on_time = TRUE;
            NEW.delay_minutes = 0;
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_calculate_times
    BEFORE UPDATE ON order_preparation_metrics
    FOR EACH ROW
EXECUTE FUNCTION calculate_preparation_times();

-- Trigger: Auto-update kitchen item counts
CREATE OR REPLACE FUNCTION update_kitchen_item_counts()
    RETURNS TRIGGER AS $$
BEGIN
    UPDATE order_preparation_metrics
    SET items_completed_on_time = (
        SELECT COUNT(*)
        FROM kitchen_order_items
        WHERE order_id = NEW.order_id
          AND status = 'COMPLETED'
    ),
        updated_at = CURRENT_TIMESTAMP
    WHERE order_id = NEW.order_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_kitchen_items
    AFTER INSERT OR UPDATE ON kitchen_order_items
    FOR EACH ROW
EXECUTE FUNCTION update_kitchen_item_counts();

-- Views for analytics and reporting

-- Active kitchen orders view
CREATE OR REPLACE VIEW active_kitchen_orders AS
SELECT
    o.id,
    o.order_number,
    o.order_type,
    o.table_number,
    o.status,
    o.priority,
    o.is_rush_order,
    o.created_at as order_time,
    o.estimated_ready_time,
    EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - o.created_at)) / 60 as elapsed_minutes,
    u.full_name as customer_name,
    o.restaurant_id,
    COUNT(koi.id) as total_items,
    SUM(CASE WHEN koi.status = 'COMPLETED' THEN 1 ELSE 0 END) as completed_items,
    SUM(CASE WHEN koi.status = 'IN_PROGRESS' THEN 1 ELSE 0 END) as in_progress_items
FROM orders o
         LEFT JOIN users u ON o.customer_id = u.id
         LEFT JOIN kitchen_order_items koi ON o.id = koi.order_id
WHERE o.status IN ('CONFIRMED', 'PREPARING')
GROUP BY o.id, u.full_name
ORDER BY o.priority DESC, o.created_at ASC;

COMMENT ON VIEW active_kitchen_orders IS 'Real-time view of active orders in kitchen';

-- Kitchen performance daily view
CREATE OR REPLACE VIEW kitchen_performance_daily AS
SELECT
    DATE(confirmed_at) as date,
    COUNT(*) as total_orders,
    AVG(actual_preparation_time) as avg_preparation_time,
    PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY actual_preparation_time) as median_preparation_time,
    SUM(CASE WHEN was_on_time THEN 1 ELSE 0 END) as orders_on_time,
    SUM(CASE WHEN was_on_time = FALSE THEN 1 ELSE 0 END) as orders_delayed,
    ROUND(SUM(CASE WHEN was_on_time THEN 1 ELSE 0 END)::DECIMAL / NULLIF(COUNT(*), 0) * 100, 2) as on_time_percentage,
    AVG(delay_minutes) FILTER (WHERE was_on_time = FALSE) as avg_delay_minutes
FROM order_preparation_metrics
WHERE confirmed_at IS NOT NULL
GROUP BY DATE(confirmed_at)
ORDER BY date DESC;

COMMENT ON VIEW kitchen_performance_daily IS 'Daily kitchen performance metrics';

-- Active deliveries view
CREATE OR REPLACE VIEW active_deliveries AS
SELECT
    da.id as assignment_id,
    o.id as order_id,
    o.order_number,
    da.status,
    u.full_name as delivery_partner_name,
    u.phone as delivery_partner_phone,
    da.current_latitude,
    da.current_longitude,
    da.distance_remaining_km,
    da.estimated_delivery_time,
    EXTRACT(EPOCH FROM (da.estimated_delivery_time - CURRENT_TIMESTAMP)) / 60 as eta_minutes,
    o.delivery_address,
    o.customer_phone,
    da.assigned_at,
    da.picked_up_at,
    o.restaurant_id
FROM delivery_assignments da
         JOIN orders o ON da.order_id = o.id
         JOIN users u ON da.delivery_partner_id = u.id
WHERE da.status IN ('ASSIGNED', 'ACCEPTED', 'PICKED_UP', 'IN_TRANSIT')
ORDER BY da.assigned_at ASC;

COMMENT ON VIEW active_deliveries IS 'Real-time view of active deliveries';

-- Delivery partner performance view
CREATE OR REPLACE VIEW delivery_partner_performance AS
SELECT
    u.id as partner_id,
    u.full_name as partner_name,
    COUNT(da.id) as total_deliveries,
    SUM(CASE WHEN da.status = 'DELIVERED' THEN 1 ELSE 0 END) as completed_deliveries,
    SUM(CASE WHEN da.status = 'FAILED' THEN 1 ELSE 0 END) as failed_deliveries,
    AVG(da.rating) FILTER (WHERE da.rating IS NOT NULL) as avg_rating,
    AVG(EXTRACT(EPOCH FROM (da.delivered_at - da.assigned_at)) / 60)
    FILTER (WHERE da.delivered_at IS NOT NULL) as avg_delivery_time_minutes
FROM users u
         JOIN delivery_assignments da ON u.id = da.delivery_partner_id
WHERE EXISTS (
    SELECT 1 FROM user_roles ur
                      JOIN roles r ON ur.role_id = r.id
    WHERE ur.user_id = u.id AND r.name = 'ROLE_DELIVERY_MAN'
)
GROUP BY u.id, u.full_name
ORDER BY completed_deliveries DESC;

COMMENT ON VIEW delivery_partner_performance IS 'Delivery partner performance metrics';

-- Grant permissions
GRANT SELECT ON active_kitchen_orders TO PUBLIC;
GRANT SELECT ON kitchen_performance_daily TO PUBLIC;
GRANT SELECT ON active_deliveries TO PUBLIC;
GRANT SELECT ON delivery_partner_performance TO PUBLIC;