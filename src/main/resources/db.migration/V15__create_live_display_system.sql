-- V15__create_live_display_system.sql

-- Display Configurations table
CREATE TABLE display_configurations (
                                        id BIGSERIAL PRIMARY KEY,
                                        restaurant_id BIGINT NOT NULL UNIQUE REFERENCES restaurants(id) ON DELETE CASCADE,
                                        display_mode VARCHAR(30) NOT NULL DEFAULT 'ORDER_NUMBER',
                                        theme VARCHAR(30) NOT NULL DEFAULT 'LIGHT',
                                        refresh_interval_seconds INTEGER NOT NULL DEFAULT 5,
                                        show_preparing BOOLEAN NOT NULL DEFAULT TRUE,
                                        show_ready BOOLEAN NOT NULL DEFAULT TRUE,
                                        show_completed BOOLEAN NOT NULL DEFAULT FALSE,
                                        max_orders_display INTEGER NOT NULL DEFAULT 20,
                                        show_order_items BOOLEAN NOT NULL DEFAULT FALSE,
                                        show_estimated_time BOOLEAN NOT NULL DEFAULT TRUE,
                                        show_elapsed_time BOOLEAN NOT NULL DEFAULT FALSE,
                                        play_sound_on_ready BOOLEAN NOT NULL DEFAULT TRUE,
                                        sound_notification_url VARCHAR(500),
                                        highlight_ready_duration_seconds INTEGER DEFAULT 30,
                                        logo_url VARCHAR(500),
                                        background_image_url VARCHAR(500),
                                        primary_color VARCHAR(7) DEFAULT '#007bff',
                                        secondary_color VARCHAR(7) DEFAULT '#6c757d',
                                        ready_color VARCHAR(7) DEFAULT '#28a745',
                                        preparing_color VARCHAR(7) DEFAULT '#ffc107',
                                        font_family VARCHAR(100) DEFAULT 'Arial, sans-serif',
                                        header_text VARCHAR(200),
                                        footer_text VARCHAR(200),
                                        language VARCHAR(5) DEFAULT 'en',
                                        is_active BOOLEAN NOT NULL DEFAULT TRUE,
                                        display_token VARCHAR(100) NOT NULL UNIQUE,
                                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                        CONSTRAINT chk_display_mode CHECK (display_mode IN (
                                                                                            'ORDER_NUMBER', 'TABLE_NUMBER', 'DETAILED_ITEMS',
                                                                                            'TIMELINE', 'GRID_LAYOUT', 'CAROUSEL'
                                            )),
                                        CONSTRAINT chk_theme CHECK (theme IN (
                                                                              'LIGHT', 'DARK', 'HIGH_CONTRAST', 'CUSTOM'
                                            ))
);

CREATE INDEX idx_display_restaurant ON display_configurations(restaurant_id);
CREATE INDEX idx_display_token ON display_configurations(display_token);
CREATE INDEX idx_display_active ON display_configurations(is_active);

-- Order Display Snapshots table
CREATE TABLE order_display_snapshots (
                                         id BIGSERIAL PRIMARY KEY,
                                         restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
                                         order_id BIGINT NOT NULL UNIQUE REFERENCES orders(id) ON DELETE CASCADE,
                                         order_number VARCHAR(50) NOT NULL,
                                         display_number VARCHAR(20),
                                         table_number VARCHAR(20),
                                         order_type VARCHAR(30) NOT NULL,
                                         display_status VARCHAR(30) NOT NULL,
                                         customer_name VARCHAR(100),
                                         total_items INTEGER NOT NULL,
                                         items_completed INTEGER NOT NULL DEFAULT 0,
                                         estimated_ready_time TIMESTAMP,
                                         actual_ready_time TIMESTAMP,
                                         elapsed_minutes INTEGER,
                                         remaining_minutes INTEGER,
                                         priority INTEGER NOT NULL DEFAULT 0,
                                         is_highlighted BOOLEAN NOT NULL DEFAULT FALSE,
                                         highlighted_at TIMESTAMP,
                                         display_position INTEGER,
                                         last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         items_json TEXT,
                                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                         CONSTRAINT chk_display_status CHECK (display_status IN (
                                                                                                 'PREPARING', 'READY', 'CALLED', 'COLLECTED', 'HIDDEN'
                                             ))
);

CREATE INDEX idx_snapshot_restaurant ON order_display_snapshots(restaurant_id);
CREATE INDEX idx_snapshot_order ON order_display_snapshots(order_id);
CREATE INDEX idx_snapshot_status ON order_display_snapshots(display_status);
CREATE INDEX idx_snapshot_updated ON order_display_snapshots(last_updated);
CREATE INDEX idx_snapshot_highlighted ON order_display_snapshots(is_highlighted, highlighted_at);

-- Display Analytics table
CREATE TABLE display_analytics (
                                   id BIGSERIAL PRIMARY KEY,
                                   restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
                                   analytics_date DATE NOT NULL,
                                   total_orders_displayed INTEGER NOT NULL DEFAULT 0,
                                   avg_display_duration_minutes INTEGER,
                                   avg_time_to_ready_minutes INTEGER,
                                   avg_time_to_collect_minutes INTEGER,
                                   peak_concurrent_orders INTEGER,
                                   total_display_views BIGINT NOT NULL DEFAULT 0,
                                   unique_display_sessions INTEGER,
                                   avg_refresh_rate_seconds INTEGER,
                                   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                   UNIQUE(restaurant_id, analytics_date)
);

CREATE INDEX idx_analytics_restaurant ON display_analytics(restaurant_id);
CREATE INDEX idx_analytics_date ON display_analytics(analytics_date);

-- Function to generate display token
CREATE OR REPLACE FUNCTION generate_display_token()
    RETURNS TEXT AS $$
BEGIN
    RETURN 'DISP-' || UPPER(SUBSTRING(MD5(RANDOM()::TEXT || CURRENT_TIMESTAMP::TEXT) FROM 1 FOR 16));
END;
$$ LANGUAGE plpgsql;

-- Trigger to auto-generate display token
CREATE OR REPLACE FUNCTION set_display_token()
    RETURNS TRIGGER AS $$
BEGIN
    IF NEW.display_token IS NULL OR NEW.display_token = '' THEN
        NEW.display_token := generate_display_token();
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_set_display_token
    BEFORE INSERT ON display_configurations
    FOR EACH ROW
EXECUTE FUNCTION set_display_token();

-- Function to auto-create display config for new restaurants
CREATE OR REPLACE FUNCTION create_default_display_config()
    RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO display_configurations (
        restaurant_id,
        display_mode,
        theme,
        header_text,
        footer_text,
        is_active
    ) VALUES (
                 NEW.id,
                 'ORDER_NUMBER',
                 'LIGHT',
                 'Your order is being prepared',
                 'Thank you for your patience',
                 TRUE
             );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_create_display_config
    AFTER INSERT ON restaurants
    FOR EACH ROW
EXECUTE FUNCTION create_default_display_config();

-- Function to update snapshot on order status change
CREATE OR REPLACE FUNCTION update_display_snapshot()
    RETURNS TRIGGER AS $$
DECLARE
    display_stat VARCHAR(30);
    disp_number VARCHAR(20);
    elapsed_min INTEGER;
    remaining_min INTEGER;
BEGIN
    -- Map order status to display status
    display_stat := CASE NEW.status
                        WHEN 'CONFIRMED' THEN 'PREPARING'
                        WHEN 'PREPARING' THEN 'PREPARING'
                        WHEN 'READY' THEN 'READY'
                        WHEN 'DELIVERED' THEN 'COLLECTED'
                        WHEN 'COMPLETED' THEN 'COLLECTED'
                        WHEN 'CANCELLED' THEN 'HIDDEN'
                        ELSE 'HIDDEN'
        END;

    -- Extract display number (last 3 digits)
    disp_number := SUBSTRING(REGEXP_REPLACE(NEW.order_number, '\D', '', 'g') FROM '.{3}$');

    -- Calculate elapsed minutes
    elapsed_min := EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - NEW.created_at)) / 60;

    -- Calculate remaining minutes
    IF NEW.estimated_ready_time IS NOT NULL THEN
        remaining_min := GREATEST(0, EXTRACT(EPOCH FROM (NEW.estimated_ready_time - CURRENT_TIMESTAMP)) / 60);
    ELSE
        remaining_min := NULL;
    END IF;

    -- Insert or update snapshot
    INSERT INTO order_display_snapshots (
        restaurant_id,
        order_id,
        order_number,
        display_number,
        table_number,
        order_type,
        display_status,
        total_items,
        items_completed,
        estimated_ready_time,
        actual_ready_time,
        elapsed_minutes,
        remaining_minutes,
        priority,
        is_highlighted,
        last_updated
    ) VALUES (
                 NEW.restaurant_id,
                 NEW.id,
                 NEW.order_number,
                 disp_number,
                 NEW.table_number,
                 NEW.order_type,
                 display_stat,
                 (SELECT COUNT(*) FROM order_items WHERE order_id = NEW.id),
                 0,
                 NEW.estimated_ready_time,
                 NEW.actual_ready_time,
                 elapsed_min,
                 remaining_min,
                 NEW.priority,
                 CASE WHEN NEW.status = 'READY' AND OLD.status != 'READY' THEN TRUE ELSE FALSE END,
                 CURRENT_TIMESTAMP
             )
    ON CONFLICT (order_id) DO UPDATE SET
                                         order_number = EXCLUDED.order_number,
                                         display_number = EXCLUDED.display_number,
                                         table_number = EXCLUDED.table_number,
                                         order_type = EXCLUDED.order_type,
                                         display_status = EXCLUDED.display_status,
                                         estimated_ready_time = EXCLUDED.estimated_ready_time,
                                         actual_ready_time = EXCLUDED.actual_ready_time,
                                         elapsed_minutes = EXCLUDED.elapsed_minutes,
                                         remaining_minutes = EXCLUDED.remaining_minutes,
                                         priority = EXCLUDED.priority,
                                         is_highlighted = CASE
                                                              WHEN EXCLUDED.display_status = 'READY' AND order_display_snapshots.display_status != 'READY'
                                                                  THEN TRUE
                                                              ELSE order_display_snapshots.is_highlighted
                                             END,
                                         highlighted_at = CASE
                                                              WHEN EXCLUDED.display_status = 'READY' AND order_display_snapshots.display_status != 'READY'
                                                                  THEN CURRENT_TIMESTAMP
                                                              ELSE order_display_snapshots.highlighted_at
                                             END,
                                         last_updated = CURRENT_TIMESTAMP,
                                         updated_at = CURRENT_TIMESTAMP;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_display_snapshot
    AFTER INSERT OR UPDATE ON orders
    FOR EACH ROW
EXECUTE FUNCTION update_display_snapshot();

-- Function to expire highlights
CREATE OR REPLACE FUNCTION expire_old_highlights()
    RETURNS INTEGER AS $$
DECLARE
    expired_count INTEGER;
BEGIN
    UPDATE order_display_snapshots
    SET is_highlighted = FALSE
    WHERE is_highlighted = TRUE
      AND highlighted_at < CURRENT_TIMESTAMP - INTERVAL '30 seconds';

    GET DIAGNOSTICS expired_count = ROW_COUNT;
    RETURN expired_count;
END;
$$ LANGUAGE plpgsql;

-- Scheduled job to expire highlights (call this from application scheduler)
-- SELECT expire_old_highlights();

-- Function to cleanup old snapshots
CREATE OR REPLACE FUNCTION cleanup_old_snapshots()
    RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM order_display_snapshots
    WHERE display_status = 'COLLECTED'
      AND last_updated < CURRENT_TIMESTAMP - INTERVAL '1 hour';

    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

-- Views for analytics

CREATE OR REPLACE VIEW display_dashboard AS
SELECT
    r.id as restaurant_id,
    r.name as restaurant_name,
    dc.display_token,
    dc.is_active,
    COUNT(CASE WHEN s.display_status = 'PREPARING' THEN 1 END) as preparing_count,
    COUNT(CASE WHEN s.display_status = 'READY' THEN 1 END) as ready_count,
    COUNT(CASE WHEN s.display_status = 'CALLED' THEN 1 END) as called_count,
    AVG(s.elapsed_minutes) as avg_elapsed_minutes,
    AVG(s.remaining_minutes) as avg_remaining_minutes,
    MAX(s.elapsed_minutes) as max_wait_time
FROM restaurants r
         LEFT JOIN display_configurations dc ON r.id = dc.restaurant_id
         LEFT JOIN order_display_snapshots s ON r.id = s.restaurant_id
    AND s.display_status IN ('PREPARING', 'READY', 'CALLED')
GROUP BY r.id, r.name, dc.display_token, dc.is_active;

CREATE OR REPLACE VIEW display_performance_metrics AS
SELECT
    r.id as restaurant_id,
    r.name as restaurant_name,
    DATE(s.created_at) as date,
    COUNT(*) as total_orders,
    AVG(s.elapsed_minutes) as avg_preparation_time,
    AVG(CASE
            WHEN s.actual_ready_time IS NOT NULL
                THEN EXTRACT(EPOCH FROM (s.actual_ready_time - s.created_at)) / 60
        END) as avg_time_to_ready,
    MAX(s.elapsed_minutes) as peak_wait_time,
    COUNT(CASE WHEN s.is_highlighted THEN 1 END) as highlighted_orders
FROM order_display_snapshots s
         JOIN restaurants r ON s.restaurant_id = r.id
WHERE s.created_at >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY r.id, r.name, DATE(s.created_at);

-- Seed default display configurations for existing restaurants
INSERT INTO display_configurations (
    restaurant_id,
    display_mode,
    theme,
    header_text,
    footer_text,
    is_active
)
SELECT
    id,
    'ORDER_NUMBER',
    'LIGHT',
    'Your order is being prepared',
    'Thank you for your patience',
    TRUE
FROM restaurants
WHERE NOT EXISTS (
    SELECT 1 FROM display_configurations dc
    WHERE dc.restaurant_id = restaurants.id
);

COMMENT ON TABLE display_configurations IS 'Display configuration per restaurant for public order monitors';
COMMENT ON TABLE order_display_snapshots IS 'Cached order data optimized for display performance';
COMMENT ON TABLE display_analytics IS 'Analytics tracking for display usage and performance';