DROP TABLE IF EXISTS order_items CASCADE;

CREATE TABLE order_items (
                             id BIGSERIAL PRIMARY KEY,
                             order_id BIGINT NOT NULL,
                             menu_item_id BIGINT NOT NULL,
                             variant_id BIGINT,

    -- Pricing snapshot (at time of order)
                             item_name VARCHAR(200) NOT NULL,
                             quantity INTEGER NOT NULL,
                             price DECIMAL(10,2) NOT NULL,
                             discount DECIMAL(10,2) NOT NULL DEFAULT 0,
                             subtotal DECIMAL(10,2) NOT NULL,

    -- Customization
                             special_instructions TEXT,

    -- Audit
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
                             CONSTRAINT order_items_order_fk FOREIGN KEY (order_id)
                                 REFERENCES orders(id) ON DELETE CASCADE,
                             CONSTRAINT order_items_menu_item_fk FOREIGN KEY (menu_item_id)
                                 REFERENCES menu_items(id) ON DELETE RESTRICT,
                             CONSTRAINT order_items_variant_fk FOREIGN KEY (variant_id)
                                 REFERENCES item_variants(id) ON DELETE RESTRICT,
                             CONSTRAINT order_items_quantity_positive CHECK (quantity > 0),
                             CONSTRAINT order_items_price_positive CHECK (price >= 0 AND subtotal >= 0)
);

-- Indexes
CREATE INDEX CONCURRENTLY idx_order_items_order
    ON order_items(order_id);

CREATE INDEX CONCURRENTLY idx_order_items_menu_item
    ON order_items(menu_item_id, created_at DESC);

-- For analytics: popular items
CREATE INDEX CONCURRENTLY idx_order_items_analytics
    ON order_items(menu_item_id, created_at)
    INCLUDE (quantity, subtotal);

-- Optimize for joins
ALTER TABLE order_items SET (
    autovacuum_vacuum_scale_factor = 0.05,
    fillfactor = 90  -- Leave room for updates
    );

COMMENT ON TABLE order_items IS 'Individual items within orders - frequently joined';