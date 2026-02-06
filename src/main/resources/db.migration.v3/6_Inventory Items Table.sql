DROP TABLE IF EXISTS inventory_items CASCADE;

CREATE TABLE inventory_items (
                                 id BIGSERIAL PRIMARY KEY,
                                 restaurant_id BIGINT NOT NULL,

    -- Item details
                                 item_code VARCHAR(50) NOT NULL,
                                 name VARCHAR(200) NOT NULL,
                                 description TEXT,
                                 category VARCHAR(50) NOT NULL,

    -- Quantity tracking
                                 unit VARCHAR(20) NOT NULL,
                                 current_quantity DECIMAL(10,3) NOT NULL DEFAULT 0,
                                 minimum_quantity DECIMAL(10,3) NOT NULL,
                                 maximum_quantity DECIMAL(10,3),
                                 reorder_point DECIMAL(10,3),
                                 reorder_quantity DECIMAL(10,3),

    -- Pricing
                                 cost_per_unit DECIMAL(10,2) NOT NULL,
                                 last_purchase_price DECIMAL(10,2),
                                 average_cost DECIMAL(10,2),

    -- Supplier info
                                 supplier_name VARCHAR(200),
                                 supplier_contact VARCHAR(100),
                                 supplier_email VARCHAR(100),

    -- Status
                                 status VARCHAR(30) NOT NULL,

    -- Storage
                                 storage_location VARCHAR(100),
                                 expiry_date DATE,

    -- Audit
                                 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 last_restocked_at TIMESTAMP,
                                 deleted_at TIMESTAMP,

    -- Constraints
                                 CONSTRAINT inventory_items_restaurant_fk FOREIGN KEY (restaurant_id)
                                     REFERENCES restaurants(id) ON DELETE CASCADE,
                                 CONSTRAINT inventory_items_code_restaurant_unique
                                     UNIQUE (restaurant_id, item_code),
                                 CONSTRAINT inventory_items_category_check
                                     CHECK (category IN ('VEGETABLES', 'FRUITS', 'MEAT', 'SEAFOOD', 'DAIRY',
                                                         'GRAINS', 'SPICES', 'OILS', 'BEVERAGES', 'PACKAGING', 'OTHER')),
                                 CONSTRAINT inventory_items_status_check
                                     CHECK (status IN ('IN_STOCK', 'LOW_STOCK', 'OUT_OF_STOCK', 'DISCONTINUED')),
                                 CONSTRAINT inventory_items_quantities_positive
                                     CHECK (current_quantity >= 0 AND minimum_quantity >= 0),
                                 CONSTRAINT inventory_items_cost_positive
                                     CHECK (cost_per_unit >= 0)
);

-- Performance indexes
CREATE INDEX CONCURRENTLY idx_inventory_restaurant
    ON inventory_items(restaurant_id, status)
    WHERE deleted_at IS NULL;

CREATE INDEX CONCURRENTLY idx_inventory_code
    ON inventory_items(restaurant_id, item_code);

CREATE INDEX CONCURRENTLY idx_inventory_category
    ON inventory_items(restaurant_id, category)
    WHERE deleted_at IS NULL;

-- Critical: Low stock alerts
CREATE INDEX CONCURRENTLY idx_inventory_low_stock
    ON inventory_items(restaurant_id, current_quantity)
    WHERE current_quantity <= minimum_quantity AND deleted_at IS NULL;

-- Expiry tracking
CREATE INDEX CONCURRENTLY idx_inventory_expiring
    ON inventory_items(restaurant_id, expiry_date)
    WHERE expiry_date IS NOT NULL
        AND expiry_date > CURRENT_DATE
        AND expiry_date <= CURRENT_DATE + INTERVAL '7 days';

-- For reorder point calculation
CREATE INDEX CONCURRENTLY idx_inventory_reorder
    ON inventory_items(restaurant_id, current_quantity, reorder_point)
    WHERE reorder_point IS NOT NULL
        AND current_quantity <= reorder_point;

ALTER TABLE inventory_items SET (
    autovacuum_vacuum_scale_factor = 0.1
    );

COMMENT ON TABLE inventory_items IS 'Restaurant inventory management - critical for operations';