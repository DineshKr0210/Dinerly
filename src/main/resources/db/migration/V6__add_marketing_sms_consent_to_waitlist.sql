-- Flyway migration: Marketing SMS consent
-- Date: 2026-09-23
-- Captures whether a guest agreed to receive marketing SMS when joining the
-- waitlist, so campaign sends can be limited to consenting guests only.

ALTER TABLE IF EXISTS waitlist ADD COLUMN IF NOT EXISTS marketing_sms_consent BOOLEAN NOT NULL DEFAULT false;
