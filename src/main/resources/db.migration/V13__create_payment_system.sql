-- V13__create_payment_system.sql

-- Payments table
CREATE TABLE payments (
                          id BIGSERIAL PRIMARY KEY,
                          order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE RESTRICT,
                          customer_id BIGINT NOT NULL REFERENCES users(id),
                          restaurant_id BIGINT NOT NULL REFERENCES restaurants(id),
                          payment_reference VARCHAR(100) NOT NULL UNIQUE,
                          payment_method VARCHAR(30) NOT NULL,
                          payment_provider VARCHAR(30),
                          amount DECIMAL(10,2) NOT NULL,
                          currency VARCHAR(3) NOT NULL DEFAULT 'BDT',
                          status VARCHAR(20) NOT NULL,
                          provider_transaction_id VARCHAR(200),
                          provider_payment_intent_id VARCHAR(200),
                          provider_response TEXT,
                          initiated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          completed_at TIMESTAMP,
                          failed_at TIMESTAMP,
                          failure_reason VARCHAR(500),
                          refund_amount DECIMAL(10,2) DEFAULT 0,
                          is_refunded BOOLEAN NOT NULL DEFAULT FALSE,
                          customer_phone VARCHAR(20),
                          customer_email VARCHAR(100),
                          ip_address VARCHAR(45),
                          user_agent VARCHAR(500),
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT chk_payment_method CHECK (payment_method IN (
                                                                                  'ONLINE_CARD', 'BKASH', 'NAGAD', 'ROCKET', 'CASH',
                                                                                  'CARD_TERMINAL', 'WALLET', 'SPLIT'
                              )),
                          CONSTRAINT chk_payment_provider CHECK (payment_provider IN (
                                                                                      'STRIPE', 'SSL_COMMERZ', 'BKASH', 'NAGAD', 'ROCKET',
                                                                                      'RAZORPAY', 'INTERNAL', 'MANUAL'
                              )),
                          CONSTRAINT chk_payment_status CHECK (status IN (
                                                                          'PENDING', 'PROCESSING', 'AUTHORIZED', 'COMPLETED', 'FAILED',
                                                                          'CANCELLED', 'REFUND_PENDING', 'REFUNDED', 'PARTIALLY_REFUNDED', 'EXPIRED'
                              )),
                          CONSTRAINT chk_positive_amount CHECK (amount > 0)
);

CREATE INDEX idx_payment_order ON payments(order_id);
CREATE INDEX idx_payment_reference ON payments(payment_reference);
CREATE INDEX idx_payment_status ON payments(status);
CREATE INDEX idx_payment_customer ON payments(customer_id);
CREATE INDEX idx_payment_created ON payments(created_at);
CREATE INDEX idx_payment_provider_txn ON payments(provider_transaction_id);

-- Payment Transactions table
CREATE TABLE payment_transactions (
                                      id BIGSERIAL PRIMARY KEY,
                                      payment_id BIGINT NOT NULL REFERENCES payments(id) ON DELETE CASCADE,
                                      transaction_type VARCHAR(30) NOT NULL,
                                      amount DECIMAL(10,2) NOT NULL,
                                      currency VARCHAR(3) NOT NULL DEFAULT 'BDT',
                                      status VARCHAR(20) NOT NULL,
                                      provider_transaction_id VARCHAR(200),
                                      transaction_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      description VARCHAR(500),
                                      metadata TEXT,
                                      balance_before DECIMAL(10,2),
                                      balance_after DECIMAL(10,2),
                                      fee_amount DECIMAL(10,2) DEFAULT 0,
                                      net_amount DECIMAL(10,2),
                                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                      CONSTRAINT chk_transaction_type CHECK (transaction_type IN (
                                                                                                  'CHARGE', 'AUTHORIZATION', 'CAPTURE', 'REFUND', 'PARTIAL_REFUND',
                                                                                                  'REVERSAL', 'ADJUSTMENT', 'FEE', 'CHARGEBACK'
                                          )),
                                      CONSTRAINT chk_transaction_status CHECK (status IN (
                                                                                          'PENDING', 'SUCCESS', 'FAILED', 'REVERSED', 'EXPIRED'
                                          ))
);

CREATE INDEX idx_transaction_payment ON payment_transactions(payment_id);
CREATE INDEX idx_transaction_timestamp ON payment_transactions(transaction_timestamp);

-- Customer Wallets table
CREATE TABLE customer_wallets (
                                  id BIGSERIAL PRIMARY KEY,
                                  customer_id BIGINT NOT NULL REFERENCES users(id),
                                  restaurant_id BIGINT REFERENCES restaurants(id),
                                  balance DECIMAL(10,2) NOT NULL DEFAULT 0,
                                  currency VARCHAR(3) NOT NULL DEFAULT 'BDT',
                                  total_credited DECIMAL(10,2) DEFAULT 0,
                                  total_debited DECIMAL(10,2) DEFAULT 0,
                                  total_refunded DECIMAL(10,2) DEFAULT 0,
                                  is_active BOOLEAN NOT NULL DEFAULT TRUE,
                                  is_locked BOOLEAN NOT NULL DEFAULT FALSE,
                                  lock_reason VARCHAR(500),
                                  last_transaction_at TIMESTAMP,
                                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                  CONSTRAINT chk_wallet_balance CHECK (balance >= 0),
                                  CONSTRAINT uq_customer_restaurant_wallet UNIQUE (customer_id, restaurant_id)
);

CREATE INDEX idx_wallet_customer ON customer_wallets(customer_id);
CREATE INDEX idx_wallet_restaurant ON customer_wallets(restaurant_id);

-- Wallet Transactions table
CREATE TABLE wallet_transactions (
                                     id BIGSERIAL PRIMARY KEY,
                                     wallet_id BIGINT NOT NULL REFERENCES customer_wallets(id) ON DELETE CASCADE,
                                     order_id BIGINT REFERENCES orders(id),
                                     payment_id BIGINT REFERENCES payments(id),
                                     transaction_type VARCHAR(30) NOT NULL,
                                     amount DECIMAL(10,2) NOT NULL,
                                     balance_before DECIMAL(10,2) NOT NULL,
                                     balance_after DECIMAL(10,2) NOT NULL,
                                     transaction_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     description VARCHAR(500) NOT NULL,
                                     reference_number VARCHAR(100),
                                     metadata TEXT,
                                     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                     CONSTRAINT chk_wallet_txn_type CHECK (transaction_type IN (
                                                                                                'CREDIT', 'DEBIT', 'REFUND', 'CASHBACK', 'REWARD',
                                                                                                'ADJUSTMENT', 'TRANSFER_IN', 'TRANSFER_OUT', 'REVERSAL'
                                         ))
);

CREATE INDEX idx_wallet_txn_wallet ON wallet_transactions(wallet_id);
CREATE INDEX idx_wallet_txn_timestamp ON wallet_transactions(transaction_timestamp);
CREATE INDEX idx_wallet_txn_order ON wallet_transactions(order_id);

-- Payment Refunds table
CREATE TABLE payment_refunds (
                                 id BIGSERIAL PRIMARY KEY,
                                 payment_id BIGINT NOT NULL REFERENCES payments(id) ON DELETE RESTRICT,
                                 refund_reference VARCHAR(100) NOT NULL UNIQUE,
                                 amount DECIMAL(10,2) NOT NULL,
                                 currency VARCHAR(3) NOT NULL DEFAULT 'BDT',
                                 refund_type VARCHAR(30) NOT NULL,
                                 status VARCHAR(20) NOT NULL,
                                 reason VARCHAR(500) NOT NULL,
                                 initiated_by BIGINT REFERENCES users(id),
                                 initiated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 processed_at TIMESTAMP,
                                 provider_refund_id VARCHAR(200),
                                 provider_response TEXT,
                                 refund_method VARCHAR(30),
                                 notes TEXT,
                                 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 CONSTRAINT chk_refund_type CHECK (refund_type IN (
                                                                                   'FULL', 'PARTIAL', 'CANCELLATION', 'DISPUTE', 'GOODWILL'
                                     )),
                                 CONSTRAINT chk_refund_status CHECK (status IN (
                                                                                'PENDING', 'APPROVED', 'PROCESSING', 'COMPLETED',
                                                                                'FAILED', 'REJECTED', 'CANCELLED'
                                     ))
);

CREATE INDEX idx_refund_payment ON payment_refunds(payment_id);
CREATE INDEX idx_refund_status ON payment_refunds(status);
CREATE INDEX idx_refund_reference ON payment_refunds(refund_reference);

-- Payment Webhooks table
CREATE TABLE payment_webhooks (
                                  id BIGSERIAL PRIMARY KEY,
                                  provider VARCHAR(30) NOT NULL,
                                  event_id VARCHAR(200) NOT NULL UNIQUE,
                                  event_type VARCHAR(100) NOT NULL,
                                  payload TEXT NOT NULL,
                                  signature VARCHAR(500),
                                  is_verified BOOLEAN NOT NULL DEFAULT FALSE,
                                  is_processed BOOLEAN NOT NULL DEFAULT FALSE,
                                  received_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  processed_at TIMESTAMP,
                                  processing_error TEXT,
                                  payment_id BIGINT REFERENCES payments(id),
                                  retry_count INTEGER DEFAULT 0,
                                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_webhook_provider ON payment_webhooks(provider);
CREATE INDEX idx_webhook_received ON payment_webhooks(received_at);
CREATE INDEX idx_webhook_event_id ON payment_webhooks(event_id);
CREATE INDEX idx_webhook_processed ON payment_webhooks(is_processed);

-- Payment Split Details table
CREATE TABLE payment_split_details (
                                       id BIGSERIAL PRIMARY KEY,
                                       parent_payment_id BIGINT NOT NULL REFERENCES payments(id) ON DELETE CASCADE,
                                       payment_method VARCHAR(30) NOT NULL,
                                       amount DECIMAL(10,2) NOT NULL,
                                       percentage DECIMAL(5,2),
                                       provider_transaction_id VARCHAR(200),
                                       status VARCHAR(20) NOT NULL,
                                       processed_at TIMESTAMP,
                                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                       CONSTRAINT chk_split_positive_amount CHECK (amount > 0)
);

CREATE INDEX idx_split_payment ON payment_split_details(parent_payment_id);

-- Payment Gateway Configs table
CREATE TABLE payment_gateway_configs (
                                         id BIGSERIAL PRIMARY KEY,
                                         restaurant_id BIGINT NOT NULL REFERENCES restaurants(id),
                                         provider VARCHAR(30) NOT NULL,
                                         is_enabled BOOLEAN NOT NULL DEFAULT FALSE,
                                         is_test_mode BOOLEAN NOT NULL DEFAULT TRUE,
                                         api_key_encrypted VARCHAR(500),
                                         api_secret_encrypted VARCHAR(500),
                                         webhook_secret_encrypted VARCHAR(500),
                                         merchant_id VARCHAR(200),
                                         config_json TEXT,
                                         supported_currencies VARCHAR(100),
                                         transaction_fee_percentage DECIMAL(5,2),
                                         transaction_fee_fixed DECIMAL(10,2),
                                         min_transaction_amount DECIMAL(10,2),
                                         max_transaction_amount DECIMAL(10,2),
                                         last_tested_at TIMESTAMP,
                                         test_status VARCHAR(20),
                                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                         CONSTRAINT uq_restaurant_provider UNIQUE (restaurant_id, provider)
);

CREATE INDEX idx_gateway_restaurant ON payment_gateway_configs(restaurant_id);
CREATE INDEX idx_gateway_provider ON payment_gateway_configs(provider);

-- Analytical Views

CREATE OR REPLACE VIEW payment_analytics AS
SELECT
    DATE(p.created_at) as payment_date,
    r.id as restaurant_id,
    r.name as restaurant_name,
    p.payment_method,
    p.payment_provider,
    p.status,
    COUNT(*) as transaction_count,
    SUM(p.amount) as total_amount,
    SUM(CASE WHEN p.status = 'COMPLETED' THEN p.amount ELSE 0 END) as successful_amount,
    SUM(CASE WHEN p.status = 'FAILED' THEN 1 ELSE 0 END) as failed_count,
    SUM(p.refund_amount) as total_refunded,
    AVG(CASE WHEN p.status = 'COMPLETED' THEN p.amount END) as avg_transaction_value
FROM payments p
         JOIN restaurants r ON p.restaurant_id = r.id
GROUP BY DATE(p.created_at), r.id, r.name, p.payment_method, p.payment_provider, p.status;

CREATE OR REPLACE VIEW wallet_balances AS
SELECT
    u.id as user_id,
    u.full_name,
    u.email,
    w.balance,
    w.currency,
    w.total_credited,
    w.total_debited,
    w.total_refunded,
    w.last_transaction_at,
    w.is_active,
    w.is_locked
FROM customer_wallets w
         JOIN users u ON w.customer_id = u.id
WHERE w.restaurant_id IS NULL;

CREATE OR REPLACE VIEW pending_refunds AS
SELECT
    r.id as refund_id,
    r.refund_reference,
    p.payment_reference,
    o.order_number,
    r.amount,
    r.currency,
    r.refund_type,
    r.status,
    r.reason,
    r.initiated_at,
    EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - r.initiated_at))/3600 as hours_pending,
    u.full_name as customer_name,
    u.email as customer_email
FROM payment_refunds r
         JOIN payments p ON r.payment_id = p.id
         JOIN orders o ON p.order_id = o.id
         JOIN users u ON p.customer_id = u.id
WHERE r.status IN ('PENDING', 'APPROVED', 'PROCESSING')
ORDER BY r.initiated_at ASC;

CREATE OR REPLACE VIEW transaction_summary AS
SELECT
    u.id as user_id,
    u.full_name,
    COUNT(DISTINCT p.id) as total_payments,
    SUM(CASE WHEN p.status = 'COMPLETED' THEN p.amount ELSE 0 END) as total_paid,
    COUNT(DISTINCT CASE WHEN p.status = 'COMPLETED' THEN p.id END) as successful_payments,
    COUNT(DISTINCT CASE WHEN p.status = 'FAILED' THEN p.id END) as failed_payments,
    SUM(p.refund_amount) as total_refunds_received,
    COALESCE(w.balance, 0) as wallet_balance
FROM users u
         LEFT JOIN payments p ON u.id = p.customer_id
         LEFT JOIN customer_wallets w ON u.id = w.customer_id AND w.restaurant_id IS NULL
GROUP BY u.id, u.full_name, w.balance;

-- Triggers

CREATE OR REPLACE FUNCTION update_wallet_balance()
    RETURNS TRIGGER AS $$
BEGIN
    UPDATE customer_wallets
    SET
        balance = NEW.balance_after,
        last_transaction_at = NEW.transaction_timestamp,
        total_credited = total_credited + CASE WHEN NEW.transaction_type IN ('CREDIT', 'REFUND', 'CASHBACK', 'REWARD', 'TRANSFER_IN') THEN NEW.amount ELSE 0 END,
        total_debited = total_debited + CASE WHEN NEW.transaction_type IN ('DEBIT', 'TRANSFER_OUT') THEN NEW.amount ELSE 0 END,
        total_refunded = total_refunded + CASE WHEN NEW.transaction_type = 'REFUND' THEN NEW.amount ELSE 0 END
    WHERE id = NEW.wallet_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_wallet_balance
    AFTER INSERT ON wallet_transactions
    FOR EACH ROW
EXECUTE FUNCTION update_wallet_balance();

-- Auto-create wallet for new users
CREATE OR REPLACE FUNCTION create_default_wallet()
    RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO customer_wallets (customer_id, currency, balance)
    VALUES (NEW.id, 'BDT', 0);

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_create_wallet
    AFTER INSERT ON users
    FOR EACH ROW
EXECUTE FUNCTION create_default_wallet();

-- Function to expire pending payments
CREATE OR REPLACE FUNCTION expire_pending_payments()
    RETURNS void AS $$
BEGIN
    UPDATE payments
    SET status = 'EXPIRED'
    WHERE status = 'PENDING'
      AND initiated_at < (CURRENT_TIMESTAMP - INTERVAL '30 minutes');
END;
$$ LANGUAGE plpgsql;

COMMENT ON TABLE payments IS 'Main payment records for orders';
COMMENT ON TABLE payment_transactions IS 'Detailed transaction log for payments';
COMMENT ON TABLE customer_wallets IS 'Customer wallet balances';
COMMENT ON TABLE wallet_transactions IS 'Wallet ledger entries';
COMMENT ON TABLE payment_refunds IS 'Payment refund requests and processing';
COMMENT ON TABLE payment_webhooks IS 'Webhook events from payment providers';
COMMENT ON TABLE payment_split_details IS 'Split payment breakdown';
COMMENT ON TABLE payment_gateway_configs IS 'Payment gateway configurations per restaurant';