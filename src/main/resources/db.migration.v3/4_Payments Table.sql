DROP TABLE IF EXISTS payments CASCADE;

CREATE TABLE payments (
                          id BIGSERIAL PRIMARY KEY,
                          payment_reference VARCHAR(100) NOT NULL,

    -- Foreign keys
                          order_id BIGINT NOT NULL,
                          customer_id BIGINT NOT NULL,
                          restaurant_id BIGINT NOT NULL,

    -- Payment details
                          payment_method VARCHAR(30) NOT NULL,
                          payment_provider VARCHAR(30),
                          amount DECIMAL(10,2) NOT NULL,
                          currency VARCHAR(3) NOT NULL DEFAULT 'BDT',
                          status VARCHAR(20) NOT NULL,

    -- Provider info
                          provider_transaction_id VARCHAR(200),
                          provider_payment_intent_id VARCHAR(200),
                          provider_response TEXT,

    -- Timing
                          initiated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          completed_at TIMESTAMP,
                          failed_at TIMESTAMP,
                          failure_reason VARCHAR(500),

    -- Refunds
                          refund_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
                          is_refunded BOOLEAN NOT NULL DEFAULT false,

    -- Contact (for MFS)
                          customer_phone VARCHAR(20),
                          customer_email VARCHAR(100),

    -- Security
                          ip_address VARCHAR(45),
                          user_agent VARCHAR(500),

    -- Audit
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
                          CONSTRAINT payments_reference_unique UNIQUE (payment_reference),
                          CONSTRAINT payments_order_fk FOREIGN KEY (order_id)
                              REFERENCES orders(id) ON DELETE RESTRICT,
                          CONSTRAINT payments_customer_fk FOREIGN KEY (customer_id)
                              REFERENCES users(id) ON DELETE RESTRICT,
                          CONSTRAINT payments_restaurant_fk FOREIGN KEY (restaurant_id)
                              REFERENCES restaurants(id) ON DELETE RESTRICT,
                          CONSTRAINT payments_method_check
                              CHECK (payment_method IN ('ONLINE_CARD', 'BKASH', 'NAGAD', 'ROCKET',
                                                        'CASH', 'CARD_TERMINAL', 'WALLET', 'SPLIT')),
                          CONSTRAINT payments_status_check
                              CHECK (status IN ('PENDING', 'PROCESSING', 'AUTHORIZED', 'COMPLETED',
                                                'FAILED', 'CANCELLED', 'REFUND_PENDING', 'REFUNDED',
                                                'PARTIALLY_REFUNDED', 'EXPIRED')),
                          CONSTRAINT payments_amount_positive CHECK (amount > 0),
                          CONSTRAINT payments_refund_valid CHECK (refund_amount <= amount)
);

-- Performance indexes
CREATE INDEX CONCURRENTLY idx_payments_order
    ON payments(order_id);

CREATE INDEX CONCURRENTLY idx_payments_reference
    ON payments(payment_reference);

CREATE INDEX CONCURRENTLY idx_payments_customer
    ON payments(customer_id, created_at DESC);

CREATE INDEX CONCURRENTLY idx_payments_restaurant_status
    ON payments(restaurant_id, status, created_at DESC);

CREATE INDEX CONCURRENTLY idx_payments_provider_txn
    ON payments(provider_transaction_id)
    WHERE provider_transaction_id IS NOT NULL;

-- Partial index for pending payments
CREATE INDEX CONCURRENTLY idx_payments_pending
    ON payments(restaurant_id, initiated_at)
    WHERE status IN ('PENDING', 'PROCESSING') AND completed_at IS NULL;

-- For reconciliation queries
CREATE INDEX CONCURRENTLY idx_payments_completed
    ON payments(restaurant_id, completed_at DESC, amount)
    WHERE status = 'COMPLETED';

ALTER TABLE payments SET (
    autovacuum_vacuum_scale_factor = 0.1
    );

COMMENT ON TABLE payments IS 'Payment transactions - critical for financial reconciliation';