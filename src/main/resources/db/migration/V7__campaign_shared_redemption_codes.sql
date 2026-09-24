-- ============================================================================
-- Migration V7: Shared Campaign Redemption Codes
--
-- Purpose: Move from one auto-generated code per recipient to a single shared
-- coupon code per campaign (e.g. "BROTPIZZA50"), identical for every guest.
-- ============================================================================

-- The shared coupon code now lives on the campaign itself.
ALTER TABLE campaigns ADD COLUMN IF NOT EXISTS redemption_code VARCHAR(20);
CREATE UNIQUE INDEX IF NOT EXISTS idx_campaigns_redemption_code
    ON campaigns(redemption_code) WHERE redemption_code IS NOT NULL;

-- redemptions.redemption_code is no longer globally unique: the same campaign
-- code is reused across every guest who redeems it. Offer/reward codes remain
-- unique in practice (enforced at the application level).
DROP INDEX IF EXISTS idx_redemption_code;
ALTER TABLE redemptions ALTER COLUMN redemption_code TYPE VARCHAR(20);

-- Speeds up the "has this guest already redeemed this campaign's code" check.
CREATE INDEX IF NOT EXISTS idx_redemptions_campaign_guest_phone ON redemptions(campaign_id, guest_phone);
