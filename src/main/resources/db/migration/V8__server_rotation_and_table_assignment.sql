-- Server rotation / table assignment feature
-- 1) Table currently-serving-server link
ALTER TABLE tables ADD COLUMN IF NOT EXISTS current_server_id BIGINT NULL;

-- 2) Waitlist linkage to an actual Table row + the server credited with the seating
ALTER TABLE waitlist ADD COLUMN IF NOT EXISTS table_id BIGINT NULL;
ALTER TABLE waitlist ADD COLUMN IF NOT EXISTS assigned_server_id BIGINT NULL;

-- 3) Per-server rotation/floor state (one row per server)
CREATE TABLE IF NOT EXISTS server_floor_status (
    id BIGSERIAL PRIMARY KEY,
    restaurant_id BIGINT NOT NULL,
    staff_id BIGINT NOT NULL UNIQUE,
    on_floor BOOLEAN NOT NULL DEFAULT FALSE,
    seated_count INTEGER NOT NULL DEFAULT 0,
    rotation_order INTEGER NOT NULL DEFAULT 0,
    is_next_up BOOLEAN NOT NULL DEFAULT FALSE,
    shift_started_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_server_floor_status_restaurant ON server_floor_status(restaurant_id);

-- 4) Server-facing notifications (e.g. "table assigned") polled by the server's own UI
CREATE TABLE IF NOT EXISTS server_notifications (
    id BIGSERIAL PRIMARY KEY,
    restaurant_id BIGINT NOT NULL,
    staff_id BIGINT NOT NULL,
    waitlist_id BIGINT NULL,
    table_id BIGINT NULL,
    type VARCHAR(30) NOT NULL DEFAULT 'TABLE_ASSIGNED',
    message VARCHAR(500) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_server_notifications_staff ON server_notifications(restaurant_id, staff_id, is_read);

