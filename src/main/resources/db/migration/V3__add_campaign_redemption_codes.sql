-- ============================================================================
-- Migration V3: Add Campaign Redemption Code Support
-- 
-- Purpose: Enable campaign-specific redemption codes with tracking
-- Changes:
--   1. Add has_redemption_code flag to campaigns table
--   2. Create indexes for campaign code performance
-- ============================================================================

-- Add has_redemption_code column to campaigns table
-- false = general marketing campaign (no codes)
-- true = offer/coupon campaign (codes generated at publish)
ALTER TABLE campaigns
    ADD COLUMN has_redemption_code BOOLEAN NOT NULL DEFAULT false;

-- Create index for faster campaign code lookups
CREATE INDEX idx_campaigns_has_redemption_code ON campaigns(has_redemption_code);

-- Existing FK constraint on redemptions.campaign_id should already be in place from V2
-- Verify: FOREIGN KEY (campaign_id) REFERENCES campaigns(id) ON DELETE SET NULL

-- Create index for campaign-based redemption queries
CREATE INDEX idx_redemptions_campaign_status ON redemptions(campaign_id, status);

-- Create index for code expiry queries
CREATE INDEX idx_redemptions_code_expires_at ON redemptions(code_expires_at);

-- ============================================================================
-- Verification Queries
-- ============================================================================
-- Check column added:
-- SELECT column_name, data_type FROM information_schema.columns 
-- WHERE table_name = 'campaigns' AND column_name = 'has_redemption_code';

-- Check indexes created:
-- SELECT indexname FROM pg_indexes WHERE tablename = 'redemptions' 
-- AND indexname LIKE 'idx_redemptions_%';
