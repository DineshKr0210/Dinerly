-- Flyway migration: Franchise location scoping for Admin
-- Date: 2026-09-23
-- Adds a self-reference so a franchise location can point back to its main
-- restaurant. NULL means the row is a main/standalone restaurant.

ALTER TABLE IF EXISTS restaurants ADD COLUMN IF NOT EXISTS main_restaurant_id BIGINT;

ALTER TABLE IF EXISTS restaurants
    ADD CONSTRAINT IF NOT EXISTS fk_restaurants_main_restaurant
    FOREIGN KEY (main_restaurant_id) REFERENCES restaurants(id);

CREATE INDEX IF NOT EXISTS idx_restaurants_main_restaurant_id ON restaurants(main_restaurant_id);
