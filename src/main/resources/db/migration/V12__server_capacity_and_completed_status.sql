-- Pre-existing bug found while testing this change: staff_role_check predates the
-- SERVER role being added to the StaffRole enum, so creating any staff member with
-- role=SERVER fails outright - the entire server rotation/section feature has been
-- unusable in this environment since it shipped. This constraint isn't defined in any
-- earlier migration (it lives outside Flyway's tracked history), so fix it here.
ALTER TABLE staff DROP CONSTRAINT IF EXISTS staff_role_check;
ALTER TABLE staff ADD CONSTRAINT staff_role_check
    CHECK (role::text = ANY (ARRAY['ADMIN','MANAGER','HOST','SERVER']::text[]));

-- Server workload capacity (flag-only, does not block rotation assignment)
ALTER TABLE server_floor_status ADD COLUMN IF NOT EXISTS max_tables INTEGER NULL;

-- Speeds up "how many tables is this server currently handling" (SEATED count per server).
CREATE INDEX IF NOT EXISTS idx_waitlist_restaurant_assigned_server_status
    ON waitlist(restaurant_id, assigned_server_id, status);

-- Same class of bug as staff_role_check above: waitlist.status (and the mirrored
-- last_active_status) have an untracked CHECK constraint that predates the new
-- COMPLETED value added for this feature. Without this, completing a seating fails.
ALTER TABLE waitlist DROP CONSTRAINT IF EXISTS waitlist_status_check;
ALTER TABLE waitlist ADD CONSTRAINT waitlist_status_check
    CHECK (status::text = ANY (ARRAY['PENDING','WAITING','NOTIFIED','SEATED','COMPLETED','CANCELLED','NO_SHOW']::text[]));

ALTER TABLE waitlist DROP CONSTRAINT IF EXISTS waitlist_last_active_status_check;
ALTER TABLE waitlist ADD CONSTRAINT waitlist_last_active_status_check
    CHECK (last_active_status::text = ANY (ARRAY['PENDING','WAITING','NOTIFIED','SEATED','COMPLETED','CANCELLED','NO_SHOW']::text[]));

-- Third occurrence of the same bug: users.role is also missing SERVER, which would
-- block a server from ever logging into their own account (POST /api/auth/login)
-- after completing /api/admin/staff/set-password - breaking the entire "server's own
-- screen" feature (My Tables, notifications) at the account-creation step.
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;
ALTER TABLE users ADD CONSTRAINT users_role_check
    CHECK (role::text = ANY (ARRAY['GUEST','STAFF','SERVER','HOST','MANAGER','OWNER','ADMIN']::text[]));
