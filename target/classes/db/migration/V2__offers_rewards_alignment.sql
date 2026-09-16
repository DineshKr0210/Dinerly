-- Flyway migration: Phase 1-5 Offers & Rewards alignment
-- Date: 2026-09-16

-- Phase 1: Expand Offer entity with new fields
ALTER TABLE IF EXISTS offers ADD COLUMN IF NOT EXISTS discount_type VARCHAR(50);
ALTER TABLE IF EXISTS offers ADD COLUMN IF NOT EXISTS discount_value DECIMAL(10, 2);
ALTER TABLE IF EXISTS offers ADD COLUMN IF NOT EXISTS discount_label VARCHAR(100);
ALTER TABLE IF EXISTS offers ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE IF EXISTS offers ADD COLUMN IF NOT EXISTS photo_url VARCHAR(500);
ALTER TABLE IF EXISTS offers ADD COLUMN IF NOT EXISTS rating DECIMAL(3, 2);
ALTER TABLE IF EXISTS offers ADD COLUMN IF NOT EXISTS rating_count BIGINT DEFAULT 0;
ALTER TABLE IF EXISTS offers ADD COLUMN IF NOT EXISTS category VARCHAR(50);
ALTER TABLE IF EXISTS offers ADD COLUMN IF NOT EXISTS per_user_limit INTEGER;
ALTER TABLE IF EXISTS offers ADD COLUMN IF NOT EXISTS per_user_daily_limit INTEGER;
ALTER TABLE IF EXISTS offers ADD COLUMN IF NOT EXISTS inventory INTEGER;
ALTER TABLE IF EXISTS offers ADD COLUMN IF NOT EXISTS original_price DECIMAL(10, 2);

-- Create restrictions table for offer restrictions (array)
CREATE TABLE IF NOT EXISTS offer_restrictions (
    offer_id BIGINT NOT NULL,
    restriction VARCHAR(255) NOT NULL,
    FOREIGN KEY (offer_id) REFERENCES offers(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_offer_restrictions_offer_id ON offer_restrictions(offer_id);

-- Phase 2: Expand Redemption entity with code fields
ALTER TABLE IF EXISTS redemptions ADD COLUMN IF NOT EXISTS redemption_code VARCHAR(6) UNIQUE;
ALTER TABLE IF EXISTS redemptions ADD COLUMN IF NOT EXISTS code_expires_at TIMESTAMP;
ALTER TABLE IF EXISTS redemptions ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'GENERATED';
ALTER TABLE IF EXISTS redemptions ADD COLUMN IF NOT EXISTS user_id BIGINT;
ALTER TABLE IF EXISTS redemptions ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT now();

CREATE UNIQUE INDEX IF NOT EXISTS idx_redemption_code ON redemptions(redemption_code);
CREATE INDEX IF NOT EXISTS idx_redemption_status ON redemptions(status);
CREATE INDEX IF NOT EXISTS idx_redemption_user_id ON redemptions(user_id);
CREATE INDEX IF NOT EXISTS idx_redemption_expires_at ON redemptions(code_expires_at);

-- Phase 3: Update RewardTier entity
ALTER TABLE IF EXISTS reward_tiers ADD COLUMN IF NOT EXISTS restaurant_id BIGINT;
ALTER TABLE IF EXISTS reward_tiers ADD COLUMN IF NOT EXISTS points_threshold BIGINT;
ALTER TABLE IF EXISTS reward_tiers ADD COLUMN IF NOT EXISTS tier_order INTEGER;
ALTER TABLE IF EXISTS reward_tiers ADD COLUMN IF NOT EXISTS color VARCHAR(50);

-- Create tier_perks table for perks array
CREATE TABLE IF NOT EXISTS tier_perks (
    tier_id BIGINT NOT NULL,
    perk VARCHAR(255) NOT NULL,
    FOREIGN KEY (tier_id) REFERENCES reward_tiers(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_tier_perks_tier_id ON tier_perks(tier_id);

-- Create RewardItem table
CREATE TABLE IF NOT EXISTS reward_items (
    id BIGSERIAL PRIMARY KEY,
    restaurant_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    points_cost BIGINT NOT NULL,
    icon VARCHAR(100),
    category VARCHAR(100),
    available BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now(),
    FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_reward_items_restaurant_id ON reward_items(restaurant_id);
CREATE INDEX IF NOT EXISTS idx_reward_items_available ON reward_items(available);
CREATE INDEX IF NOT EXISTS idx_reward_items_category ON reward_items(category);

-- Create PointsEarningRule table
CREATE TABLE IF NOT EXISTS points_earning_rules (
    id BIGSERIAL PRIMARY KEY,
    restaurant_id BIGINT NOT NULL,
    action VARCHAR(100) NOT NULL,
    points_value BIGINT NOT NULL,
    description TEXT,
    icon VARCHAR(100),
    clickable BOOLEAN DEFAULT false,
    action_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now(),
    FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_points_earning_rules_restaurant_id ON points_earning_rules(restaurant_id);
CREATE INDEX IF NOT EXISTS idx_points_earning_rules_action ON points_earning_rules(action);

-- Phase 5: Create ReceiptClaim table
CREATE TABLE IF NOT EXISTS receipt_claims (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    restaurant_id BIGINT NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    points_claimed BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'UPLOADED',
    rejection_reason TEXT,
    receipt_amount VARCHAR(100),
    receipt_date VARCHAR(100),
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now(),
    approved_at TIMESTAMP,
    approved_by BIGINT,
    FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_receipt_claims_user_id ON receipt_claims(user_id);
CREATE INDEX IF NOT EXISTS idx_receipt_claims_restaurant_id ON receipt_claims(restaurant_id);
CREATE INDEX IF NOT EXISTS idx_receipt_claims_status ON receipt_claims(status);
CREATE INDEX IF NOT EXISTS idx_receipt_claims_created_at ON receipt_claims(created_at);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_offers_restaurant_status ON offers(restaurant_id, status);
CREATE INDEX IF NOT EXISTS idx_offers_category ON offers(category);
CREATE INDEX IF NOT EXISTS idx_offers_end_date ON offers(end_date);
CREATE INDEX IF NOT EXISTS idx_reward_tiers_restaurant_id ON reward_tiers(restaurant_id);
CREATE INDEX IF NOT EXISTS idx_reward_tiers_tier_order ON reward_tiers(tier_order);
