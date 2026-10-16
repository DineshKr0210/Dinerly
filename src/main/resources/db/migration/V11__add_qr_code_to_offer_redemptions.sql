-- V11__add_qr_code_to_offer_redemptions.sql
-- Adds QR code storage to the redemptions table for Offer redemptions only
-- Not used for Campaign codes or Reward redemptions

ALTER TABLE redemptions ADD COLUMN qr_code_data BYTEA;

-- Add comment to clarify this is for Offers only
COMMENT ON COLUMN redemptions.qr_code_data IS 'Base64 encoded QR code PNG image - ONLY for Offer redemptions, not Campaign or Reward codes';

-- Create index for redemptions with QR codes (for faster lookups if needed)
CREATE INDEX idx_redemptions_qr_code_data ON redemptions USING hash (qr_code_data) WHERE qr_code_data IS NOT NULL;

