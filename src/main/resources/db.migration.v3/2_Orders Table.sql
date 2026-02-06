DROP TABLE IF EXISTS orders CASCADE;

CREATE TABLE orders (
                        id BIGSERIAL PRIMARY KEY,
                        order_number VARCHAR(50) NOT NULL,

    -- Foreign keys
                        restaurant_id BIGINT NOT NULL,
                        customer_id BIGINT NOT NULL,
                        table_session_id BIGINT,
                        assigned_waiter_id BIGINT,

    -- Order details
                        order_type VARCHAR(30) NOT NULL,
                        status VARCHAR(30) NOT NULL,
                        payment_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    -- Pricing
                        subtotal DECIMAL(10,2) NOT NULL,
                        tax_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
                        discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
                        delivery_fee DECIMAL(10,2) NOT NULL DEFAULT 0,
                        service_charge DECIMAL(10,2) NOT NULL DEFAULT 0,
                        tip_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
                        total_amount DECIMAL(10,2) NOT NULL,

    -- Delivery info
                        delivery_address TEXT,
                        delivery_latitude DECIMAL(10, 8),
                        delivery_longitude DECIMAL(11, 8),
                        delivery_instructions TEXT,
                        customer_phone VARCHAR(20),

    -- Table info
                        table_number VARCHAR(20),
                        guest_count INTEGER,

    -- Timing
                        estimated_preparation_time_minutes INTEGER,
                        estimated_ready_time TIMESTAMP,
                        actual_ready_time TIMESTAMP,
                        estimated_delivery_time TIMESTAMP,
                        actual_delivery_time TIMESTAMP,

    -- Priority
                        priority INTEGER NOT NULL DEFAULT 0,
                        is_rush_order BOOLEAN NOT NULL DEFAULT false,

    -- Notes
                        special_instructions TEXT,
                        kitchen_notes TEXT,
                        cancellation_reason TEXT,

    -- Audit
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        confirmed_at TIMESTAMP,
                        cancelled_at TIMESTAMP,
                        completed_at TIMESTAMP,

    -- Constraints
                        CONSTRAINT orders_order_number_unique UNIQUE (order_number),
                        CONSTRAINT orders_restaurant_fk FOREIGN KEY (restaurant_id)
                            REFERENCES restaurants(id) ON DELETE RESTRICT,
                        CONSTRAINT orders_customer_fk FOREIGN KEY (customer_id)
                            REFERENCES users(id) ON DELETE RESTRICT,
                        CONSTRAINT orders_table_session_fk FOREIGN KEY (table_session_id)
                            REFERENCES table_sessions(id) ON DELETE SET NULL,
                        CONSTRAINT orders_waiter_fk FOREIGN KEY (assigned_waiter_id)
                            REFERENCES users(id) ON DELETE SET NULL,
                        CONSTRAINT orders_order_type_check
                            CHECK (order_type IN ('DINE_IN', 'TAKEAWAY', 'DELIVERY')),
                        CONSTRAINT orders_status_check
                            CHECK (status IN ('PENDING', 'CONFIRMED', 'PREPARING', 'READY',
                                              'OUT_FOR_DELIVERY', 'DELIVERED', 'COMPLETED', 'CANCELLED', 'REJECTED')),
                        CONSTRAINT orders_payment_status_check
                            CHECK (payment_status IN ('PENDING', 'PAID', 'FAILED', 'REFUNDED', 'PARTIALLY_REFUNDED')),
                        CONSTRAINT orders_amounts_positive
                            CHECK (subtotal >= 0 AND total_amount >= 0 AND tax_amount >= 0),
                        CONSTRAINT orders_guest_count_positive
                            CHECK (guest_count IS NULL OR guest_count > 0)
);

-- Critical Performance Indexes
CREATE INDEX CONCURRENTLY idx_orders_restaurant_status
    ON orders(restaurant_id, status, created_at DESC);

CREATE INDEX CONCURRENTLY idx_orders_customer
    ON orders(customer_id, created_at DESC);

CREATE INDEX CONCURRENTLY idx_orders_status_created
    ON orders(status, created_at DESC);

CREATE INDEX CONCURRENTLY idx_orders_order_number
    ON orders(order_number);

-- Partial indexes for active orders (most frequently queried)
CREATE INDEX CONCURRENTLY idx_orders_active_kitchen
    ON orders(restaurant_id, priority DESC, created_at)
    WHERE status IN ('CONFIRMED', 'PREPARING') AND completed_at IS NULL;

CREATE INDEX CONCURRENTLY idx_orders_pending_payment
    ON orders(restaurant_id, created_at DESC)
    WHERE payment_status = 'PENDING' AND completed_at IS NULL;

CREATE INDEX CONCURRENTLY idx_orders_delivery_pending
    ON orders(restaurant_id, created_at)
    WHERE order_type = 'DELIVERY' AND status IN ('READY', 'OUT_FOR_DELIVERY');

-- Composite index for analytics queries
CREATE INDEX CONCURRENTLY idx_orders_analytics
    ON orders(restaurant_id, created_at, status, total_amount)
    WHERE completed_at IS NOT NULL;

-- Index for time-based queries
CREATE INDEX CONCURRENTLY idx_orders_created_at_brin
    ON orders USING brin(created_at)
    WITH (pages_per_range = 128);

-- Table partitioning by date (for high-volume restaurants)
-- This is optional but recommended for 100k+ orders/month
CREATE TABLE orders_2024_01 PARTITION OF orders
    FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');
CREATE TABLE orders_2024_02 PARTITION OF orders
    FOR VALUES FROM ('2024-02-01') TO ('2024-03-01');
-- ... continue for each month

-- Optimize autovacuum for high-traffic table
ALTER TABLE orders SET (
    autovacuum_vacuum_scale_factor = 0.05,
    autovacuum_analyze_scale_factor = 0.02,
    autovacuum_vacuum_cost_delay = 10,
    toast_tuple_target = 8160
    );

COMMENT ON TABLE orders IS 'Core order transactions - high volume, heavily indexed';
COMMENT ON INDEX idx_orders_active_kitchen IS 'Critical for kitchen display queries';