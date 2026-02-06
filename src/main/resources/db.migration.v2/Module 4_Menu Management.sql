-- =====================================================
-- MENU MODULE - OPTIMIZED SCHEMA
-- =====================================================

-- ============== MENU_CATEGORIES TABLE ==============
ALTER TABLE menu_categories
    ADD CONSTRAINT fk_menu_categories_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_menu_categories_parent
        FOREIGN KEY (parent_category_id) REFERENCES menu_categories(id) ON DELETE SET NULL,
    ADD CONSTRAINT chk_menu_categories_no_self_parent
        CHECK (id != parent_category_id),
    ADD CONSTRAINT chk_menu_categories_display_order
        CHECK (display_order >= 0);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_menu_categories_restaurant
    ON menu_categories (restaurant_id, is_active, display_order);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_menu_categories_parent
    ON menu_categories (parent_category_id)
    WHERE parent_category_id IS NOT NULL;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_menu_categories_featured
    ON menu_categories (restaurant_id)
    WHERE is_featured = true AND is_active = true;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_menu_categories_availability
    ON menu_categories (restaurant_id, is_active)
    WHERE available_from IS NOT NULL;

-- Prevent circular dependencies in category hierarchy
CREATE OR REPLACE FUNCTION check_category_hierarchy()
    RETURNS TRIGGER AS $$
DECLARE
    v_depth INT := 0;
    v_current_id BIGINT := NEW.parent_category_id;
BEGIN
    WHILE v_current_id IS NOT NULL AND v_depth < 10 LOOP
            IF v_current_id = NEW.id THEN
                RAISE EXCEPTION 'Circular dependency detected in category hierarchy';
            END IF;

            SELECT parent_category_id INTO v_current_id
            FROM menu_categories
            WHERE id = v_current_id;

            v_depth := v_depth + 1;
        END LOOP;

    IF v_depth >= 10 THEN
        RAISE EXCEPTION 'Category hierarchy too deep (max 10 levels)';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_check_category_hierarchy
    BEFORE INSERT OR UPDATE ON menu_categories
    FOR EACH ROW
EXECUTE FUNCTION check_category_hierarchy();

-- ============== MENU_ITEMS TABLE ==============
ALTER TABLE menu_items
    ADD CONSTRAINT fk_menu_items_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_menu_items_category
        FOREIGN KEY (category_id) REFERENCES menu_categories(id) ON DELETE SET NULL,
    ADD CONSTRAINT chk_menu_items_prices
        CHECK (base_price >= 0 AND (discounted_price IS NULL OR discounted_price >= 0)),
    ADD CONSTRAINT chk_menu_items_discount_valid
        CHECK (discounted_price IS NULL OR discounted_price < base_price),
    ADD CONSTRAINT chk_menu_items_spice_level
        CHECK (spice_level >= 0 AND spice_level <= 5),
    ADD CONSTRAINT chk_menu_items_stock
        CHECK (stock_quantity IS NULL OR stock_quantity >= 0),
    ADD CONSTRAINT chk_menu_items_prep_time
        CHECK (preparation_time_minutes > 0);

CREATE UNIQUE INDEX IF NOT EXISTS idx_menu_items_sku
    ON menu_items (sku);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_menu_items_restaurant_active
    ON menu_items (restaurant_id, is_active, display_order);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_menu_items_category
    ON menu_items (category_id, is_active);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_menu_items_available
    ON menu_items (restaurant_id, is_available)
    WHERE is_available = true;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_menu_items_featured
    ON menu_items (restaurant_id, is_featured)
    WHERE is_featured = true AND is_active = true;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_menu_items_best_seller
    ON menu_items (restaurant_id, is_best_seller)
    WHERE is_best_seller = true AND is_active = true;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_menu_items_dietary
    ON menu_items (restaurant_id)
    WHERE (is_vegetarian = true OR is_vegan = true OR is_gluten_free = true)
        AND is_active = true;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_menu_items_price_range
    ON menu_items (restaurant_id, base_price)
    WHERE is_active = true;

-- Full-text search on menu items
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_menu_items_search
    ON menu_items USING gin (
                             to_tsvector('english', name || ' ' || COALESCE(description, ''))
        );

-- Low stock monitoring
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_menu_items_low_stock
    ON menu_items (restaurant_id, stock_quantity)
    WHERE stock_quantity IS NOT NULL
        AND stock_quantity <= low_stock_threshold;

-- ============== ITEM_VARIANTS TABLE ==============
ALTER TABLE item_variants
    ADD CONSTRAINT fk_item_variants_menu_item
        FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE,
    ADD CONSTRAINT chk_item_variants_display_order
        CHECK (display_order >= 0);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_item_variants_menu_item
    ON item_variants (menu_item_id, display_order);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_item_variants_default
    ON item_variants (menu_item_id)
    WHERE is_default = true;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_item_variants_available
    ON item_variants (menu_item_id)
    WHERE is_available = true;

-- Ensure only one default variant per menu item
CREATE UNIQUE INDEX IF NOT EXISTS idx_item_variants_one_default
    ON item_variants (menu_item_id)
    WHERE is_default = true;

-- ============== MODIFIER_GROUPS TABLE ==============
ALTER TABLE modifier_groups
    ADD CONSTRAINT fk_modifier_groups_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE,
    ADD CONSTRAINT chk_modifier_groups_selections
        CHECK (min_selections >= 0 AND max_selections > 0 AND max_selections >= min_selections),
    ADD CONSTRAINT chk_modifier_groups_required_selections
        CHECK (NOT is_required OR min_selections > 0);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_modifier_groups_restaurant
    ON modifier_groups (restaurant_id, is_active);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_modifier_groups_active
    ON modifier_groups (is_active)
    WHERE is_active = true;

-- ============== MODIFIER_OPTIONS TABLE ==============
ALTER TABLE modifier_options
    ADD CONSTRAINT fk_modifier_options_group
        FOREIGN KEY (modifier_group_id) REFERENCES modifier_groups(id) ON DELETE CASCADE,
    ADD CONSTRAINT chk_modifier_options_display_order
        CHECK (display_order >= 0);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_modifier_options_group
    ON modifier_options (modifier_group_id, display_order);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_modifier_options_available
    ON modifier_options (modifier_group_id)
    WHERE is_available = true;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_modifier_options_default
    ON modifier_options (modifier_group_id)
    WHERE is_default = true;

-- ============== ITEM_MODIFIERS TABLE ==============
ALTER TABLE item_modifiers
    ADD CONSTRAINT fk_item_modifiers_menu_item
        FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_item_modifiers_modifier_group
        FOREIGN KEY (modifier_group_id) REFERENCES modifier_groups(id) ON DELETE CASCADE;

CREATE UNIQUE INDEX IF NOT EXISTS idx_item_modifiers_unique
    ON item_modifiers (menu_item_id, modifier_group_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_item_modifiers_menu_item
    ON item_modifiers (menu_item_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_item_modifiers_group
    ON item_modifiers (modifier_group_id);

-- ============== PRICE_SCHEDULES TABLE ==============
ALTER TABLE price_schedules
    ADD CONSTRAINT fk_price_schedules_menu_item
        FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE,
    ADD CONSTRAINT chk_price_schedules_dates
        CHECK (end_date IS NULL OR end_date >= start_date),
    ADD CONSTRAINT chk_price_schedules_time
        CHECK (end_time IS NULL OR end_time > start_time),
    ADD CONSTRAINT chk_price_schedules_price
        CHECK (special_price >= 0);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_price_schedules_menu_item
    ON price_schedules (menu_item_id, is_active);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_price_schedules_active
    ON price_schedules (menu_item_id, start_date, end_date)
    WHERE is_active = true;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_price_schedules_current
    ON price_schedules (menu_item_id)
    WHERE is_active = true
        AND (start_date IS NULL OR start_date <= CURRENT_DATE)
        AND (end_date IS NULL OR end_date >= CURRENT_DATE);

-- ============== PERFORMANCE VIEWS ==============

-- Menu item availability with pricing
CREATE OR REPLACE VIEW v_menu_items_available AS
SELECT
    mi.id,
    mi.restaurant_id,
    mi.name,
    mi.sku,
    mc.name as category_name,
    CASE
        WHEN ps.special_price IS NOT NULL THEN ps.special_price
        WHEN mi.discounted_price IS NOT NULL THEN mi.discounted_price
        ELSE mi.base_price
        END as current_price,
    mi.base_price,
    mi.discounted_price,
    ps.special_price as schedule_price,
    mi.is_available,
    mi.stock_quantity,
    mi.preparation_time_minutes,
    mi.is_vegetarian,
    mi.is_vegan,
    mi.is_gluten_free,
    mi.spice_level,
    mi.is_featured,
    mi.is_best_seller
FROM menu_items mi
         LEFT JOIN menu_categories mc ON mi.category_id = mc.id
         LEFT JOIN price_schedules ps ON mi.id = ps.menu_item_id
    AND ps.is_active = true
    AND (ps.start_date IS NULL OR ps.start_date <= CURRENT_DATE)
    AND (ps.end_date IS NULL OR ps.end_date >= CURRENT_DATE)
    AND (ps.start_time IS NULL OR ps.start_time <= CURRENT_TIME)
    AND (ps.end_time IS NULL OR ps.end_time >= CURRENT_TIME)
WHERE mi.is_active = true;

-- Materialized view for popular items (refresh daily)
CREATE MATERIALIZED VIEW IF NOT EXISTS mv_popular_menu_items AS
SELECT
    mi.id as menu_item_id,
    mi.restaurant_id,
    mi.name,
    mi.category_id,
    COUNT(DISTINCT oi.order_id) as order_count,
    SUM(oi.quantity) as total_quantity_sold,
    SUM(oi.subtotal) as total_revenue,
    AVG(oi.price) as average_price,
    DENSE_RANK() OVER (
        PARTITION BY mi.restaurant_id
        ORDER BY COUNT(DISTINCT oi.order_id) DESC
        ) as popularity_rank
FROM menu_items mi
         JOIN order_items oi ON mi.id = oi.menu_item_id
         JOIN orders o ON oi.order_id = o.id
WHERE o.status IN ('COMPLETED', 'DELIVERED')
  AND o.created_at >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY mi.id, mi.restaurant_id, mi.name, mi.category_id;

CREATE UNIQUE INDEX idx_mv_popular_items_item
    ON mv_popular_menu_items (menu_item_id);
CREATE INDEX idx_mv_popular_items_restaurant_rank
    ON mv_popular_menu_items (restaurant_id, popularity_rank);