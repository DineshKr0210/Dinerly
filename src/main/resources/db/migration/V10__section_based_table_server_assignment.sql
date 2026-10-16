-- V10__section_based_table_server_assignment.sql
-- Creates the table_server_assignment table for Scenario 2 (Section-Based Assignment)
-- and adds serverAssignmentMode field to advanced_settings

-- Create table_server_assignment table
CREATE TABLE IF NOT EXISTS table_server_assignment (
    id BIGSERIAL PRIMARY KEY,
    restaurant_id BIGINT NOT NULL,
    table_id BIGINT NOT NULL UNIQUE,
    assigned_server_id BIGINT NOT NULL,
    section_name VARCHAR(100),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE,
    FOREIGN KEY (table_id) REFERENCES tables(id) ON DELETE CASCADE
);

-- Create index for faster lookups
CREATE INDEX idx_table_server_assignment_restaurant_id ON table_server_assignment(restaurant_id);
CREATE INDEX idx_table_server_assignment_assigned_server_id ON table_server_assignment(assigned_server_id);
CREATE INDEX idx_table_server_assignment_is_active ON table_server_assignment(is_active);

-- Add comment explaining the new feature
COMMENT ON TABLE table_server_assignment IS 'Scenario 2: Section-based table-to-server assignments. Each table is permanently assigned to a server.';
COMMENT ON COLUMN table_server_assignment.section_name IS 'Optional section identifier (e.g., Window, Bar, Patio)';
COMMENT ON COLUMN table_server_assignment.is_active IS 'Soft delete flag; false means assignment is deactivated';

