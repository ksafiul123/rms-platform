-- Customer Preference Tracking System Schema

-- Customer Preferences (Global)
CREATE TABLE customer_preferences (
                                      id BIGSERIAL PRIMARY KEY,
                                      customer_id BIGINT NOT NULL UNIQUE,
                                      spice_level VARCHAR(20) CHECK (spice_level IN ('NONE', 'MILD', 'MEDIUM', 'HOT', 'EXTRA_HOT')),
                                      sweetness_level VARCHAR(20) CHECK (sweetness_level IN ('NO_SUGAR', 'LOW', 'MEDIUM', 'HIGH')),
                                      salt_level VARCHAR(20) CHECK (salt_level IN ('NO_SALT', 'LOW', 'MEDIUM', 'HIGH')),
                                      cooking_preference VARCHAR(20) CHECK (cooking_preference IN (
                                                                                                   'RARE', 'MEDIUM_RARE', 'MEDIUM', 'MEDIUM_WELL', 'WELL_DONE',
                                                                                                   'CRISPY', 'SOFT', 'AL_DENTE'
                                          )),
                                      temperature_preference VARCHAR(20) CHECK (temperature_preference IN (
                                                                                                           'COLD', 'ROOM_TEMP', 'HOT', 'EXTRA_HOT'
                                          )),
                                      is_vegetarian BOOLEAN NOT NULL DEFAULT FALSE,
                                      is_vegan BOOLEAN NOT NULL DEFAULT FALSE,
                                      is_gluten_free BOOLEAN NOT NULL DEFAULT FALSE,
                                      is_dairy_free BOOLEAN NOT NULL DEFAULT FALSE,
                                      is_nut_free BOOLEAN NOT NULL DEFAULT FALSE,
                                      special_instructions TEXT,
                                      portion_preference VARCHAR(20),
                                      visible_to_chefs BOOLEAN NOT NULL DEFAULT TRUE,
                                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                      CONSTRAINT fk_customer_pref_customer FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes for customer_preferences
CREATE INDEX idx_customer_pref_customer ON customer_preferences(customer_id);
CREATE INDEX idx_customer_pref_visible ON customer_preferences(visible_to_chefs);

-- Customer Allergies (ElementCollection)
CREATE TABLE customer_allergies (
                                    customer_preference_id BIGINT NOT NULL,
                                    allergy VARCHAR(100) NOT NULL,

                                    CONSTRAINT fk_allergies_preference FOREIGN KEY (customer_preference_id)
                                        REFERENCES customer_preferences(id) ON DELETE CASCADE
);

CREATE INDEX idx_allergies_preference ON customer_allergies(customer_preference_id);

-- Customer Dislikes (ElementCollection)
CREATE TABLE customer_dislikes (
                                   customer_preference_id BIGINT NOT NULL,
                                   dislike VARCHAR(100) NOT NULL,

                                   CONSTRAINT fk_dislikes_preference FOREIGN KEY (customer_preference_id)
                                       REFERENCES customer_preferences(id) ON DELETE CASCADE
);

CREATE INDEX idx_dislikes_preference ON customer_dislikes(customer_preference_id);

-- Favorite Menu Items
CREATE TABLE favorite_menu_items (
                                     id BIGSERIAL PRIMARY KEY,
                                     customer_id BIGINT NOT NULL,
                                     menu_item_id BIGINT NOT NULL,
                                     restaurant_id BIGINT NOT NULL,
                                     notes VARCHAR(500),
                                     order_count INTEGER NOT NULL DEFAULT 0,
                                     last_ordered_at TIMESTAMP,
                                     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                     CONSTRAINT fk_favorite_customer FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE,
                                     CONSTRAINT fk_favorite_menu_item FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE,
                                     CONSTRAINT fk_favorite_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE,
                                     CONSTRAINT uq_favorite_customer_item UNIQUE (customer_id, menu_item_id)
);

-- Indexes for favorite_menu_items
CREATE INDEX idx_favorite_customer ON favorite_menu_items(customer_id);
CREATE INDEX idx_favorite_menu_item ON favorite_menu_items(menu_item_id);
CREATE INDEX idx_favorite_restaurant ON favorite_menu_items(restaurant_id);
CREATE INDEX idx_favorite_order_count ON favorite_menu_items(order_count DESC);

-- Menu Item Specific Preferences
CREATE TABLE menu_item_preferences (
                                       id BIGSERIAL PRIMARY KEY,
                                       customer_id BIGINT NOT NULL,
                                       menu_item_id BIGINT NOT NULL,
                                       spice_level VARCHAR(20) CHECK (spice_level IN ('NONE', 'MILD', 'MEDIUM', 'HOT', 'EXTRA_HOT')),
                                       cooking_preference VARCHAR(20) CHECK (cooking_preference IN (
                                                                                                    'RARE', 'MEDIUM_RARE', 'MEDIUM', 'MEDIUM_WELL', 'WELL_DONE',
                                                                                                    'CRISPY', 'SOFT', 'AL_DENTE'
                                           )),
                                       extra_ingredients VARCHAR(500),
                                       remove_ingredients VARCHAR(500),
                                       special_instructions TEXT,
                                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                       CONSTRAINT fk_menu_item_pref_customer FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE,
                                       CONSTRAINT fk_menu_item_pref_item FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE,
                                       CONSTRAINT uq_menu_item_pref_customer_item UNIQUE (customer_id, menu_item_id)
);

-- Indexes for menu_item_preferences
CREATE INDEX idx_menu_item_pref_customer ON menu_item_preferences(customer_id);
CREATE INDEX idx_menu_item_pref_item ON menu_item_preferences(menu_item_id);

-- Triggers for updated_at
CREATE OR REPLACE FUNCTION update_customer_pref_updated_at()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_customer_pref_updated_at
    BEFORE UPDATE ON customer_preferences
    FOR EACH ROW
EXECUTE FUNCTION update_customer_pref_updated_at();

CREATE TRIGGER trigger_update_menu_item_pref_updated_at
    BEFORE UPDATE ON menu_item_preferences
    FOR EACH ROW
EXECUTE FUNCTION update_customer_pref_updated_at();

-- Views for reporting and analytics

-- Customer dietary restrictions summary
CREATE VIEW customer_dietary_summary AS
SELECT
    cp.customer_id,
    u.name AS customer_name,
    cp.is_vegetarian,
    cp.is_vegan,
    cp.is_gluten_free,
    cp.is_dairy_free,
    cp.is_nut_free,
    CASE
        WHEN EXISTS (SELECT 1 FROM customer_allergies ca WHERE ca.customer_preference_id = cp.id)
            THEN TRUE ELSE FALSE
        END AS has_allergies,
    cp.visible_to_chefs
FROM customer_preferences cp
         JOIN users u ON cp.customer_id = u.id;

-- Popular menu items (by favorites)
CREATE VIEW popular_favorite_items AS
SELECT
    mi.id AS menu_item_id,
    mi.name AS menu_item_name,
    mi.restaurant_id,
    r.name AS restaurant_name,
    COUNT(f.id) AS favorite_count,
    SUM(f.order_count) AS total_orders,
    AVG(f.order_count) AS avg_orders_per_customer
FROM menu_items mi
         JOIN favorite_menu_items f ON mi.id = f.menu_item_id
         JOIN restaurants r ON mi.restaurant_id = r.id
GROUP BY mi.id, mi.name, mi.restaurant_id, r.name
ORDER BY favorite_count DESC;

-- Customer preference statistics by restaurant
CREATE VIEW restaurant_preference_stats AS
SELECT
    r.id AS restaurant_id,
    r.name AS restaurant_name,
    COUNT(DISTINCT f.customer_id) AS total_customers_with_favorites,
    COUNT(f.id) AS total_favorites,
    SUM(CASE WHEN cp.is_vegetarian THEN 1 ELSE 0 END) AS vegetarian_customers,
    SUM(CASE WHEN cp.is_vegan THEN 1 ELSE 0 END) AS vegan_customers,
    SUM(CASE WHEN cp.is_gluten_free THEN 1 ELSE 0 END) AS gluten_free_customers,
    SUM(CASE WHEN cp.spice_level = 'HOT' OR cp.spice_level = 'EXTRA_HOT' THEN 1 ELSE 0 END) AS spicy_preference_customers
FROM restaurants r
         LEFT JOIN favorite_menu_items f ON r.id = f.restaurant_id
         LEFT JOIN customer_preferences cp ON f.customer_id = cp.customer_id
WHERE cp.visible_to_chefs = TRUE
GROUP BY r.id, r.name;

-- Comments for documentation
COMMENT ON TABLE customer_preferences IS 'Stores global food preferences for customers, visible to chefs across all restaurants';
COMMENT ON TABLE favorite_menu_items IS 'Tracks customer favorite menu items with order count';
COMMENT ON TABLE menu_item_preferences IS 'Stores customer preferences specific to individual menu items';
COMMENT ON TABLE customer_allergies IS 'Customer allergies list';
COMMENT ON TABLE customer_dislikes IS 'Customer dislikes list';

COMMENT ON COLUMN customer_preferences.visible_to_chefs IS 'Privacy control - if false, preferences are hidden from chefs';
COMMENT ON COLUMN customer_preferences.spice_level IS 'Global spice preference: NONE, MILD, MEDIUM, HOT, EXTRA_HOT';
COMMENT ON COLUMN customer_preferences.cooking_preference IS 'How customer prefers food cooked: RARE, MEDIUM, WELL_DONE, CRISPY, etc.';
COMMENT ON COLUMN customer_preferences.temperature_preference IS 'Preferred serving temperature';

COMMENT ON COLUMN favorite_menu_items.order_count IS 'Tracks how many times this favorite has been ordered';
COMMENT ON COLUMN favorite_menu_items.last_ordered_at IS 'Last time this favorite was ordered';

COMMENT ON COLUMN menu_item_preferences.extra_ingredients IS 'Comma-separated list of extra ingredients customer wants';
COMMENT ON COLUMN menu_item_preferences.remove_ingredients IS 'Comma-separated list of ingredients to remove';

-- Sample queries for chefs

-- Get customer preferences for an order
-- SELECT cp.*, ca.allergy, cd.dislike
-- FROM customer_preferences cp
-- LEFT JOIN customer_allergies ca ON cp.id = ca.customer_preference_id
-- LEFT JOIN customer_dislikes cd ON cp.id = cd.customer_preference_id
-- WHERE cp.customer_id = ? AND cp.visible_to_chefs = TRUE;

-- Get menu item specific preferences for an order item
-- SELECT mip.*
-- FROM menu_item_preferences mip
-- WHERE mip.customer_id = ? AND mip.menu_item_id = ?;

-- Get customer's favorites at a restaurant
-- SELECT f.*, mi.name AS menu_item_name
-- FROM favorite_menu_items f
-- JOIN menu_items mi ON f.menu_item_id = mi.id
-- WHERE f.customer_id = ? AND f.restaurant_id = ?
-- ORDER BY f.order_count DESC, f.last_ordered_at DESC;