-- V14__create_settlement_payout_system.sql

-- Order Settlements table
CREATE TABLE order_settlements (
                                   id BIGSERIAL PRIMARY KEY,
                                   order_id BIGINT NOT NULL UNIQUE REFERENCES orders(id) ON DELETE RESTRICT,
                                   payment_id BIGINT NOT NULL REFERENCES payments(id),
                                   restaurant_id BIGINT NOT NULL REFERENCES restaurants(id),
                                   settlement_reference VARCHAR(100) NOT NULL UNIQUE,
                                   order_amount DECIMAL(10,2) NOT NULL,
                                   payment_amount DECIMAL(10,2) NOT NULL,
                                   commission_percentage DECIMAL(5,2),
                                   commission_amount DECIMAL(10,2) NOT NULL,
                                   platform_fee DECIMAL(10,2) DEFAULT 0,
                                   delivery_fee DECIMAL(10,2) DEFAULT 0,
                                   tax_amount DECIMAL(10,2) DEFAULT 0,
                                   discount_amount DECIMAL(10,2) DEFAULT 0,
                                   refund_amount DECIMAL(10,2) DEFAULT 0,
                                   adjustment_amount DECIMAL(10,2) DEFAULT 0,
                                   net_amount DECIMAL(10,2) NOT NULL,
                                   currency VARCHAR(3) NOT NULL DEFAULT 'BDT',
                                   settlement_status VARCHAR(30) NOT NULL,
                                   settlement_date DATE NOT NULL,
                                   settled_at TIMESTAMP,
                                   payout_id BIGINT REFERENCES restaurant_payouts(id),
                                   notes TEXT,
                                   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                   CONSTRAINT chk_settlement_status CHECK (settlement_status IN (
                                                                                                 'PENDING', 'IN_PROGRESS', 'SETTLED', 'ON_HOLD',
                                                                                                 'DISPUTED', 'CANCELLED', 'REFUNDED'
                                       ))
);

CREATE INDEX idx_settlement_order ON order_settlements(order_id);
CREATE INDEX idx_settlement_restaurant ON order_settlements(restaurant_id);
CREATE INDEX idx_settlement_status ON order_settlements(settlement_status);
CREATE INDEX idx_settlement_date ON order_settlements(settlement_date);
CREATE INDEX idx_settlement_payout ON order_settlements(payout_id);

-- Restaurant Payouts table
CREATE TABLE restaurant_payouts (
                                    id BIGSERIAL PRIMARY KEY,
                                    restaurant_id BIGINT NOT NULL REFERENCES restaurants(id),
                                    payout_reference VARCHAR(100) NOT NULL UNIQUE,
                                    payout_type VARCHAR(30) NOT NULL,
                                    period_start_date DATE NOT NULL,
                                    period_end_date DATE NOT NULL,
                                    total_orders INTEGER NOT NULL,
                                    total_order_amount DECIMAL(10,2),
                                    total_commission DECIMAL(10,2),
                                    total_fees DECIMAL(10,2) DEFAULT 0,
                                    total_refunds DECIMAL(10,2) DEFAULT 0,
                                    total_adjustments DECIMAL(10,2) DEFAULT 0,
                                    payout_amount DECIMAL(10,2) NOT NULL,
                                    currency VARCHAR(3) NOT NULL DEFAULT 'BDT',
                                    payout_method VARCHAR(30) NOT NULL,
                                    bank_account_number VARCHAR(50),
                                    bank_name VARCHAR(100),
                                    bank_branch VARCHAR(100),
                                    account_holder_name VARCHAR(200),
                                    mobile_money_number VARCHAR(20),
                                    mobile_money_provider VARCHAR(30),
                                    payout_status VARCHAR(30) NOT NULL,
                                    payout_date DATE,
                                    processed_at TIMESTAMP,
                                    initiated_by BIGINT REFERENCES users(id),
                                    initiated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    approved_by BIGINT REFERENCES users(id),
                                    approved_at TIMESTAMP,
                                    processed_by BIGINT REFERENCES users(id),
                                    transaction_reference VARCHAR(200),
                                    rejection_reason VARCHAR(500),
                                    notes TEXT,
                                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                    CONSTRAINT chk_payout_type CHECK (payout_type IN (
                                                                                      'MANUAL', 'SCHEDULED', 'ON_DEMAND', 'EARLY_PAYOUT'
                                        )),
                                    CONSTRAINT chk_payout_method CHECK (payout_method IN (
                                                                                          'BANK_TRANSFER', 'BKASH', 'NAGAD', 'ROCKET',
                                                                                          'CASH', 'CHEQUE', 'WALLET_CREDIT'
                                        )),
                                    CONSTRAINT chk_payout_status CHECK (payout_status IN (
                                                                                          'DRAFT', 'PENDING_APPROVAL', 'APPROVED', 'PROCESSING',
                                                                                          'COMPLETED', 'FAILED', 'CANCELLED', 'REJECTED', 'ON_HOLD'
                                        ))
);

CREATE INDEX idx_payout_restaurant ON restaurant_payouts(restaurant_id);
CREATE INDEX idx_payout_status ON restaurant_payouts(payout_status);
CREATE INDEX idx_payout_date ON restaurant_payouts(payout_date);
CREATE INDEX idx_payout_reference ON restaurant_payouts(payout_reference);
CREATE INDEX idx_payout_period ON restaurant_payouts(period_start_date, period_end_date);

-- Commission Rules table
CREATE TABLE commission_rules (
                                  id BIGSERIAL PRIMARY KEY,
                                  restaurant_id BIGINT NOT NULL REFERENCES restaurants(id),
                                  rule_name VARCHAR(100) NOT NULL,
                                  commission_type VARCHAR(30) NOT NULL,
                                  commission_percentage DECIMAL(5,2),
                                  fixed_fee_per_order DECIMAL(10,2),
                                  min_commission_amount DECIMAL(10,2),
                                  max_commission_amount DECIMAL(10,2),
                                  applies_to_order_type VARCHAR(30),
                                  min_order_amount DECIMAL(10,2),
                                  effective_from DATE NOT NULL,
                                  effective_to DATE,
                                  is_active BOOLEAN NOT NULL DEFAULT TRUE,
                                  priority INTEGER NOT NULL DEFAULT 0,
                                  description TEXT,
                                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                  CONSTRAINT chk_commission_type CHECK (commission_type IN (
                                                                                            'PERCENTAGE', 'FIXED', 'TIERED', 'HYBRID'
                                      ))
);

CREATE INDEX idx_commission_restaurant ON commission_rules(restaurant_id);
CREATE INDEX idx_commission_active ON commission_rules(is_active);
CREATE INDEX idx_commission_dates ON commission_rules(effective_from, effective_to);

-- Commission Tiers table
CREATE TABLE commission_tiers (
                                  id BIGSERIAL PRIMARY KEY,
                                  commission_rule_id BIGINT NOT NULL REFERENCES commission_rules(id) ON DELETE CASCADE,
                                  tier_name VARCHAR(100) NOT NULL,
                                  min_monthly_orders INTEGER,
                                  max_monthly_orders INTEGER,
                                  min_monthly_revenue DECIMAL(10,2),
                                  max_monthly_revenue DECIMAL(10,2),
                                  commission_percentage DECIMAL(5,2) NOT NULL,
                                  tier_order INTEGER NOT NULL,
                                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tier_rule ON commission_tiers(commission_rule_id);

-- Settlement Adjustments table
CREATE TABLE settlement_adjustments (
                                        id BIGSERIAL PRIMARY KEY,
                                        settlement_id BIGINT REFERENCES order_settlements(id),
                                        restaurant_id BIGINT NOT NULL REFERENCES restaurants(id),
                                        adjustment_reference VARCHAR(100) NOT NULL UNIQUE,
                                        adjustment_type VARCHAR(30) NOT NULL,
                                        amount DECIMAL(10,2) NOT NULL,
                                        reason VARCHAR(500) NOT NULL,
                                        created_by BIGINT NOT NULL REFERENCES users(id),
                                        approved_by BIGINT REFERENCES users(id),
                                        approved_at TIMESTAMP,
                                        status VARCHAR(30) NOT NULL,
                                        notes TEXT,
                                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                        CONSTRAINT chk_adjustment_type CHECK (adjustment_type IN (
                                                                                                  'BONUS', 'PENALTY', 'CORRECTION', 'PROMOTIONAL',
                                                                                                  'CHARGEBACK', 'DISPUTE_RESOLUTION'
                                            )),
                                        CONSTRAINT chk_adjustment_status CHECK (status IN (
                                                                                           'PENDING', 'APPROVED', 'REJECTED', 'APPLIED'
                                            ))
);

CREATE INDEX idx_adjustment_settlement ON settlement_adjustments(settlement_id);
CREATE INDEX idx_adjustment_restaurant ON settlement_adjustments(restaurant_id);
CREATE INDEX idx_adjustment_status ON settlement_adjustments(status);

-- Payout Documents table
CREATE TABLE payout_documents (
                                  id BIGSERIAL PRIMARY KEY,
                                  payout_id BIGINT NOT NULL REFERENCES restaurant_payouts(id) ON DELETE CASCADE,
                                  document_type VARCHAR(30) NOT NULL,
                                  file_name VARCHAR(255) NOT NULL,
                                  file_url VARCHAR(500) NOT NULL,
                                  file_size BIGINT,
                                  mime_type VARCHAR(100),
                                  uploaded_by BIGINT NOT NULL REFERENCES users(id),
                                  uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                  CONSTRAINT chk_document_type CHECK (document_type IN (
                                                                                        'INVOICE', 'RECEIPT', 'BANK_STATEMENT', 'TAX_DOCUMENT',
                                                                                        'SETTLEMENT_REPORT', 'SIGNED_AGREEMENT', 'OTHER'
                                      ))
);

CREATE INDEX idx_document_payout ON payout_documents(payout_id);

-- Settlement Reconciliations table
CREATE TABLE settlement_reconciliations (
                                            id BIGSERIAL PRIMARY KEY,
                                            restaurant_id BIGINT NOT NULL REFERENCES restaurants(id),
                                            reconciliation_reference VARCHAR(100) NOT NULL UNIQUE,
                                            period_start_date DATE NOT NULL,
                                            period_end_date DATE NOT NULL,
                                            total_settlements INTEGER NOT NULL,
                                            total_settlement_amount DECIMAL(10,2),
                                            total_payouts INTEGER NOT NULL,
                                            total_payout_amount DECIMAL(10,2),
                                            variance_amount DECIMAL(10,2),
                                            discrepancies_found INTEGER NOT NULL,
                                            reconciliation_status VARCHAR(30) NOT NULL,
                                            performed_by BIGINT NOT NULL REFERENCES users(id),
                                            performed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                            notes TEXT,
                                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                            updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                            CONSTRAINT chk_reconciliation_status CHECK (reconciliation_status IN (
                                                                                                                  'IN_PROGRESS', 'RECONCILED', 'DISCREPANCY_FOUND', 'RESOLVED'
                                                ))
);

CREATE INDEX idx_reconciliation_restaurant ON settlement_reconciliations(restaurant_id);
CREATE INDEX idx_reconciliation_period ON settlement_reconciliations(period_start_date, period_end_date);

-- Add foreign key to order_settlements after restaurant_payouts exists
ALTER TABLE order_settlements
    ADD CONSTRAINT fk_settlement_payout
        FOREIGN KEY (payout_id) REFERENCES restaurant_payouts(id);

-- Analytical Views

CREATE OR REPLACE VIEW settlement_analytics AS
SELECT
    r.id as restaurant_id,
    r.name as restaurant_name,
    DATE(s.settlement_date) as date,
    COUNT(*) as total_settlements,
    SUM(s.order_amount) as total_order_amount,
    SUM(s.commission_amount) as total_commission,
    SUM(s.platform_fee) as total_platform_fees,
    SUM(s.net_amount) as total_net_amount,
    AVG(s.commission_percentage) as avg_commission_rate,
    SUM(CASE WHEN s.settlement_status = 'SETTLED' THEN s.net_amount ELSE 0 END) as amount_paid,
    SUM(CASE WHEN s.settlement_status = 'PENDING' THEN s.net_amount ELSE 0 END) as amount_pending
FROM order_settlements s
         JOIN restaurants r ON s.restaurant_id = r.id
GROUP BY r.id, r.name, DATE(s.settlement_date);

CREATE OR REPLACE VIEW payout_summary AS
SELECT
    r.id as restaurant_id,
    r.name as restaurant_name,
    p.payout_status,
    COUNT(*) as payout_count,
    SUM(p.payout_amount) as total_amount,
    SUM(p.total_commission) as total_commission_collected,
    AVG(p.total_orders) as avg_orders_per_payout,
    MIN(p.payout_date) as earliest_payout,
    MAX(p.payout_date) as latest_payout
FROM restaurant_payouts p
         JOIN restaurants r ON p.restaurant_id = r.id
GROUP BY r.id, r.name, p.payout_status;

CREATE OR REPLACE VIEW pending_settlements_summary AS
SELECT
    r.id as restaurant_id,
    r.name as restaurant_name,
    COUNT(s.id) as pending_count,
    SUM(s.net_amount) as pending_amount,
    MIN(s.settlement_date) as oldest_settlement,
    MAX(s.settlement_date) as newest_settlement,
    AVG(EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - s.created_at))/86400) as avg_days_pending
FROM order_settlements s
         JOIN restaurants r ON s.restaurant_id = r.id
WHERE s.settlement_status = 'PENDING'
GROUP BY r.id, r.name;

CREATE OR REPLACE VIEW commission_performance AS
SELECT
    r.id as restaurant_id,
    r.name as restaurant_name,
    DATE_TRUNC('month', s.settlement_date) as month,
    COUNT(*) as total_orders,
    SUM(s.order_amount) as total_revenue,
    SUM(s.commission_amount) as total_commission,
    AVG(s.commission_percentage) as avg_commission_rate,
    SUM(s.commission_amount) / NULLIF(SUM(s.order_amount), 0) * 100 as effective_commission_rate
FROM order_settlements s
         JOIN restaurants r ON s.restaurant_id = r.id
GROUP BY r.id, r.name, DATE_TRUNC('month', s.settlement_date);

-- Triggers

-- Auto-create settlement on payment completion
CREATE OR REPLACE FUNCTION create_settlement_on_payment()
    RETURNS TRIGGER AS $$
DECLARE
    commission_amt DECIMAL(10,2);
    commission_pct DECIMAL(5,2);
    net_amt DECIMAL(10,2);
BEGIN
    -- Only create settlement for completed payments
    IF NEW.status = 'COMPLETED' AND OLD.status != 'COMPLETED' THEN

        -- Get commission from restaurant's subscription or default to 5%
        SELECT COALESCE(
                       (SELECT commission_percentage
                        FROM commission_rules cr
                        WHERE cr.restaurant_id = NEW.restaurant_id
                          AND cr.is_active = TRUE
                          AND CURRENT_DATE BETWEEN cr.effective_from AND COALESCE(cr.effective_to, CURRENT_DATE)
                        ORDER BY cr.priority DESC
                        LIMIT 1),
                       5.00
               ) INTO commission_pct;

        commission_amt := NEW.amount * (commission_pct / 100);
        net_amt := NEW.amount - commission_amt;

        INSERT INTO order_settlements (
            order_id,
            payment_id,
            restaurant_id,
            settlement_reference,
            order_amount,
            payment_amount,
            commission_percentage,
            commission_amount,
            net_amount,
            currency,
            settlement_status,
            settlement_date
        ) VALUES (
                     NEW.order_id,
                     NEW.id,
                     NEW.restaurant_id,
                     'SETL-' || UPPER(SUBSTRING(MD5(RANDOM()::TEXT) FROM 1 FOR 10)),
                     NEW.amount,
                     NEW.amount,
                     commission_pct,
                     commission_amt,
                     net_amt,
                     NEW.currency,
                     'PENDING',
                     CURRENT_DATE
                 );

    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_create_settlement
    AFTER UPDATE ON payments
    FOR EACH ROW
EXECUTE FUNCTION create_settlement_on_payment();

-- Update settlement on adjustment approval
CREATE OR REPLACE FUNCTION apply_settlement_adjustment()
    RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status = 'APPLIED' AND OLD.status != 'APPLIED' THEN

        UPDATE order_settlements
        SET
            adjustment_amount = adjustment_amount + NEW.amount,
            net_amount = net_amount + NEW.amount,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = NEW.settlement_id;

    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_apply_adjustment
    AFTER UPDATE ON settlement_adjustments
    FOR EACH ROW
EXECUTE FUNCTION apply_settlement_adjustment();

-- Seed default commission rules for existing restaurants
INSERT INTO commission_rules (
    restaurant_id,
    rule_name,
    commission_type,
    commission_percentage,
    effective_from,
    is_active,
    priority,
    description
)
SELECT
    id,
    'Default Commission Rule',
    'PERCENTAGE',
    CASE
        WHEN sp.plan_name = 'BASIC' THEN 8.00
        WHEN sp.plan_name = 'STANDARD' THEN 6.00
        WHEN sp.plan_name = 'PREMIUM' THEN 4.00
        WHEN sp.plan_name = 'ENTERPRISE' THEN 2.00
        ELSE 5.00
        END,
    CURRENT_DATE,
    TRUE,
    1,
    'Automatically created default commission rule based on subscription plan'
FROM restaurants r
         LEFT JOIN restaurant_subscriptions rs ON r.id = rs.restaurant_id
         LEFT JOIN subscription_plans sp ON rs.plan_id = sp.id
WHERE NOT EXISTS (
    SELECT 1 FROM commission_rules cr
    WHERE cr.restaurant_id = r.id
);

COMMENT ON TABLE order_settlements IS 'Settlement records per order';
COMMENT ON TABLE restaurant_payouts IS 'Batch payouts to restaurants';
COMMENT ON TABLE commission_rules IS 'Commission configuration per restaurant';
COMMENT ON TABLE commission_tiers IS 'Tiered commission structure';
COMMENT ON TABLE settlement_adjustments IS 'Manual settlement adjustments';
COMMENT ON TABLE payout_documents IS 'Supporting documents for payouts';
COMMENT ON TABLE settlement_reconciliations IS 'Settlement reconciliation records';