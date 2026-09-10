-- Flyway migration: create dinerly_points and points_ledger
CREATE TABLE IF NOT EXISTS dinerly_points (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    balance BIGINT NOT NULL DEFAULT 0,
    version BIGINT,
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now()
);

CREATE TABLE IF NOT EXISTS points_ledger (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    delta BIGINT NOT NULL,
    balance_after BIGINT NOT NULL,
    reason TEXT,
    source_type VARCHAR(100),
    source_id BIGINT,
    created_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_points_ledger_user ON points_ledger(user_id);
CREATE INDEX IF NOT EXISTS idx_dinerly_points_user ON dinerly_points(user_id);
