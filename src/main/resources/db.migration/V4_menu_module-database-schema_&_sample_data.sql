-- ============================================
-- MENU MANAGEMENT MODULE - DATABASE SCHEMA
-- ============================================

-- Menu Categories Table
CREATE TABLE menu_categories (
                                 id BIGSERIAL PRIMARY KEY,
                                 restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
                                 name VARCHAR(100) NOT NULL,
                                 description VARCHAR(500),
                                 display_order INTEGER,
                                 image_url VARCHAR(500),
                                 icon_name VARCHAR(50),
                                 parent_category_id BIGINT REFERENCES menu_categories(id) ON DELETE SET NULL,
                                 is_active BOOLEAN DEFAULT TRUE,
                                 is_featured BOOLEAN DEFAULT FALSE,
                                 available_from TIME,
                                 available_to TIME,
                                 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_category_restaurant ON menu_categories(restaurant_id);
CREATE INDEX idx_category_parent ON menu_categories(parent_category_id);
CREATE INDEX idx_category_active ON menu_categories(restaurant_id, is_active);

-- Menu Items Table
CREATE TABLE menu_items (
                            id BIGSERIAL PRIMARY KEY,
                            restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
                            category_id BIGINT NOT NULL REFERENCES menu_categories(id),
                            sku VARCHAR(20) NOT NULL,
                            name VARCHAR(150) NOT NULL,
                            description VARCHAR(1000),
                            base_price DECIMAL(10,2) NOT NULL,
                            discounted_price DECIMAL(10,2),
                            cost_price DECIMAL(10,2),
                            image_url VARCHAR(500),
                            preparation_time_minutes INTEGER,
                            item_type VARCHAR(50),
                            is_vegetarian BOOLEAN DEFAULT FALSE,
                            is_vegan BOOLEAN DEFAULT FALSE,
                            is_gluten_free BOOLEAN DEFAULT FALSE,
                            is_spicy BOOLEAN DEFAULT FALSE,
                            spice_level INTEGER,
                            calories INTEGER,
                            allergen_info VARCHAR(500),
                            is_available BOOLEAN DEFAULT TRUE,
                            is_active BOOLEAN DEFAULT TRUE,
                            is_featured BOOLEAN DEFAULT FALSE,
                            is_best_seller BOOLEAN DEFAULT FALSE,
                            available_from TIME,
                            available_to TIME,
                            available_for_dine_in BOOLEAN DEFAULT TRUE,
                            available_for_takeaway BOOLEAN DEFAULT TRUE,
                            available_for_delivery BOOLEAN DEFAULT TRUE,
                            stock_quantity INTEGER,
                            low_stock_threshold INTEGER,
                            display_order INTEGER,
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            UNIQUE(sku, restaurant_id)
);

CREATE INDEX idx_item_restaurant ON menu_items(restaurant_id);
CREATE INDEX idx_item_category ON menu_items(category_id);
CREATE INDEX idx_item_sku ON menu_items(sku);
CREATE INDEX idx_item_active ON menu_items(restaurant_id, is_active);
CREATE INDEX idx_item_available ON menu_items(restaurant_id, is_available);

-- Item Variants Table (Size variations)
CREATE TABLE item_variants (
                               id BIGSERIAL PRIMARY KEY,
                               menu_item_id BIGINT NOT NULL REFERENCES menu_items(id) ON DELETE CASCADE,
                               name VARCHAR(50) NOT NULL,
                               sku VARCHAR(20),
                               price_adjustment DECIMAL(10,2) DEFAULT 0.00,
                               is_default BOOLEAN DEFAULT FALSE,
                               is_available BOOLEAN DEFAULT TRUE,
                               display_order INTEGER,
                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_variant_item ON item_variants(menu_item_id);

-- Modifier Groups Table
CREATE TABLE modifier_groups (
                                 id BIGSERIAL PRIMARY KEY,
                                 restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
                                 name VARCHAR(100) NOT NULL,
                                 description VARCHAR(500),
                                 selection_type VARCHAR(20) NOT NULL,
                                 min_selections INTEGER DEFAULT 0,
                                 max_selections INTEGER DEFAULT 1,
                                 is_required BOOLEAN DEFAULT FALSE,
                                 display_order INTEGER,
                                 is_active BOOLEAN DEFAULT TRUE,
                                 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_modifier_group_restaurant ON modifier_groups(restaurant_id);

-- Modifier Options Table
CREATE TABLE modifier_options (
                                  id BIGSERIAL PRIMARY KEY,
                                  modifier_group_id BIGINT NOT NULL REFERENCES modifier_groups(id) ON DELETE CASCADE,
                                  name VARCHAR(100) NOT NULL,
                                  description VARCHAR(500),
                                  price_adjustment DECIMAL(10,2) DEFAULT 0.00,
                                  is_default BOOLEAN DEFAULT FALSE,
                                  is_available BOOLEAN DEFAULT TRUE,
                                  display_order INTEGER,
                                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_modifier_option_group ON modifier_options(modifier_group_id);

-- Item Modifiers Junction Table
CREATE TABLE item_modifiers (
                                id BIGSERIAL PRIMARY KEY,
                                menu_item_id BIGINT NOT NULL REFERENCES menu_items(id) ON DELETE CASCADE,
                                modifier_group_id BIGINT NOT NULL REFERENCES modifier_groups(id) ON DELETE CASCADE,
                                is_required BOOLEAN DEFAULT FALSE,
                                display_order INTEGER,
                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                UNIQUE(menu_item_id, modifier_group_id)
);

CREATE INDEX idx_item_modifier_item ON item_modifiers(menu_item_id);
CREATE INDEX idx_item_modifier_group ON item_modifiers(modifier_group_id);

-- Price Schedules Table (Dynamic pricing)
CREATE TABLE price_schedules (
                                 id BIGSERIAL PRIMARY KEY,
                                 menu_item_id BIGINT NOT NULL REFERENCES menu_items(id) ON DELETE CASCADE,
                                 name VARCHAR(100) NOT NULL,
                                 price DECIMAL(10,2) NOT NULL,
                                 day_of_week INTEGER,
                                 start_time TIME,
                                 end_time TIME,
                                 start_date TIMESTAMP,
                                 end_date TIMESTAMP,
                                 is_active BOOLEAN DEFAULT TRUE,
                                 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_price_schedule_item ON price_schedules(menu_item_id);

-- Ingredients Table
CREATE TABLE ingredients (
                             id BIGSERIAL PRIMARY KEY,
                             restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
                             name VARCHAR(100) NOT NULL,
                             sku VARCHAR(20),
                             unit VARCHAR(50),
                             current_stock DECIMAL(10,2),
                             min_stock DECIMAL(10,2),
                             cost_per_unit DECIMAL(10,2),
                             is_active BOOLEAN DEFAULT TRUE,
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ingredient_restaurant ON ingredients(restaurant_id);
CREATE INDEX idx_ingredient_sku ON ingredients(sku, restaurant_id);

-- Item Ingredients Table (Recipe)
CREATE TABLE item_ingredients (
                                  id BIGSERIAL PRIMARY KEY,
                                  menu_item_id BIGINT NOT NULL REFERENCES menu_items(id) ON DELETE CASCADE,
                                  ingredient_id BIGINT NOT NULL REFERENCES ingredients(id) ON DELETE CASCADE,
                                  quantity DECIMAL(10,3) NOT NULL,
                                  unit VARCHAR(50),
                                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_item_ingredient_item ON item_ingredients(menu_item_id);
CREATE INDEX idx_item_ingredient_ingredient ON item_ingredients(ingredient_id);

-- ============================================
-- SAMPLE DATA
-- ============================================

-- Sample Categories for Restaurant ID 2
INSERT INTO menu_categories (restaurant_id, name, description, display_order, icon_name, is_featured) VALUES
                                                                                                          (2, 'Appetizers', 'Start your meal with our delicious appetizers', 1, 'appetizer', TRUE),
                                                                                                          (2, 'Main Course', 'Our signature main dishes', 2, 'main-dish', TRUE),
                                                                                                          (2, 'Desserts', 'Sweet endings to your meal', 3, 'dessert', FALSE),
                                                                                                          (2, 'Beverages', 'Refreshing drinks and beverages', 4, 'beverage', TRUE);

-- Sample Subcategories (Nested)
INSERT INTO menu_categories (restaurant_id, name, description, parent_category_id, display_order) VALUES
                                                                                                      (2, 'Hot Beverages', 'Coffee, Tea, and hot drinks',
                                                                                                       (SELECT id FROM menu_categories WHERE name = 'Beverages' AND restaurant_id = 2 LIMIT 1), 1),
                                                                                                      (2, 'Cold Beverages', 'Juices, Smoothies, and cold drinks',
                                                                                                       (SELECT id FROM menu_categories WHERE name = 'Beverages' AND restaurant_id = 2 LIMIT 1), 2);

-- Sample Modifier Groups
INSERT INTO modifier_groups (restaurant_id, name, description, selection_type, min_selections, max_selections, is_required, display_order) VALUES
                                                                                                                                               (2, 'Spice Level', 'Choose your preferred spice level', 'SINGLE', 1, 1, TRUE, 1),
                                                                                                                                               (2, 'Extra Toppings', 'Add extra toppings to your dish', 'MULTIPLE', 0, 5, FALSE, 2),
                                                                                                                                               (2, 'Portion Size', 'Choose your portion size', 'SINGLE', 1, 1, TRUE, 3);

-- Sample Modifier Options
INSERT INTO modifier_options (modifier_group_id, name, price_adjustment, is_default, display_order) VALUES
-- Spice Levels
((SELECT id FROM modifier_groups WHERE name = 'Spice Level' AND restaurant_id = 2 LIMIT 1), 'Mild', 0.00, TRUE, 1),
((SELECT id FROM modifier_groups WHERE name = 'Spice Level' AND restaurant_id = 2 LIMIT 1), 'Medium', 0.00, FALSE, 2),
((SELECT id FROM modifier_groups WHERE name = 'Spice Level' AND restaurant_id = 2 LIMIT 1), 'Hot', 0.00, FALSE, 3),
((SELECT id FROM modifier_groups WHERE name = 'Spice Level' AND restaurant_id = 2 LIMIT 1), 'Extra Hot', 0.00, FALSE, 4),

-- Extra Toppings
((SELECT id FROM modifier_groups WHERE name = 'Extra Toppings' AND restaurant_id = 2 LIMIT 1), 'Extra Cheese', 1.50, FALSE, 1),
((SELECT id FROM modifier_groups WHERE name = 'Extra Toppings' AND restaurant_id = 2 LIMIT 1), 'Extra Chicken', 3.00, FALSE, 2),
((SELECT id FROM modifier_groups WHERE name = 'Extra Toppings' AND restaurant_id = 2 LIMIT 1), 'Mushrooms', 1.00, FALSE, 3),
((SELECT id FROM modifier_groups WHERE name = 'Extra Toppings' AND restaurant_id = 2 LIMIT 1), 'Olives', 1.00, FALSE, 4),

-- Portion Sizes
((SELECT id FROM modifier_groups WHERE name = 'Portion Size' AND restaurant_id = 2 LIMIT 1), 'Regular', 0.00, TRUE, 1),
((SELECT id FROM modifier_groups WHERE name = 'Portion Size' AND restaurant_id = 2 LIMIT 1), 'Large', 3.00, FALSE, 2);

-- Sample Menu Items
INSERT INTO menu_items (
    restaurant_id, category_id, sku, name, description, base_price, discounted_price,
    preparation_time_minutes, item_type, is_vegetarian, is_spicy, spice_level,
    calories, is_featured, is_best_seller, display_order
) VALUES
      (2,
       (SELECT id FROM menu_categories WHERE name = 'Appetizers' AND restaurant_id = 2 LIMIT 1),
       'ITEM-2-00001', 'Spring Rolls', 'Crispy vegetable spring rolls served with sweet chili sauce',
       6.99, NULL, 15, 'APPETIZER', TRUE, FALSE, 0, 250, TRUE, FALSE, 1),

      (2,
       (SELECT id FROM menu_categories WHERE name = 'Appetizers' AND restaurant_id = 2 LIMIT 1),
       'ITEM-2-00002', 'Chicken Wings', 'Spicy buffalo chicken wings with ranch dip',
       9.99, 7.99, 20, 'APPETIZER', FALSE, TRUE, 3, 450, TRUE, TRUE, 2),

      (2,
       (SELECT id FROM menu_categories WHERE name = 'Main Course' AND restaurant_id = 2 LIMIT 1),
       'ITEM-2-00003', 'Margherita Pizza', 'Classic pizza with tomato sauce, mozzarella, and fresh basil',
       12.99, NULL, 25, 'MAIN_COURSE', TRUE, FALSE, 1, 680, TRUE, TRUE, 1),

      (2,
       (SELECT id FROM menu_categories WHERE name = 'Main Course' AND restaurant_id = 2 LIMIT 1),
       'ITEM-2-00004', 'Chicken Tikka Masala', 'Tender chicken in creamy tomato-based curry sauce',
       14.99, NULL, 30, 'MAIN_COURSE', FALSE, TRUE, 2, 520, TRUE, TRUE, 2),

      (2,
       (SELECT id FROM menu_categories WHERE name = 'Desserts' AND restaurant_id = 2 LIMIT 1),
       'ITEM-2-00005', 'Chocolate Lava Cake', 'Warm chocolate cake with molten center, served with vanilla ice cream',
       7.99, NULL, 15, 'DESSERT', TRUE, FALSE, 0, 420, TRUE, FALSE, 1);

-- Sample Item Variants (for Pizza)
INSERT INTO item_variants (menu_item_id, name, sku, price_adjustment, is_default, display_order) VALUES
                                                                                                     ((SELECT id FROM menu_items WHERE sku = 'ITEM-2-00003'), 'Small (9")', 'ITEM-2-00003-S', -2.00, FALSE, 1),
                                                                                                     ((SELECT id FROM menu_items WHERE sku = 'ITEM-2-00003'), 'Medium (12")', 'ITEM-2-00003-M', 0.00, TRUE, 2),
                                                                                                     ((SELECT id FROM menu_items WHERE sku = 'ITEM-2-00003'), 'Large (15")', 'ITEM-2-00003-L', 4.00, FALSE, 3);

-- Link Modifiers to Items
INSERT INTO item_modifiers (menu_item_id, modifier_group_id, is_required, display_order) VALUES
-- Pizza gets Extra Toppings
((SELECT id FROM menu_items WHERE sku = 'ITEM-2-00003'),
 (SELECT id FROM modifier_groups WHERE name = 'Extra Toppings' AND restaurant_id = 2 LIMIT 1), FALSE, 1),

-- Chicken Tikka gets Spice Level and Portion Size
((SELECT id FROM menu_items WHERE sku = 'ITEM-2-00004'),
 (SELECT id FROM modifier_groups WHERE name = 'Spice Level' AND restaurant_id = 2 LIMIT 1), TRUE, 1),
((SELECT id FROM menu_items WHERE sku = 'ITEM-2-00004'),
 (SELECT id FROM modifier_groups WHERE name = 'Portion Size' AND restaurant_id = 2 LIMIT 1), TRUE, 2);

-- Sample Ingredients
INSERT INTO ingredients (restaurant_id, name, sku, unit, current_stock, min_stock, cost_per_unit) VALUES
                                                                                                      (2, 'Chicken Breast', 'ING-001', 'kg', 50.00, 10.00, 8.50),
                                                                                                      (2, 'Tomato Sauce', 'ING-002', 'liter', 20.00, 5.00, 3.00),
                                                                                                      (2, 'Mozzarella Cheese', 'ING-003', 'kg', 15.00, 3.00, 12.00),
                                                                                                      (2, 'Fresh Basil', 'ING-004', 'bunch', 10.00, 2.00, 1.50),
                                                                                                      (2, 'Vegetables Mix', 'ING-005', 'kg', 25.00, 5.00, 4.00);

-- Link Ingredients to Items (Recipe)
INSERT INTO item_ingredients (menu_item_id, ingredient_id, quantity, unit) VALUES
-- Margherita Pizza
((SELECT id FROM menu_items WHERE sku = 'ITEM-2-00003'),
 (SELECT id FROM ingredients WHERE sku = 'ING-002'), 0.150, 'liter'),
((SELECT id FROM menu_items WHERE sku = 'ITEM-2-00003'),
 (SELECT id FROM ingredients WHERE sku = 'ING-003'), 0.200, 'kg'),
((SELECT id FROM menu_items WHERE sku = 'ITEM-2-00003'),
 (SELECT id FROM ingredients WHERE sku = 'ING-004'), 1.000, 'bunch'),

-- Spring Rolls
((SELECT id FROM menu_items WHERE sku = 'ITEM-2-00001'),
 (SELECT id FROM ingredients WHERE sku = 'ING-005'), 0.150, 'kg'),

-- Chicken Tikka Masala
((SELECT id FROM menu_items WHERE sku = 'ITEM-2-00004'),
 (SELECT id FROM ingredients WHERE sku = 'ING-001'), 0.300, 'kg'),
((SELECT id FROM menu_items WHERE sku = 'ING-002'), 0.200, 'liter');

-- ============================================
-- USEFUL QUERIES
-- ============================================

-- Get full menu with categories and items
SELECT
    c.id as category_id,
    c.name as category_name,
    c.display_order as cat_order,
    mi.id as item_id,
    mi.name as item_name,
    mi.base_price,
    mi.discounted_price,
    mi.is_available,
    mi.display_order as item_order
FROM menu_categories c
         LEFT JOIN menu_items mi ON c.id = mi.category_id AND mi.is_active = TRUE
WHERE c.restaurant_id = 2
  AND c.is_active = TRUE
ORDER BY c.display_order, mi.display_order;

-- Get menu item with all details
SELECT
    mi.*,
    c.name as category_name,
    COALESCE(mi.discounted_price, mi.base_price) as final_price
FROM menu_items mi
         JOIN menu_categories c ON mi.category_id = c.id
WHERE mi.id = 1;

-- Get item variants
SELECT * FROM item_variants
WHERE menu_item_id = 1
ORDER BY display_order;

-- Get item modifiers with options
SELECT
    mg.name as group_name,
    mg.selection_type,
    mg.is_required,
    mo.name as option_name,
    mo.price_adjustment
FROM item_modifiers im
         JOIN modifier_groups mg ON im.modifier_group_id = mg.id
         JOIN modifier_options mo ON mo.modifier_group_id = mg.id
WHERE im.menu_item_id = 1
  AND mo.is_available = TRUE
ORDER BY im.display_order, mo.display_order;

-- Calculate item cost based on ingredients
SELECT
    mi.name,
    mi.base_price as selling_price,
    SUM(ii.quantity * ing.cost_per_unit) as ingredient_cost,
    mi.base_price - SUM(ii.quantity * ing.cost_per_unit) as profit_margin
FROM menu_items mi
         JOIN item_ingredients ii ON mi.id = ii.menu_item_id
         JOIN ingredients ing ON ii.ingredient_id = ing.id
WHERE mi.id = 1
GROUP BY mi.id, mi.name, mi.base_price;

-- Find low stock ingredients
SELECT * FROM ingredients
WHERE restaurant_id = 2
  AND current_stock <= min_stock
ORDER BY current_stock ASC;