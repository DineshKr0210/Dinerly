-- =====================================================
-- ROLE & PERMISSION SYSTEM - DATABASE MIGRATION
-- PostgreSQL Version - CORRECTED (Supabase/PG databases)
-- =====================================================

-- STEP 1: Add new columns to users table
    ALTER TABLE users ADD COLUMN IF NOT EXISTS staff_id BIGINT UNIQUE DEFAULT NULL;

-- STEP 2: Add new columns to staff table
ALTER TABLE staff ADD COLUMN IF NOT EXISTS user_id BIGINT UNIQUE DEFAULT NULL;

-- STEP 3: Convert text column temporarily to handle migration
-- Create a temporary text column
ALTER TABLE users ADD COLUMN role_temp TEXT;

-- Copy data and convert RESTAURANT to OWNER
UPDATE users SET role_temp = CASE
    WHEN role::text = 'RESTAURANT' THEN 'OWNER'
    ELSE role::text
END;

-- Drop the old enum column
ALTER TABLE users DROP COLUMN role;

-- Rename temp column
    ALTER TABLE users RENAME COLUMN role_temp TO role;

-- STEP 4: Create new enum type with all 6 roles
CREATE TYPE user_role AS ENUM(
    'GUEST',
    'ADMIN',
    'OWNER',
    'MANAGER',
    'HOST',
    'STAFF'
);

-- STEP 5: Convert role column to new enum type
ALTER TABLE users ALTER COLUMN role TYPE user_role USING role::user_role;

-- STEP 6: Add foreign key constraints (if not already present)
ALTER TABLE users ADD CONSTRAINT IF NOT EXISTS fk_users_restaurant_id
    FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE SET NULL;

ALTER TABLE users ADD CONSTRAINT IF NOT EXISTS fk_users_staff_id
    FOREIGN KEY (staff_id) REFERENCES staff(id) ON DELETE SET NULL;

ALTER TABLE staff ADD CONSTRAINT IF NOT EXISTS fk_staff_user_id
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- STEP 7: Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_restaurant_id ON users(restaurant_id);
CREATE INDEX IF NOT EXISTS idx_users_staff_id ON users(staff_id);
CREATE INDEX IF NOT EXISTS idx_staff_user_id ON staff(user_id);

-- STEP 8: Verify migration
-- Run these queries to verify:
SELECT DISTINCT role FROM users;  -- Should show: OWNER, GUEST, ADMIN, MANAGER, HOST, STAFF
SELECT COUNT(*) FROM users WHERE role::text = 'RESTAURANT';  -- Should be 0