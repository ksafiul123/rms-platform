DROP TABLE IF EXISTS menu_items CASCADE;

CREATE TABLE menu_items (
                            id BIGSERIAL PRIMARY KEY,
                            restaurant_id BIGINT NOT NULL,
                            category_id BIGINT NOT NULL,

    -- Item details
                            name VARCHAR(200) NOT NULL,
                            description TEXT,
                            sku VARCHAR(50),

    -- Pricing
                            base_price DECIMAL(10,2) NOT NULL,
                            discounted_price DECIMAL(10,2),

    -- Media
                            image_url VARCHAR(500),
                            additional_images TEXT, -- JSON array

    -- Attributes
                            preparation_time_minutes INTEGER,
                            item_type VARCHAR(50),

    -- Dietary info
                            is_vegetarian BOOLEAN NOT NULL DEFAULT false,
                            is_vegan BOOLEAN NOT NULL DEFAULT false,
                            is_gluten_free BOOLEAN NOT NULL DEFAULT false,
                            is_spicy BOOLEAN NOT NULL DEFAULT false,
                            spice_level INTEGER DEFAULT 0,
                            calories INTEGER,
                            allergen_info VARCHAR(500),

    -- Availability
                            is_available BOOLEAN NOT NULL DEFAULT true,
                            is_active BOOLEAN NOT NULL DEFAULT true,
                            available_from TIME,
                            available_to TIME,
                            available_for_dine_in BOOLEAN NOT NULL DEFAULT true,
                            available_for_takeaway BOOLEAN NOT NULL DEFAULT true,
                            available_for_delivery BOOLEAN NOT NULL DEFAULT true,

    -- Inventory
                            stock_quantity INTEGER,
                            low_stock_threshold INTEGER,

    -- SEO & Display
                            display_order INTEGER NOT NULL DEFAULT 0,
                            is_featured BOOLEAN NOT NULL DEFAULT false,
                            is_best_seller BOOLEAN NOT NULL DEFAULT false,

    -- Audit
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            deleted_at TIMESTAMP,

    -- Constraints
                            CONSTRAINT menu_items_restaurant_fk FOREIGN KEY (restaurant_id)
                                REFERENCES restaurants(id) ON DELETE CASCADE,
                            CONSTRAINT menu_items_category_fk FOREIGN KEY (category_id)
                                REFERENCES menu_categories(id) ON DELETE RESTRICT,
                            CONSTRAINT menu_items_price_positive
                                CHECK (base_price >= 0),
                            CONSTRAINT menu_items_discounted_price_valid
                                CHECK (discounted_price IS NULL OR discounted_price < base_price),
                            CONSTRAINT menu_items_spice_level_valid
                                CHECK (spice_level BETWEEN 0 AND 5),
                            CONSTRAINT menu_items_stock_positive
                                CHECK (stock_quantity IS NULL OR stock_quantity >= 0)
);

-- Performance indexes
CREATE INDEX CONCURRENTLY idx_menu_items_restaurant_active
    ON menu_items(restaurant_id, is_active, display_order)
    WHERE deleted_at IS NULL;

CREATE INDEX CONCURRENTLY idx_menu_items_category
    ON menu_items(category_id, display_order)
    WHERE is_active = true AND deleted_at IS NULL;

CREATE INDEX CONCURRENTLY idx_menu_items_featured
    ON menu_items(restaurant_id)
    WHERE is_featured = true AND is_active = true AND deleted_at IS NULL;

CREATE INDEX CONCURRENTLY idx_menu_items_available
    ON menu_items(restaurant_id, is_available, is_active)
    WHERE deleted_at IS NULL;

-- Partial index for low stock
CREATE INDEX CONCURRENTLY idx_menu_items_low_stock
    ON menu_items(restaurant_id, stock_quantity)
    WHERE stock_quantity IS NOT NULL
        AND low_stock_threshold IS NOT NULL
        AND stock_quantity <= low_stock_threshold;

-- GIN index for full-text search
CREATE INDEX CONCURRENTLY idx_menu_items_search
    ON menu_items USING gin(
                            to_tsvector('english', name || ' ' || COALESCE(description, ''))
        ) WHERE is_active = true AND deleted_at IS NULL;

-- Expression index for availability check
CREATE INDEX CONCURRENTLY idx_menu_items_time_available
    ON menu_items(restaurant_id, available_from, available_to)
    WHERE is_available = true AND is_active = true;

ALTER TABLE menu_items SET (
    autovacuum_vacuum_scale_factor = 0.1,
    fillfactor = 90
    );

COMMENT ON TABLE menu_items IS 'Restaurant menu items - heavily queried by customers';