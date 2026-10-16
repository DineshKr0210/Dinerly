-- Server order notes: free-text notes a server can attach to their currently-seated
-- table (what the guest wants to order, special requests, allergies, etc.)
ALTER TABLE waitlist ADD COLUMN IF NOT EXISTS order_notes TEXT NULL;
ALTER TABLE waitlist ADD COLUMN IF NOT EXISTS order_notes_updated_at TIMESTAMP NULL;

