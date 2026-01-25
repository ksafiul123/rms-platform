-- Inventory Management System Schema

-- Inventory Items
CREATE TABLE inventory_items (
                                 id BIGSERIAL PRIMARY KEY,
                                 restaurant_id BIGINT NOT NULL,
                                 branch_id BIGINT,
                                 item_code VARCHAR(50) NOT NULL,
                                 name VARCHAR(200) NOT NULL,
                                 description TEXT,
                                 category VARCHAR(50) NOT NULL CHECK (category IN (
                                                                                   'VEGETABLES', 'FRUITS', 'MEAT', 'SEAFOOD', 'DAIRY', 'GRAINS',
                                                                                   'SPICES', 'BEVERAGES', 'CONDIMENTS', 'PACKAGING', 'CLEANING_SUPPLIES', 'OTHER'
                                     )),
                                 unit VARCHAR(20) NOT NULL CHECK (unit IN (
                                                                           'KG', 'G', 'L', 'ML', 'PCS', 'DOZEN', 'BOX', 'PACKET', 'BOTTLE', 'CAN'
                                     )),
                                 current_quantity DECIMAL(10, 3) NOT NULL DEFAULT 0,
                                 minimum_quantity DECIMAL(10, 3) NOT NULL,
                                 maximum_quantity DECIMAL(10, 3),
                                 reorder_quantity DECIMAL(10, 3),
                                 cost_per_unit DECIMAL(10, 2) NOT NULL,
                                 supplier_name VARCHAR(200),
                                 supplier_contact VARCHAR(100),
                                 status VARCHAR(20) NOT NULL DEFAULT 'IN_STOCK' CHECK (status IN (
                                                                                                  'IN_STOCK', 'LOW_STOCK', 'OUT_OF_STOCK', 'DISCONTINUED'
                                     )),
                                 expiry_date DATE,
                                 storage_location VARCHAR(100),
                                 notes TEXT,
                                 is_active BOOLEAN NOT NULL DEFAULT TRUE,
                                 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 CONSTRAINT fk_inventory_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE,
                                 CONSTRAINT fk_inventory_branch FOREIGN KEY (branch_id) REFERENCES restaurant_branches(id) ON DELETE SET NULL,
                                 CONSTRAINT uq_inventory_item_code UNIQUE (restaurant_id, item_code),
                                 CONSTRAINT chk_inventory_quantity CHECK (current_quantity >= 0)
);

-- Indexes for inventory_items
CREATE INDEX idx_inventory_items_restaurant ON inventory_items(restaurant_id);
CREATE INDEX idx_inventory_items_category ON inventory_items(category);
CREATE INDEX idx_inventory_items_status ON inventory_items(status);
CREATE INDEX idx_inventory_items_item_code ON inventory_items(item_code);
CREATE INDEX idx_inventory_items_branch ON inventory_items(branch_id);
CREATE INDEX idx_inventory_items_expiry ON inventory_items(expiry_date);

-- Menu Item Inventory Links
CREATE TABLE menu_item_inventory (
                                     id BIGSERIAL PRIMARY KEY,
                                     menu_item_id BIGINT NOT NULL,
                                     inventory_item_id BIGINT NOT NULL,
                                     quantity_required DECIMAL(10, 3) NOT NULL,
                                     is_optional BOOLEAN NOT NULL DEFAULT FALSE,
                                     notes VARCHAR(500),
                                     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                     CONSTRAINT fk_menu_item_inv_menu FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE,
                                     CONSTRAINT fk_menu_item_inv_inventory FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(id) ON DELETE CASCADE,
                                     CONSTRAINT uq_menu_item_inventory UNIQUE (menu_item_id, inventory_item_id),
                                     CONSTRAINT chk_quantity_required CHECK (quantity_required > 0)
);

-- Indexes for menu_item_inventory
CREATE INDEX idx_menu_item_inv_menu ON menu_item_inventory(menu_item_id);
CREATE INDEX idx_menu_item_inv_inventory ON menu_item_inventory(inventory_item_id);

-- Stock Transactions
CREATE TABLE stock_transactions (
                                    id BIGSERIAL PRIMARY KEY,
                                    inventory_item_id BIGINT NOT NULL,
                                    restaurant_id BIGINT NOT NULL,
                                    transaction_type VARCHAR(30) NOT NULL CHECK (transaction_type IN (
                                                                                                      'PURCHASE', 'MANUAL_ADDITION', 'ORDER_DEDUCTION', 'WASTAGE',
                                                                                                      'MANUAL_DEDUCTION', 'ADJUSTMENT', 'TRANSFER_IN', 'TRANSFER_OUT', 'RETURN_TO_SUPPLIER'
                                        )),
                                    quantity DECIMAL(10, 3) NOT NULL,
                                    quantity_before DECIMAL(10, 3) NOT NULL,
                                    quantity_after DECIMAL(10, 3) NOT NULL,
                                    cost_per_unit DECIMAL(10, 2),
                                    total_cost DECIMAL(10, 2),
                                    order_id BIGINT,
                                    performed_by BIGINT NOT NULL,
                                    reference_number VARCHAR(100),
                                    supplier VARCHAR(200),
                                    notes TEXT,
                                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                    CONSTRAINT fk_stock_trans_inventory FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(id) ON DELETE CASCADE,
                                    CONSTRAINT fk_stock_trans_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE,
                                    CONSTRAINT fk_stock_trans_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL,
                                    CONSTRAINT fk_stock_trans_user FOREIGN KEY (performed_by) REFERENCES users(id) ON DELETE RESTRICT
);

-- Indexes for stock_transactions
CREATE INDEX idx_stock_trans_inventory ON stock_transactions(inventory_item_id);
CREATE INDEX idx_stock_trans_type ON stock_transactions(transaction_type);
CREATE INDEX idx_stock_trans_order ON stock_transactions(order_id);
CREATE INDEX idx_stock_trans_created ON stock_transactions(created_at);
CREATE INDEX idx_stock_trans_restaurant ON stock_transactions(restaurant_id);

-- Low Stock Alerts
CREATE TABLE low_stock_alerts (
                                  id BIGSERIAL PRIMARY KEY,
                                  inventory_item_id BIGINT NOT NULL,
                                  restaurant_id BIGINT NOT NULL,
                                  alert_type VARCHAR(20) NOT NULL CHECK (alert_type IN (
                                                                                        'LOW_STOCK', 'OUT_OF_STOCK', 'EXPIRING_SOON'
                                      )),
                                  current_quantity DECIMAL(10, 3) NOT NULL,
                                  minimum_quantity DECIMAL(10, 3) NOT NULL,
                                  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN (
                                                                                                 'ACTIVE', 'ACKNOWLEDGED', 'RESOLVED'
                                      )),
                                  acknowledged_by BIGINT,
                                  acknowledged_at TIMESTAMP,
                                  resolved_at TIMESTAMP,
                                  notes TEXT,
                                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                  CONSTRAINT fk_alert_inventory FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(id) ON DELETE CASCADE,
                                  CONSTRAINT fk_alert_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE,
                                  CONSTRAINT fk_alert_acknowledged_by FOREIGN KEY (acknowledged_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Indexes for low_stock_alerts
CREATE INDEX idx_alerts_inventory ON low_stock_alerts(inventory_item_id);
CREATE INDEX idx_alerts_restaurant ON low_stock_alerts(restaurant_id);
CREATE INDEX idx_alerts_status ON low_stock_alerts(status);
CREATE INDEX idx_alerts_created ON low_stock_alerts(created_at);
CREATE INDEX idx_alerts_type ON low_stock_alerts(alert_type);

-- Triggers
CREATE OR REPLACE FUNCTION update_inventory_updated_at()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_inventory_updated_at
    BEFORE UPDATE ON inventory_items
    FOR EACH ROW
EXECUTE FUNCTION update_inventory_updated_at();

CREATE TRIGGER trigger_update_menu_item_inv_updated_at
    BEFORE UPDATE ON menu_item_inventory
    FOR EACH ROW
EXECUTE FUNCTION update_inventory_updated_at();

-- Function to automatically update inventory status based on quantity
CREATE OR REPLACE FUNCTION update_inventory_status()
    RETURNS TRIGGER AS $$
BEGIN
    IF NEW.current_quantity <= 0 THEN
        NEW.status = 'OUT_OF_STOCK';
    ELSIF NEW.current_quantity <= NEW.minimum_quantity THEN
        NEW.status = 'LOW_STOCK';
    ELSE
        NEW.status = 'IN_STOCK';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_inventory_status
    BEFORE INSERT OR UPDATE OF current_quantity, minimum_quantity ON inventory_items
    FOR EACH ROW
EXECUTE FUNCTION update_inventory_status();

-- Views for reporting
CREATE VIEW inventory_value_summary AS
SELECT
    i.restaurant_id,
    i.category,
    COUNT(*) AS item_count,
    SUM(i.current_quantity * i.cost_per_unit) AS total_value,
    SUM(CASE WHEN i.status = 'LOW_STOCK' THEN 1 ELSE 0 END) AS low_stock_count,
    SUM(CASE WHEN i.status = 'OUT_OF_STOCK' THEN 1 ELSE 0 END) AS out_of_stock_count
FROM inventory_items i
WHERE i.is_active = TRUE
GROUP BY i.restaurant_id, i.category;

CREATE VIEW menu_item_availability AS
SELECT
    mi.id AS menu_item_id,
    mi.name AS menu_item_name,
    mi.restaurant_id,
    COUNT(mii.id) AS ingredient_count,
    SUM(CASE WHEN ii.current_quantity >= mii.quantity_required THEN 1 ELSE 0 END) AS available_ingredients,
    CASE
        WHEN COUNT(mii.id) FILTER (WHERE mii.is_optional = FALSE) =
             SUM(CASE WHEN ii.current_quantity >= mii.quantity_required AND mii.is_optional = FALSE THEN 1 ELSE 0 END)
            THEN TRUE
        ELSE FALSE
        END AS is_available
FROM menu_items mi
         LEFT JOIN menu_item_inventory mii ON mi.id = mii.menu_item_id
         LEFT JOIN inventory_items ii ON mii.inventory_item_id = ii.id
WHERE mi.is_active = TRUE
GROUP BY mi.id, mi.name, mi.restaurant_id;

-- Comments for documentation
COMMENT ON TABLE inventory_items IS 'Stores all inventory items with stock levels and alerts';
COMMENT ON TABLE menu_item_inventory IS 'Links menu items to required inventory items (ingredients)';
COMMENT ON TABLE stock_transactions IS 'Audit trail of all stock movements (in/out)';
COMMENT ON TABLE low_stock_alerts IS 'Tracks low stock alerts for inventory items';

COMMENT ON COLUMN inventory_items.current_quantity IS 'Current stock quantity';
COMMENT ON COLUMN inventory_items.minimum_quantity IS 'Minimum quantity threshold (triggers low stock alert)';
COMMENT ON COLUMN inventory_items.maximum_quantity IS 'Maximum stock capacity';
COMMENT ON COLUMN inventory_items.reorder_quantity IS 'Standard quantity to reorder when stock is low';
COMMENT ON COLUMN inventory_items.status IS 'Auto-calculated based on current_quantity vs minimum_quantity';

COMMENT ON COLUMN menu_item_inventory.quantity_required IS 'Quantity of inventory item needed per 1 menu item';
COMMENT ON COLUMN menu_item_inventory.is_optional IS 'If true, menu item can be made without this ingredient';

COMMENT ON COLUMN stock_transactions.quantity IS 'Positive for stock increases, negative for decreases';
COMMENT ON COLUMN stock_transactions.transaction_type IS 'Type of stock movement';
COMMENT ON COLUMN stock_transactions.order_id IS 'Reference to order if deduction was for order fulfillment';