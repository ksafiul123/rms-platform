-- =====================================================
-- REMAINING MODULES OPTIMIZATION (5-16)
-- =====================================================

-- ============== MODULE 5: ORDER MANAGEMENT ==============

ALTER TABLE orders
    ADD CONSTRAINT fk_orders_customer
        FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_orders_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_orders_table_session
        FOREIGN KEY (table_session_id) REFERENCES table_sessions(id) ON DELETE SET NULL,
    ADD CONSTRAINT chk_orders_amounts
        CHECK (
            subtotal >= 0 AND
            tax_amount >= 0 AND
            discount_amount >= 0 AND
            delivery_fee >= 0 AND
            tip_amount >= 0 AND
            total_amount >= 0
            ),
    ADD CONSTRAINT chk_orders_total_calculation
        CHECK (
            total_amount = subtotal + tax_amount + delivery_fee + tip_amount - discount_amount
            );

CREATE UNIQUE INDEX IF NOT EXISTS idx_orders_number
    ON orders (order_number);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_orders_restaurant_status
    ON orders (restaurant_id, status, created_at DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_orders_customer
    ON orders (customer_id, created_at DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_orders_table_session
    ON orders (table_session_id)
    WHERE table_session_id IS NOT NULL;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_orders_type_status
    ON orders (order_type, status, created_at DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_orders_payment_status
    ON orders (payment_status, created_at DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_orders_active
    ON orders (restaurant_id, status)
    WHERE status IN ('PENDING', 'CONFIRMED', 'PREPARING', 'READY');
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_orders_date_range
    ON orders (restaurant_id, created_at DESC)
    INCLUDE (total_amount, status);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_orders_delivery
    ON orders (restaurant_id, order_type, status)
    WHERE order_type = 'DELIVERY';

-- Partition orders by month for scale
-- CREATE TABLE orders_partitioned (LIKE orders INCLUDING ALL)
-- PARTITION BY RANGE (created_at);

-- ============== ORDER_ITEMS ==============

ALTER TABLE order_items
    ADD CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_order_items_menu_item
        FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE RESTRICT,
    ADD CONSTRAINT fk_order_items_variant
        FOREIGN KEY (variant_id) REFERENCES item_variants(id) ON DELETE SET NULL,
    ADD CONSTRAINT chk_order_items_quantity
        CHECK (quantity > 0),
    ADD CONSTRAINT chk_order_items_amounts
        CHECK (price >= 0 AND subtotal >= 0);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_order_items_order
    ON order_items (order_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_order_items_menu_item
    ON order_items (menu_item_id, created_at DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_order_items_order_menu
    ON order_items (order_id, menu_item_id);

-- ============== ORDER_ITEM_MODIFIERS ==============

ALTER TABLE order_item_modifiers
    ADD CONSTRAINT fk_order_item_modifiers_order_item
        FOREIGN KEY (order_item_id) REFERENCES order_items(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_order_item_modifiers_modifier_option
        FOREIGN KEY (modifier_option_id) REFERENCES modifier_options(id) ON DELETE RESTRICT;

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_order_item_modifiers_order_item
    ON order_item_modifiers (order_item_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_order_item_modifiers_option
    ON order_item_modifiers (modifier_option_id);

-- ============== MODULE 6: TABLE SESSIONS (QR ORDERING) ==============

ALTER TABLE tables
    ADD CONSTRAINT fk_tables_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE,
    ADD CONSTRAINT chk_tables_capacity
        CHECK (capacity > 0);

CREATE UNIQUE INDEX IF NOT EXISTS idx_tables_qr_code
    ON tables (qr_code);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_tables_restaurant
    ON tables (restaurant_id, is_active);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_tables_status
    ON tables (restaurant_id, status)
    WHERE is_active = true;

ALTER TABLE table_sessions
    ADD CONSTRAINT fk_table_sessions_table
        FOREIGN KEY (table_id) REFERENCES tables(id) ON DELETE CASCADE,
    ADD CONSTRAINT chk_table_sessions_amount
        CHECK (total_amount >= 0),
    ADD CONSTRAINT chk_table_sessions_guest_count
        CHECK (guest_count >= 0);

CREATE UNIQUE INDEX IF NOT EXISTS idx_table_sessions_code
    ON table_sessions (session_code);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_table_sessions_table_active
    ON table_sessions (table_id, status)
    WHERE status = 'ACTIVE';
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_table_sessions_status
    ON table_sessions (status, created_at DESC);

ALTER TABLE table_session_guests
    ADD CONSTRAINT fk_table_session_guests_session
        FOREIGN KEY (table_session_id) REFERENCES table_sessions(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_table_session_guests_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_table_session_guests_session
    ON table_session_guests (table_session_id, joined_at DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_table_session_guests_user
    ON table_session_guests (user_id)
    WHERE user_id IS NOT NULL;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_table_session_guests_active
    ON table_session_guests (table_session_id)
    WHERE status = 'ACTIVE';

-- ============== MODULE 7: INVENTORY ==============

ALTER TABLE inventory_items
    ADD CONSTRAINT fk_inventory_items_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE,
    ADD CONSTRAINT chk_inventory_quantities
        CHECK (
            current_quantity >= 0 AND
            minimum_quantity >= 0 AND
            (maximum_quantity IS NULL OR maximum_quantity >= minimum_quantity)
            ),
    ADD CONSTRAINT chk_inventory_cost
        CHECK (cost_per_unit >= 0);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_inventory_items_restaurant
    ON inventory_items (restaurant_id, status);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_inventory_items_category
    ON inventory_items (restaurant_id, category);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_inventory_items_low_stock
    ON inventory_items (restaurant_id)
    WHERE current_quantity <= minimum_quantity AND status = 'IN_STOCK';
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_inventory_items_code
    ON inventory_items (restaurant_id, item_code);

-- Full-text search
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_inventory_items_search
    ON inventory_items USING gin (
                                  to_tsvector('english', name || ' ' || COALESCE(description, ''))
        );

ALTER TABLE stock_transactions
    ADD CONSTRAINT fk_stock_transactions_inventory
        FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_stock_transactions_order
        FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_stock_transactions_performed_by
        FOREIGN KEY (performed_by) REFERENCES users(id) ON DELETE SET NULL,
    ADD CONSTRAINT chk_stock_transactions_quantity
        CHECK (quantity != 0);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_stock_transactions_inventory
    ON stock_transactions (inventory_item_id, transaction_timestamp DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_stock_transactions_type
    ON stock_transactions (transaction_type, transaction_timestamp DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_stock_transactions_order
    ON stock_transactions (order_id)
    WHERE order_id IS NOT NULL;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_stock_transactions_date_range
    ON stock_transactions (inventory_item_id, transaction_timestamp DESC)
    INCLUDE (quantity, total_cost);

ALTER TABLE menu_item_inventory
    ADD CONSTRAINT fk_menu_item_inventory_menu_item
        FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_menu_item_inventory_inventory_item
        FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(id) ON DELETE CASCADE,
    ADD CONSTRAINT chk_menu_item_inventory_quantity
        CHECK (quantity_required > 0);

CREATE UNIQUE INDEX IF NOT EXISTS idx_menu_item_inventory_unique
    ON menu_item_inventory (menu_item_id, inventory_item_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_menu_item_inventory_menu
    ON menu_item_inventory (menu_item_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_menu_item_inventory_item
    ON menu_item_inventory (inventory_item_id);

ALTER TABLE low_stock_alerts
    ADD CONSTRAINT fk_low_stock_alerts_inventory
        FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_low_stock_alerts_acknowledged_by
        FOREIGN KEY (acknowledged_by) REFERENCES users(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_low_stock_alerts_resolved_by
        FOREIGN KEY (resolved_by) REFERENCES users(id) ON DELETE SET NULL;

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_low_stock_alerts_inventory
    ON low_stock_alerts (inventory_item_id, created_at DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_low_stock_alerts_active
    ON low_stock_alerts (inventory_item_id, status)
    WHERE status = 'ACTIVE';
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_low_stock_alerts_type
    ON low_stock_alerts (alert_type, status);

-- ============== MODULE 8: CUSTOMER PREFERENCES ==============

ALTER TABLE customer_preferences
    ADD CONSTRAINT fk_customer_preferences_customer
        FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE;

CREATE UNIQUE INDEX IF NOT EXISTS idx_customer_preferences_customer
    ON customer_preferences (customer_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_customer_preferences_visible
    ON customer_preferences (visible_to_chefs);

ALTER TABLE favorite_menu_items
    ADD CONSTRAINT fk_favorite_menu_items_customer
        FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_favorite_menu_items_menu_item
        FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE,
    ADD CONSTRAINT chk_favorite_order_count
        CHECK (order_count >= 0);

CREATE UNIQUE INDEX IF NOT EXISTS idx_favorite_menu_items_unique
    ON favorite_menu_items (customer_id, menu_item_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_favorite_menu_items_customer
    ON favorite_menu_items (customer_id, created_at DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_favorite_menu_items_menu
    ON favorite_menu_items (menu_item_id);

ALTER TABLE menu_item_preferences
    ADD CONSTRAINT fk_menu_item_preferences_customer
        FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_menu_item_preferences_menu_item
        FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE;

CREATE UNIQUE INDEX IF NOT EXISTS idx_menu_item_preferences_unique
    ON menu_item_preferences (customer_id, menu_item_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_menu_item_preferences_customer
    ON menu_item_preferences (customer_id);

-- ============== MODULE 9: LIVE ORDER TRACKING ==============

ALTER TABLE order_status_history
    ADD CONSTRAINT fk_order_status_history_order
        FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_order_status_history_updated_by
        FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL;

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_order_status_history_order
    ON order_status_history (order_id, timestamp DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_order_status_history_status
    ON order_status_history (status, timestamp DESC);

-- Partition by month for high volume
-- CREATE TABLE order_status_history_partitioned (
--     LIKE order_status_history INCLUDING ALL
-- ) PARTITION BY RANGE (timestamp);

ALTER TABLE kitchen_order_items
    ADD CONSTRAINT fk_kitchen_order_items_order
        FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_kitchen_order_items_order_item
        FOREIGN KEY (order_item_id) REFERENCES order_items(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_kitchen_order_items_assigned_to
        FOREIGN KEY (assigned_to) REFERENCES users(id) ON DELETE SET NULL,
    ADD CONSTRAINT chk_kitchen_order_items_priority
        CHECK (priority >= 0);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_kitchen_order_items_order
    ON kitchen_order_items (order_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_kitchen_order_items_status
    ON kitchen_order_items (status, created_at DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_kitchen_order_items_assigned
    ON kitchen_order_items (assigned_to)
    WHERE assigned_to IS NOT NULL AND status != 'COMPLETED';

ALTER TABLE delivery_assignments
    ADD CONSTRAINT fk_delivery_assignments_order
        FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_delivery_assignments_partner
        FOREIGN KEY (delivery_partner_id) REFERENCES users(id) ON DELETE RESTRICT;

CREATE UNIQUE INDEX IF NOT EXISTS idx_delivery_assignments_order
    ON delivery_assignments (order_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_delivery_assignments_partner
    ON delivery_assignments (delivery_partner_id, status);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_delivery_assignments_active
    ON delivery_assignments (status)
    WHERE status IN ('ASSIGNED', 'ACCEPTED', 'PICKED_UP', 'IN_TRANSIT');

-- Geospatial index for location tracking
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_delivery_assignments_location
    ON delivery_assignments USING gist (
                                        ll_to_earth(current_latitude, current_longitude)
        ) WHERE current_latitude IS NOT NULL;

ALTER TABLE order_timeline
    ADD CONSTRAINT fk_order_timeline_order
        FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE;

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_order_timeline_order
    ON order_timeline (order_id, timestamp DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_order_timeline_event
    ON order_timeline (event_type, timestamp DESC);

ALTER TABLE order_preparation_metrics
    ADD CONSTRAINT fk_order_preparation_metrics_order
        FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    ADD CONSTRAINT chk_metrics_on_time
        CHECK (
            delay_minutes IS NULL OR
            (was_on_time = false AND delay_minutes > 0)
            );

CREATE UNIQUE INDEX IF NOT EXISTS idx_order_preparation_metrics_order
    ON order_preparation_metrics (order_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_order_preparation_metrics_performance
    ON order_preparation_metrics (was_on_time, created_at DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_order_preparation_metrics_delay
    ON order_preparation_metrics (delay_minutes DESC)
    WHERE delay_minutes IS NOT NULL;

-- ============== MODULE 10: NOTIFICATIONS ==============

ALTER TABLE notification_templates
    ADD CONSTRAINT chk_notification_templates_priority
        CHECK (priority >= 0);

CREATE UNIQUE INDEX IF NOT EXISTS idx_notification_templates_code
    ON notification_templates (code);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_notification_templates_channel
    ON notification_templates (channel, is_active);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_notification_templates_type
    ON notification_templates (type, is_active);

ALTER TABLE notification_logs
    ADD CONSTRAINT fk_notification_logs_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_notification_logs_template
        FOREIGN KEY (template_id) REFERENCES notification_templates(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_notification_logs_order
        FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_notification_logs_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE SET NULL,
    ADD CONSTRAINT chk_notification_logs_retry
        CHECK (retry_count <= max_retries);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_notification_logs_user
    ON notification_logs (user_id, sent_at DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_notification_logs_status
    ON notification_logs (status, created_at DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_notification_logs_channel
    ON notification_logs (channel, sent_at DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_notification_logs_failed
    ON notification_logs (status, retry_count)
    WHERE status = 'FAILED' AND retry_count < max_retries;

-- Partition by month
-- CREATE TABLE notification_logs_partitioned (
--     LIKE notification_logs INCLUDING ALL
-- ) PARTITION BY RANGE (created_at);

ALTER TABLE user_notification_preferences
    ADD CONSTRAINT fk_user_notification_preferences_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

CREATE UNIQUE INDEX IF NOT EXISTS idx_user_notification_preferences_user
    ON user_notification_preferences (user_id);

ALTER TABLE push_notification_devices
    ADD CONSTRAINT fk_push_notification_devices_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

CREATE UNIQUE INDEX IF NOT EXISTS idx_push_notification_devices_token
    ON push_notification_devices (device_token);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_push_notification_devices_user
    ON push_notification_devices (user_id, is_active);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_push_notification_devices_active
    ON push_notification_devices (is_active, last_used_at DESC)
    WHERE is_active = true;

-- ============== MODULE 11: WAITING GAMES & ENGAGEMENT ==============

ALTER TABLE game_types
    ADD CONSTRAINT chk_game_types_players
        CHECK (min_players > 0 AND max_players >= min_players),
    ADD CONSTRAINT chk_game_types_duration
        CHECK (average_duration_seconds > 0),
    ADD CONSTRAINT chk_game_types_points
        CHECK (base_points_per_game >= 0),
    ADD CONSTRAINT chk_game_types_difficulty
        CHECK (difficulty_level BETWEEN 1 AND 5);

CREATE UNIQUE INDEX IF NOT EXISTS idx_game_types_code
    ON game_types (code);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_game_types_active
    ON game_types (is_active)
    WHERE is_active = true;

ALTER TABLE game_sessions
    ADD CONSTRAINT fk_game_sessions_table_session
        FOREIGN KEY (table_session_id) REFERENCES table_sessions(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_game_sessions_game_type
        FOREIGN KEY (game_type_id) REFERENCES game_types(id) ON DELETE RESTRICT,
    ADD CONSTRAINT fk_game_sessions_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE,
    ADD CONSTRAINT chk_game_sessions_rounds
        CHECK (current_round > 0 AND current_round <= total_rounds),
    ADD CONSTRAINT chk_game_sessions_points
        CHECK (total_points_awarded >= 0);

CREATE UNIQUE INDEX IF NOT EXISTS idx_game_sessions_code
    ON game_sessions (session_code);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_game_sessions_table
    ON game_sessions (table_session_id, status);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_game_sessions_active
    ON game_sessions (restaurant_id, status)
    WHERE status IN ('WAITING', 'IN_PROGRESS');

ALTER TABLE game_players
    ADD CONSTRAINT fk_game_players_session
        FOREIGN KEY (game_session_id) REFERENCES game_sessions(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_game_players_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    ADD CONSTRAINT chk_game_players_scores
        CHECK (current_score >= 0 AND (final_score IS NULL OR final_score >= 0)),
    ADD CONSTRAINT chk_game_players_accuracy
        CHECK (accuracy_percentage IS NULL OR (accuracy_percentage >= 0 AND accuracy_percentage <= 100));

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_game_players_session
    ON game_players (game_session_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_game_players_user
    ON game_players (user_id)
    WHERE user_id IS NOT NULL;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_game_players_active
    ON game_players (game_session_id, is_active)
    WHERE is_active = true;

ALTER TABLE discount_rewards
    ADD CONSTRAINT fk_discount_rewards_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_discount_rewards_game_session
        FOREIGN KEY (game_session_id) REFERENCES game_sessions(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_discount_rewards_order
        FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_discount_rewards_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE,
    ADD CONSTRAINT chk_discount_rewards_amounts
        CHECK (
            (discount_percentage IS NULL OR discount_percentage > 0) AND
            (discount_amount IS NULL OR discount_amount > 0)
            ),
    ADD CONSTRAINT chk_discount_rewards_points
        CHECK (points_earned >= 0);

CREATE UNIQUE INDEX IF NOT EXISTS idx_discount_rewards_code
    ON discount_rewards (reward_code);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_discount_rewards_user
    ON discount_rewards (user_id, status);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_discount_rewards_active
    ON discount_rewards (user_id, expires_at)
    WHERE status = 'ACTIVE' AND expires_at > NOW();

-- ============== MODULE 12: PAYMENTS ==============

ALTER TABLE payments
    ADD CONSTRAINT fk_payments_order
        FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_payments_customer
        FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_payments_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE,
    ADD CONSTRAINT chk_payments_amount
        CHECK (amount > 0),
    ADD CONSTRAINT chk_payments_refund
        CHECK (refund_amount >= 0 AND refund_amount <= amount);

CREATE UNIQUE INDEX IF NOT EXISTS idx_payments_reference
    ON payments (payment_reference);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_payments_order
    ON payments (order_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_payments_customer
    ON payments (customer_id, created_at DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_payments_restaurant
    ON payments (restaurant_id, status, created_at DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_payments_status
    ON payments (status, created_at DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_payments_method
    ON payments (payment_method, created_at DESC);

ALTER TABLE payment_transactions
    ADD CONSTRAINT fk_payment_transactions_payment
        FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE CASCADE,
    ADD CONSTRAINT chk_payment_transactions_amount
        CHECK (amount != 0);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_payment_transactions_payment
    ON payment_transactions (payment_id, transaction_timestamp DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_payment_transactions_type
    ON payment_transactions (transaction_type, status);

ALTER TABLE customer_wallets
    ADD CONSTRAINT fk_customer_wallets_customer
        FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_customer_wallets_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE,
    ADD CONSTRAINT chk_customer_wallets_balance
        CHECK (balance >= 0);

CREATE UNIQUE INDEX IF NOT EXISTS idx_customer_wallets_customer
    ON customer_wallets (customer_id, restaurant_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_customer_wallets_active
    ON customer_wallets (customer_id)
    WHERE is_active = true AND is_locked = false;

ALTER TABLE wallet_transactions
    ADD CONSTRAINT fk_wallet_transactions_wallet
        FOREIGN KEY (wallet_id) REFERENCES customer_wallets(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_wallet_transactions_order
        FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_wallet_transactions_payment
        FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE SET NULL,
    ADD CONSTRAINT chk_wallet_transactions_amount
        CHECK (amount != 0);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_wallet_transactions_wallet
    ON wallet_transactions (wallet_id, transaction_timestamp DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_wallet_transactions_type
    ON wallet_transactions (transaction_type, transaction_timestamp DESC);

-- ============== MODULE 13: SETTLEMENTS & PAYOUTS ==============

ALTER TABLE order_settlements
    ADD CONSTRAINT fk_order_settlements_order
        FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_order_settlements_payment
        FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_order_settlements_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_order_settlements_payout
        FOREIGN KEY (payout_id) REFERENCES restaurant_payouts(id) ON DELETE SET NULL,
    ADD CONSTRAINT chk_order_settlements_amounts
        CHECK (
            order_amount >= 0 AND
            payment_amount >= 0 AND
            commission_amount >= 0 AND
            net_amount >= 0
            );

CREATE UNIQUE INDEX IF NOT EXISTS idx_order_settlements_reference
    ON order_settlements (settlement_reference);
CREATE UNIQUE INDEX IF NOT EXISTS idx_order_settlements_order
    ON order_settlements (order_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_order_settlements_restaurant
    ON order_settlements (restaurant_id, settlement_status, settlement_date DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_order_settlements_pending
    ON order_settlements (restaurant_id)
    WHERE settlement_status = 'PENDING';
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_order_settlements_payout
    ON order_settlements (payout_id)
    WHERE payout_id IS NOT NULL;

ALTER TABLE restaurant_payouts
    ADD CONSTRAINT fk_restaurant_payouts_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_restaurant_payouts_initiated_by
        FOREIGN KEY (initiated_by) REFERENCES users(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_restaurant_payouts_approved_by
        FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_restaurant_payouts_processed_by
        FOREIGN KEY (processed_by) REFERENCES users(id) ON DELETE SET NULL,
    ADD CONSTRAINT chk_restaurant_payouts_dates
        CHECK (period_end_date >= period_start_date),
    ADD CONSTRAINT chk_restaurant_payouts_amounts
        CHECK (
            total_orders >= 0 AND
            payout_amount >= 0
            );

CREATE UNIQUE INDEX IF NOT EXISTS idx_restaurant_payouts_reference
    ON restaurant_payouts (payout_reference);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_restaurant_payouts_restaurant
    ON restaurant_payouts (restaurant_id, payout_status, created_at DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_restaurant_payouts_pending
    ON restaurant_payouts (payout_status)
    WHERE payout_status IN ('PENDING_APPROVAL', 'APPROVED');
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_restaurant_payouts_period
    ON restaurant_payouts (restaurant_id, period_start_date, period_end_date);

-- ============== MODULE 14: PUBLIC DISPLAY ==============

ALTER TABLE display_configurations
    ADD CONSTRAINT fk_display_configurations_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE,
    ADD CONSTRAINT chk_display_refresh_interval
        CHECK (refresh_interval_seconds > 0),
    ADD CONSTRAINT chk_display_max_orders
        CHECK (max_orders_display > 0);

CREATE UNIQUE INDEX IF NOT EXISTS idx_display_configurations_restaurant
    ON display_configurations (restaurant_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_display_configurations_token
    ON display_configurations (display_token);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_display_configurations_active
    ON display_configurations (is_active)
    WHERE is_active = true;

ALTER TABLE order_display_snapshots
    ADD CONSTRAINT fk_order_display_snapshots_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_order_display_snapshots_order
        FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    ADD CONSTRAINT chk_order_display_priority
        CHECK (priority >= 0);

CREATE UNIQUE INDEX IF NOT EXISTS idx_order_display_snapshots_order
    ON order_display_snapshots (order_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_order_display_snapshots_restaurant
    ON order_display_snapshots (restaurant_id, display_status, display_position);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_order_display_snapshots_status
    ON order_display_snapshots (display_status, last_updated DESC);

-- ============== MODULE 15: ANALYTICS ==============

ALTER TABLE analytics_snapshots
    ADD CONSTRAINT fk_analytics_snapshots_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE;

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_analytics_snapshots_restaurant
    ON analytics_snapshots (restaurant_id, report_type, snapshot_date DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_analytics_snapshots_cached
    ON analytics_snapshots (is_cached, cache_expires_at)
    WHERE is_cached = true;

ALTER TABLE sales_reports
    ADD CONSTRAINT fk_sales_reports_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE,
    ADD CONSTRAINT chk_sales_reports_amounts
        CHECK (
            total_orders >= 0 AND
            total_revenue >= 0 AND
            net_revenue >= 0
            );

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_sales_reports_restaurant
    ON sales_reports (restaurant_id, report_date DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_sales_reports_period
    ON sales_reports (restaurant_id, period_type, report_date DESC);

ALTER TABLE menu_performance_reports
    ADD CONSTRAINT fk_menu_performance_reports_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_menu_performance_reports_menu_item
        FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_menu_performance_reports_category
        FOREIGN KEY (category_id) REFERENCES menu_categories(id) ON DELETE SET NULL;

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_menu_performance_restaurant
    ON menu_performance_reports (restaurant_id, report_date DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_menu_performance_item
    ON menu_performance_reports (menu_item_id, report_date DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_menu_performance_popularity
    ON menu_performance_reports (restaurant_id, popularity_rank)
    WHERE popularity_rank IS NOT NULL;

-- ============== GLOBAL PERFORMANCE IMPROVEMENTS ==============

-- Enable query parallelization
ALTER SYSTEM SET max_parallel_workers_per_gather = 4;
ALTER SYSTEM SET max_parallel_workers = 8;

-- Optimize autovacuum
ALTER SYSTEM SET autovacuum_vacuum_scale_factor = 0.1;
ALTER SYSTEM SET autovacuum_analyze_scale_factor = 0.05;

-- Connection pooling optimization
ALTER SYSTEM SET max_connections = 200;
ALTER SYSTEM SET shared_buffers = '4GB';
ALTER SYSTEM SET effective_cache_size = '12GB';
ALTER SYSTEM SET maintenance_work_mem = '1GB';
ALTER SYSTEM SET random_page_cost = 1.1;

-- Enable JIT compilation for complex queries
ALTER SYSTEM SET jit = on;

-- Reload configuration
SELECT pg_reload_conf();

-- ============== MAINTENANCE FUNCTIONS ==============

-- Function to update all statistics
CREATE OR REPLACE FUNCTION update_all_statistics()
    RETURNS void AS $$
BEGIN
    ANALYZE;
    VACUUM ANALYZE;
END;
$$ LANGUAGE plpgsql;

-- Function to rebuild all indexes
CREATE OR REPLACE FUNCTION rebuild_all_indexes()
    RETURNS void AS $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN
        SELECT schemaname, tablename, indexname
        FROM pg_indexes
        WHERE schemaname = 'public'
        LOOP
            EXECUTE 'REINDEX INDEX CONCURRENTLY ' || quote_ident(r.schemaname) || '.' || quote_ident(r.indexname);
        END LOOP;
END;
$$ LANGUAGE plpgsql;

-- Schedule automatic statistics update (call from cron)
-- SELECT cron.schedule('update-stats', '0 2 * * *', 'SELECT update_all_statistics()');

COMMENT ON FUNCTION update_all_statistics() IS 'Updates statistics for all tables';
COMMENT ON FUNCTION rebuild_all_indexes() IS 'Rebuilds all indexes concurrently';