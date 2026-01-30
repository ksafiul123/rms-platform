-- V10__create_notification_system.sql

-- Notification Templates
CREATE TABLE notification_templates (
                                        id BIGSERIAL PRIMARY KEY,
                                        name VARCHAR(100) NOT NULL UNIQUE,
                                        code VARCHAR(50) NOT NULL UNIQUE,
                                        channel VARCHAR(20) NOT NULL,
                                        type VARCHAR(20) NOT NULL,
                                        subject VARCHAR(200),
                                        content TEXT NOT NULL,
                                        html_content TEXT,
                                        image_url VARCHAR(500),
                                        action_url VARCHAR(500),
                                        action_text VARCHAR(100),
                                        is_active BOOLEAN NOT NULL DEFAULT TRUE,
                                        priority INTEGER NOT NULL DEFAULT 0,
                                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                        CONSTRAINT chk_channel CHECK (channel IN ('EMAIL', 'WHATSAPP', 'PUSH', 'SMS')),
                                        CONSTRAINT chk_type CHECK (type IN ('TRANSACTIONAL', 'PROMOTIONAL', 'ALERT', 'INFORMATIONAL'))
);

CREATE INDEX idx_template_code ON notification_templates(code);
CREATE INDEX idx_template_channel ON notification_templates(channel);
CREATE INDEX idx_template_active ON notification_templates(is_active);

-- Notification Logs
CREATE TABLE notification_logs (
                                   id BIGSERIAL PRIMARY KEY,
                                   user_id BIGINT REFERENCES users(id),
                                   template_id BIGINT REFERENCES notification_templates(id),
                                   channel VARCHAR(20) NOT NULL,
                                   type VARCHAR(20) NOT NULL,
                                   recipient VARCHAR(200) NOT NULL,
                                   subject VARCHAR(200),
                                   content TEXT NOT NULL,
                                   metadata TEXT,
                                   status VARCHAR(20) NOT NULL,
                                   sent_at TIMESTAMP,
                                   delivered_at TIMESTAMP,
                                   read_at TIMESTAMP,
                                   failed_at TIMESTAMP,
                                   error_message TEXT,
                                   retry_count INTEGER NOT NULL DEFAULT 0,
                                   max_retries INTEGER NOT NULL DEFAULT 3,
                                   provider_message_id VARCHAR(200),
                                   order_id BIGINT REFERENCES orders(id),
                                   restaurant_id BIGINT REFERENCES restaurants(id),
                                   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                   CONSTRAINT chk_notification_status CHECK (
                                       status IN ('PENDING', 'SENT', 'DELIVERED', 'READ', 'FAILED', 'CANCELLED')
                                       )
);

CREATE INDEX idx_notification_user ON notification_logs(user_id);
CREATE INDEX idx_notification_status ON notification_logs(status);
CREATE INDEX idx_notification_sent_at ON notification_logs(sent_at);
CREATE INDEX idx_notification_channel ON notification_logs(channel);
CREATE INDEX idx_notification_order ON notification_logs(order_id);
CREATE INDEX idx_notification_restaurant ON notification_logs(restaurant_id);

-- User Notification Preferences
CREATE TABLE user_notification_preferences (
                                               id BIGSERIAL PRIMARY KEY,
                                               user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,

    -- Email preferences
                                               email_enabled BOOLEAN NOT NULL DEFAULT TRUE,
                                               email_order_updates BOOLEAN NOT NULL DEFAULT TRUE,
                                               email_promotional BOOLEAN NOT NULL DEFAULT TRUE,
                                               email_newsletter BOOLEAN NOT NULL DEFAULT FALSE,

    -- WhatsApp preferences
                                               whatsapp_enabled BOOLEAN NOT NULL DEFAULT TRUE,
                                               whatsapp_order_updates BOOLEAN NOT NULL DEFAULT TRUE,
                                               whatsapp_promotional BOOLEAN NOT NULL DEFAULT FALSE,

    -- Push preferences
                                               push_enabled BOOLEAN NOT NULL DEFAULT TRUE,
                                               push_order_updates BOOLEAN NOT NULL DEFAULT TRUE,
                                               push_promotional BOOLEAN NOT NULL DEFAULT TRUE,
                                               push_deals BOOLEAN NOT NULL DEFAULT TRUE,

    -- SMS preferences
                                               sms_enabled BOOLEAN NOT NULL DEFAULT FALSE,

    -- Quiet hours
                                               quiet_hours_enabled BOOLEAN NOT NULL DEFAULT FALSE,
                                               quiet_hours_start VARCHAR(5),
                                               quiet_hours_end VARCHAR(5),

                                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                               updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_pref_user ON user_notification_preferences(user_id);

-- Promotional Campaigns
CREATE TABLE promotional_campaigns (
                                       id BIGSERIAL PRIMARY KEY,
                                       restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
                                       name VARCHAR(200) NOT NULL,
                                       description TEXT,
                                       campaign_type VARCHAR(30) NOT NULL,
                                       target_audience VARCHAR(30) NOT NULL,
                                       channels VARCHAR(100),
                                       template_id BIGINT REFERENCES notification_templates(id),
                                       discount_code VARCHAR(50),
                                       discount_percentage INTEGER,
                                       discount_amount DECIMAL(10,2),
                                       scheduled_at TIMESTAMP,
                                       start_date TIMESTAMP NOT NULL,
                                       end_date TIMESTAMP NOT NULL,
                                       status VARCHAR(20) NOT NULL,
                                       total_sent INTEGER DEFAULT 0,
                                       total_delivered INTEGER DEFAULT 0,
                                       total_read INTEGER DEFAULT 0,
                                       total_conversions INTEGER DEFAULT 0,
                                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                       CONSTRAINT chk_campaign_type CHECK (
                                           campaign_type IN ('FLASH_DEAL', 'SEASONAL_OFFER', 'NEW_MENU_LAUNCH',
                                                             'CUSTOMER_RETENTION', 'CART_ABANDONMENT',
                                                             'BIRTHDAY_SPECIAL', 'LOYALTY_REWARD')
                                           ),
                                       CONSTRAINT chk_target_audience CHECK (
                                           target_audience IN ('ALL_CUSTOMERS', 'NEW_CUSTOMERS', 'REGULAR_CUSTOMERS',
                                                               'VIP_CUSTOMERS', 'INACTIVE_CUSTOMERS', 'CUSTOM_SEGMENT')
                                           ),
                                       CONSTRAINT chk_campaign_status CHECK (
                                           status IN ('DRAFT', 'SCHEDULED', 'RUNNING', 'COMPLETED', 'PAUSED', 'CANCELLED')
                                           )
);

CREATE INDEX idx_campaign_restaurant ON promotional_campaigns(restaurant_id);
CREATE INDEX idx_campaign_status ON promotional_campaigns(status);
CREATE INDEX idx_campaign_scheduled ON promotional_campaigns(scheduled_at);

-- Push Notification Devices
CREATE TABLE push_notification_devices (
                                           id BIGSERIAL PRIMARY KEY,
                                           user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                           device_token VARCHAR(500) NOT NULL UNIQUE,
                                           device_type VARCHAR(20) NOT NULL,
                                           device_model VARCHAR(100),
                                           os_version VARCHAR(50),
                                           app_version VARCHAR(50),
                                           is_active BOOLEAN NOT NULL DEFAULT TRUE,
                                           last_used_at TIMESTAMP,
                                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                           updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                           CONSTRAINT chk_device_type CHECK (device_type IN ('ANDROID', 'IOS', 'WEB'))
);

CREATE INDEX idx_device_user ON push_notification_devices(user_id);
CREATE INDEX idx_device_token ON push_notification_devices(device_token);
CREATE INDEX idx_device_active ON push_notification_devices(is_active);

-- Seed notification templates
INSERT INTO notification_templates (name, code, channel, type, subject, content, is_active, priority) VALUES
-- Order Email Templates
('Order Placed Email', 'ORDER_PLACED', 'EMAIL', 'TRANSACTIONAL',
 'Order Confirmed - {{orderNumber}}',
 'Hi {{customerName}},

Your order #{{orderNumber}} has been placed successfully!

Order Details:
- Items: {{itemCount}} items
- Total: {{currency}}{{totalAmount}}
- Estimated Time: {{estimatedTime}} minutes

Track your order: {{trackingUrl}}

Thank you for choosing {{restaurantName}}!',
 true, 10),

('Order Ready Email', 'ORDER_READY', 'EMAIL', 'TRANSACTIONAL',
 'Your Order is Ready! - {{orderNumber}}',
 'Great news, {{customerName}}!

Your order #{{orderNumber}} is ready for {{orderType}}!

{{additionalInfo}}

Thank you!',
 true, 10),

-- Order WhatsApp Templates
('Order Placed WhatsApp', 'ORDER_PLACED', 'WHATSAPP', 'TRANSACTIONAL',
 NULL,
 '🎉 Order Confirmed!

Hi {{customerName}}, your order #{{orderNumber}} has been placed.

Total: {{currency}}{{totalAmount}}
Estimated time: {{estimatedTime}} mins

Track: {{trackingUrl}}',
 true, 10),

('Order Ready WhatsApp', 'ORDER_READY', 'WHATSAPP', 'TRANSACTIONAL',
 NULL,
 '✨ Your order #{{orderNumber}} is ready!

{{orderType}} at {{restaurantName}}

Thank you! 😊',
 true, 10),

-- Order Push Notifications
('Order Placed Push', 'ORDER_PLACED', 'PUSH', 'TRANSACTIONAL',
 'Order Confirmed! 🎉',
 'Your order #{{orderNumber}} has been placed. Estimated time: {{estimatedTime}} mins',
 true, 10),

('Order Preparing Push', 'ORDER_PREPARING', 'PUSH', 'TRANSACTIONAL',
 'Order is being prepared 👨‍🍳',
 'Your delicious food is being prepared by our chefs',
 true, 10),

('Order Ready Push', 'ORDER_READY', 'PUSH', 'TRANSACTIONAL',
 'Order Ready! ✨',
 'Your order #{{orderNumber}} is ready for {{orderType}}',
 true, 10),

('Order Delivered Push', 'ORDER_DELIVERED', 'PUSH', 'TRANSACTIONAL',
 'Order Delivered! 🎊',
 'Your order has been delivered. Enjoy your meal!',
 true, 10),

-- Promotional Templates
('Flash Deal Email', 'FLASH_DEAL', 'EMAIL', 'PROMOTIONAL',
 '⚡ Flash Deal! {{discountPercentage}}% OFF',
 'Hi {{customerName}},

Don''t miss out! Get {{discountPercentage}}% off on all orders!

Use code: {{discountCode}}
Valid until: {{expiryTime}}

Order now: {{orderUrl}}',
 true, 5),

('New Menu Launch Push', 'NEW_MENU', 'PUSH', 'PROMOTIONAL',
 'New on the Menu! 🍽️',
 'Check out our latest {{categoryName}} items',
 true, 5);

-- Views for analytics
CREATE OR REPLACE VIEW notification_statistics AS
SELECT
    DATE(sent_at) as date,
    channel,
    type,
    status,
    COUNT(*) as count
FROM notification_logs
WHERE sent_at IS NOT NULL
GROUP BY DATE(sent_at), channel, type, status
ORDER BY date DESC, channel, type;

CREATE OR REPLACE VIEW campaign_performance AS
SELECT
    c.id,
    c.name,
    c.campaign_type,
    c.status,
    c.total_sent,
    c.total_delivered,
    c.total_read,
    c.total_conversions,
    ROUND((c.total_delivered::DECIMAL / NULLIF(c.total_sent, 0)) * 100, 2) as delivery_rate,
    ROUND((c.total_read::DECIMAL / NULLIF(c.total_delivered, 0)) * 100, 2) as open_rate,
    ROUND((c.total_conversions::DECIMAL / NULLIF(c.total_sent, 0)) * 100, 2) as conversion_rate,
    c.start_date,
    c.end_date
FROM promotional_campaigns c
ORDER BY c.created_at DESC;

CREATE OR REPLACE VIEW user_notification_summary AS
SELECT
    u.id as user_id,
    u.full_name,
    u.email,
    u.phone,
    COUNT(DISTINCT nl.id) as total_notifications,
    SUM(CASE WHEN nl.channel = 'EMAIL' THEN 1 ELSE 0 END) as email_count,
    SUM(CASE WHEN nl.channel = 'WHATSAPP' THEN 1 ELSE 0 END) as whatsapp_count,
    SUM(CASE WHEN nl.channel = 'PUSH' THEN 1 ELSE 0 END) as push_count,
    SUM(CASE WHEN nl.status = 'DELIVERED' THEN 1 ELSE 0 END) as delivered_count,
    SUM(CASE WHEN nl.status = 'READ' THEN 1 ELSE 0 END) as read_count
FROM users u
         LEFT JOIN notification_logs nl ON u.id = nl.user_id
GROUP BY u.id, u.full_name, u.email, u.phone;

COMMENT ON TABLE notification_templates IS 'Pre-defined notification templates';
COMMENT ON TABLE notification_logs IS 'Complete log of all notifications sent';
COMMENT ON TABLE user_notification_preferences IS 'User preferences for notifications';
COMMENT ON TABLE promotional_campaigns IS 'Marketing campaigns';
COMMENT ON TABLE push_notification_devices IS 'User devices for push notifications';