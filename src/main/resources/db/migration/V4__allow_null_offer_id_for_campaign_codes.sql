-- ============================================================================
-- Migration V4: Allow NULL offer_id for Campaign-Generated Codes
--
-- Purpose: Support campaign-generated redemption codes that aren't tied to offers
-- Changes:
--   1. Make offer_id nullable in redemptions table
--   2. Allow campaigns to generate codes independently
--
-- Rationale:
--   - Campaign codes can be generated without an associated offer
--   - Offer-based redemptions still work with offer_id set
--   - Campaign-based redemptions have offer_id = NULL
-- ============================================================================

-- Alter redemptions table to allow NULL offer_id
ALTER TABLE redemptions
    MODIFY COLUMN offer_id BIGINT NULL;

-- Create index for faster campaign-only redemption queries
CREATE INDEX idx_redemptions_campaign_only ON redemptions(campaign_id) WHERE offer_id IS NULL;

-- ============================================================================
-- Verification Query:
-- ============================================================================
-- SELECT COLUMN_NAME, IS_NULLABLE, COLUMN_KEY
-- FROM INFORMATION_SCHEMA.COLUMNS
-- WHERE TABLE_NAME = 'redemptions' AND COLUMN_NAME = 'offer_id';
-- Expected: IS_NULLABLE = YES, COLUMN_KEY = MUL (foreign key)

