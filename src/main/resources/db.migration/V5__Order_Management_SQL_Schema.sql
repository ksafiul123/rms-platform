-- Order Management System Schema

-- Orders table
CREATE TABLE orders (
                        id BIGSERIAL PRIMARY KEY,
                        restaurant_id BIGINT NOT NULL,
                        customer_id BIGINT NOT NULL,
                        order_number VARCHAR(20) NOT NULL UNIQUE,
                        order_type VARCHAR(20) NOT NULL CHECK (order_type IN ('DINE_IN', 'TAKEAWAY', 'DELIVERY')),
                        status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'OUT_FOR_DELIVERY', 'COMPLETED', 'CANCELLED')),
                        table_number VARCHAR(10),
                        delivery_address TEXT,
                        delivery_man_id BIGINT,
                        subtotal DECIMAL(10, 2) NOT NULL,
                        tax_amount DECIMAL(10, 2) NOT NULL,
                        delivery_fee DECIMAL(10, 2),
                        discount_amount DECIMAL(10, 2),
                        total_amount DECIMAL(10, 2) NOT NULL,
                        special_instructions TEXT,
                        estimated_ready_time TIMESTAMP,
                        actual_ready_time TIMESTAMP,
                        delivery_time TIMESTAMP,
                        cancelled_at TIMESTAMP,
                        cancelled_by BIGINT,
                        cancellation_reason TEXT,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                        CONSTRAINT fk_orders_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE,
                        CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE,
                        CONSTRAINT fk_orders_delivery_man FOREIGN KEY (delivery_man_id) REFERENCES users(id) ON DELETE SET NULL,
                        CONSTRAINT fk_orders_cancelled_by FOREIGN KEY (cancelled_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Indexes for orders
CREATE INDEX idx_orders_restaurant_id ON orders(restaurant_id);
CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_delivery_man_id ON orders(delivery_man_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_order_type ON orders(order_type);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_orders_order_number ON orders(order_number);
CREATE INDEX idx_orders_restaurant_status ON orders(restaurant_id, status);
CREATE INDEX idx_orders_restaurant_created ON orders(restaurant_id, created_at);

-- Order items table
CREATE TABLE order_items (
                             id BIGSERIAL PRIMARY KEY,
                             order_id BIGINT NOT NULL,
                             menu_item_id BIGINT NOT NULL,
                             item_name VARCHAR(200) NOT NULL,
                             quantity INTEGER NOT NULL CHECK (quantity > 0),
                             unit_price DECIMAL(10, 2) NOT NULL,
                             subtotal DECIMAL(10, 2) NOT NULL,
                             special_instructions TEXT,

                             CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
                             CONSTRAINT fk_order_items_menu_item FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE RESTRICT
);

-- Indexes for order items
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_menu_item_id ON order_items(menu_item_id);

-- Order item modifiers table
CREATE TABLE order_item_modifiers (
                                      id BIGSERIAL PRIMARY KEY,
                                      order_item_id BIGINT NOT NULL,
                                      modifier_id BIGINT NOT NULL,
                                      modifier_name VARCHAR(200) NOT NULL,
                                      price DECIMAL(10, 2) NOT NULL,

                                      CONSTRAINT fk_order_item_modifiers_order_item FOREIGN KEY (order_item_id) REFERENCES order_items(id) ON DELETE CASCADE,
                                      CONSTRAINT fk_order_item_modifiers_modifier FOREIGN KEY (modifier_id) REFERENCES modifiers(id) ON DELETE RESTRICT
);

-- Indexes for order item modifiers
CREATE INDEX idx_order_item_modifiers_order_item_id ON order_item_modifiers(order_item_id);

-- Order status history table
CREATE TABLE order_status_history (
                                      id BIGSERIAL PRIMARY KEY,
                                      order_id BIGINT NOT NULL,
                                      from_status VARCHAR(20),
                                      to_status VARCHAR(20) NOT NULL,
                                      changed_by BIGINT NOT NULL,
                                      notes TEXT,
                                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                      CONSTRAINT fk_order_status_history_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
                                      CONSTRAINT fk_order_status_history_changed_by FOREIGN KEY (changed_by) REFERENCES users(id) ON DELETE RESTRICT
);

-- Indexes for order status history
CREATE INDEX idx_order_status_history_order_id ON order_status_history(order_id);
CREATE INDEX idx_order_status_history_created_at ON order_status_history(created_at);

-- Trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_orders_updated_at()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_orders_updated_at
    BEFORE UPDATE ON orders
    FOR EACH ROW
EXECUTE FUNCTION update_orders_updated_at();

-- Comments for documentation
COMMENT ON TABLE orders IS 'Stores all customer orders for dine-in, takeaway, and delivery';
COMMENT ON TABLE order_items IS 'Individual items in each order with quantity and pricing snapshot';
COMMENT ON TABLE order_item_modifiers IS 'Modifiers applied to order items with pricing snapshot';
COMMENT ON TABLE order_status_history IS 'Audit trail of order status changes';

COMMENT ON COLUMN orders.order_number IS 'Unique order identifier visible to customers';
COMMENT ON COLUMN orders.order_type IS 'Type of order: DINE_IN, TAKEAWAY, or DELIVERY';
COMMENT ON COLUMN orders.status IS 'Current order status in lifecycle';
COMMENT ON COLUMN orders.table_number IS 'Table number for dine-in orders';
COMMENT ON COLUMN orders.delivery_address IS 'Delivery address for delivery orders';
COMMENT ON COLUMN orders.subtotal IS 'Total before tax and fees';
COMMENT ON COLUMN orders.tax_amount IS 'Calculated tax amount';
COMMENT ON COLUMN orders.delivery_fee IS 'Delivery fee for delivery orders';
COMMENT ON COLUMN orders.discount_amount IS 'Discount applied to order';
COMMENT ON COLUMN orders.total_amount IS 'Final order amount';
COMMENT ON COLUMN orders.estimated_ready_time IS 'Estimated time when order will be ready';
COMMENT ON COLUMN orders.actual_ready_time IS 'Actual time when order was marked ready';