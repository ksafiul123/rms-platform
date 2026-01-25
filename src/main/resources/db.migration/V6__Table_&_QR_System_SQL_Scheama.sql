-- QR-Based Table Ordering System Schema

-- Tables
CREATE TABLE tables (
                        id BIGSERIAL PRIMARY KEY,
                        restaurant_id BIGINT NOT NULL,
                        branch_id BIGINT,
                        table_number VARCHAR(20) NOT NULL,
                        qr_code VARCHAR(100) NOT NULL UNIQUE,
                        qr_code_image_url VARCHAR(500),
                        capacity INTEGER NOT NULL CHECK (capacity > 0),
                        floor VARCHAR(50),
                        section VARCHAR(50),
                        status VARCHAR(20) NOT NULL CHECK (status IN ('AVAILABLE', 'OCCUPIED', 'RESERVED', 'CLEANING', 'MAINTENANCE')),
                        is_active BOOLEAN NOT NULL DEFAULT TRUE,
                        description VARCHAR(500),
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                        CONSTRAINT fk_tables_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE,
                        CONSTRAINT fk_tables_branch FOREIGN KEY (branch_id) REFERENCES restaurant_branches(id) ON DELETE SET NULL,
                        CONSTRAINT uq_tables_restaurant_number UNIQUE (restaurant_id, table_number)
);

-- Indexes for tables
CREATE INDEX idx_tables_restaurant_id ON tables(restaurant_id);
CREATE INDEX idx_tables_qr_code ON tables(qr_code);
CREATE INDEX idx_tables_status ON tables(status);
CREATE INDEX idx_tables_branch_id ON tables(branch_id);
CREATE INDEX idx_tables_restaurant_active ON tables(restaurant_id, is_active);

-- Table Sessions
CREATE TABLE table_sessions (
                                id BIGSERIAL PRIMARY KEY,
                                table_id BIGINT NOT NULL,
                                restaurant_id BIGINT NOT NULL,
                                session_code VARCHAR(50) NOT NULL UNIQUE,
                                status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'COMPLETED', 'CANCELLED')),
                                started_at TIMESTAMP NOT NULL,
                                ended_at TIMESTAMP,
                                guest_count INTEGER NOT NULL DEFAULT 0,
                                total_amount DECIMAL(10, 2) DEFAULT 0.00,
                                notes TEXT,
                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                CONSTRAINT fk_table_sessions_table FOREIGN KEY (table_id) REFERENCES tables(id) ON DELETE CASCADE,
                                CONSTRAINT fk_table_sessions_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE
);

-- Indexes for table sessions
CREATE INDEX idx_table_sessions_table_id ON table_sessions(table_id);
CREATE INDEX idx_table_sessions_restaurant_id ON table_sessions(restaurant_id);
CREATE INDEX idx_table_sessions_status ON table_sessions(status);
CREATE INDEX idx_table_sessions_session_code ON table_sessions(session_code);
CREATE INDEX idx_table_sessions_started_at ON table_sessions(started_at);
CREATE INDEX idx_table_sessions_restaurant_status ON table_sessions(restaurant_id, status);

-- Table Session Guests
CREATE TABLE table_session_guests (
                                      id BIGSERIAL PRIMARY KEY,
                                      session_id BIGINT NOT NULL,
                                      user_id BIGINT,
                                      guest_name VARCHAR(100),
                                      is_host BOOLEAN NOT NULL DEFAULT FALSE,
                                      status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'LEFT')),
                                      joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      left_at TIMESTAMP,

                                      CONSTRAINT fk_table_session_guests_session FOREIGN KEY (session_id) REFERENCES table_sessions(id) ON DELETE CASCADE,
                                      CONSTRAINT fk_table_session_guests_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
                                      CONSTRAINT uq_session_guests_session_user UNIQUE (session_id, user_id)
);

-- Indexes for table session guests
CREATE INDEX idx_table_session_guests_session_id ON table_session_guests(session_id);
CREATE INDEX idx_table_session_guests_user_id ON table_session_guests(user_id);
CREATE INDEX idx_table_session_guests_joined_at ON table_session_guests(joined_at);
CREATE INDEX idx_table_session_guests_status ON table_session_guests(status);

-- Add table_session_id to orders table (modify existing orders table)
ALTER TABLE orders ADD COLUMN table_session_id BIGINT;
ALTER TABLE orders ADD CONSTRAINT fk_orders_table_session
    FOREIGN KEY (table_session_id) REFERENCES table_sessions(id) ON DELETE SET NULL;
CREATE INDEX idx_orders_table_session_id ON orders(table_session_id);

-- Trigger to update tables updated_at timestamp
CREATE OR REPLACE FUNCTION update_tables_updated_at()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_tables_updated_at
    BEFORE UPDATE ON tables
    FOR EACH ROW
EXECUTE FUNCTION update_tables_updated_at();

-- Trigger to update table_sessions updated_at timestamp
CREATE OR REPLACE FUNCTION update_table_sessions_updated_at()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_table_sessions_updated_at
    BEFORE UPDATE ON table_sessions
    FOR EACH ROW
EXECUTE FUNCTION update_table_sessions_updated_at();

-- Function to automatically update table status when session starts/ends
CREATE OR REPLACE FUNCTION update_table_status_on_session_change()
    RETURNS TRIGGER AS $$
BEGIN
    -- When session becomes active
    IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') AND NEW.status = 'ACTIVE' THEN
        UPDATE tables SET status = 'OCCUPIED' WHERE id = NEW.table_id;
    END IF;

    -- When session ends
    IF (TG_OP = 'UPDATE') AND OLD.status = 'ACTIVE' AND NEW.status IN ('COMPLETED', 'CANCELLED') THEN
        -- Check if there are other active sessions
        IF NOT EXISTS (
            SELECT 1 FROM table_sessions
            WHERE table_id = NEW.table_id AND status = 'ACTIVE' AND id != NEW.id
        ) THEN
            UPDATE tables SET status = 'AVAILABLE' WHERE id = NEW.table_id;
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_table_status_on_session
    AFTER INSERT OR UPDATE ON table_sessions
    FOR EACH ROW
EXECUTE FUNCTION update_table_status_on_session_change();

-- View for active sessions with table details
CREATE VIEW active_sessions_view AS
SELECT
    ts.id AS session_id,
    ts.session_code,
    ts.started_at,
    ts.guest_count,
    ts.total_amount,
    t.id AS table_id,
    t.table_number,
    t.capacity,
    t.floor,
    t.section,
    r.id AS restaurant_id,
    r.name AS restaurant_name,
    COUNT(DISTINCT tsg.id) FILTER (WHERE tsg.status = 'ACTIVE') AS active_guests,
    COUNT(DISTINCT o.id) AS order_count
FROM table_sessions ts
         JOIN tables t ON ts.table_id = t.id
         JOIN restaurants r ON ts.restaurant_id = r.id
         LEFT JOIN table_session_guests tsg ON ts.id = tsg.session_id
         LEFT JOIN orders o ON ts.id = o.table_session_id
WHERE ts.status = 'ACTIVE'
GROUP BY ts.id, ts.session_code, ts.started_at, ts.guest_count, ts.total_amount,
         t.id, t.table_number, t.capacity, t.floor, t.section,
         r.id, r.name;

-- Comments for documentation
COMMENT ON TABLE tables IS 'Restaurant tables with unique QR codes for ordering';
COMMENT ON TABLE table_sessions IS 'Active dining sessions at tables supporting multiple guests';
COMMENT ON TABLE table_session_guests IS 'Guests participating in a table session';

COMMENT ON COLUMN tables.qr_code IS 'Unique QR code identifier for table';
COMMENT ON COLUMN tables.qr_code_image_url IS 'Base64 or URL to QR code image';
COMMENT ON COLUMN tables.status IS 'Current table status: AVAILABLE, OCCUPIED, RESERVED, CLEANING, MAINTENANCE';

COMMENT ON COLUMN table_sessions.session_code IS 'Unique code for others to join the session';
COMMENT ON COLUMN table_sessions.guest_count IS 'Number of guests in session (auto-calculated)';
COMMENT ON COLUMN table_sessions.total_amount IS 'Total amount of all orders in session';

COMMENT ON COLUMN table_session_guests.is_host IS 'First guest who scanned QR and started session';
COMMENT ON COLUMN table_session_guests.user_id IS 'User ID if authenticated, null for anonymous guests';
COMMENT ON COLUMN table_session_guests.guest_name IS 'Guest name for display purposes';

-- Sample data for testing
-- INSERT INTO tables (restaurant_id, table_number, qr_code, capacity, floor, section, status) VALUES
-- (1, 'T1', 'QR-A1B2C3D4E5F6G7H8', 4, 'Ground Floor', 'Window Side', 'AVAILABLE'),
-- (1, 'T2', 'QR-I9J0K1L2M3N4O5P6', 2, 'Ground Floor', 'Center', 'AVAILABLE'),
-- (1, 'T3', 'QR-Q7R8S9T0U1V2W3X4', 6, 'First Floor', 'Private', 'AVAILABLE');