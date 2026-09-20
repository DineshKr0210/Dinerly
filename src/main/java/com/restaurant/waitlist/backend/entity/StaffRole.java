package com.restaurant.waitlist.backend.entity;

/**
 * Valid staff roles for restaurant operations.
 * Maps to User.UserRole for permission-based access control.
 */
public enum StaffRole {
    ADMIN("Owner"),           // Account-level administrator
    MANAGER("Manager"),       // Location-level operations manager
    HOST("Host");             // Front desk staff

    private final String displayName;

    StaffRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Validates if a role string is a valid staff role.
     */
    public static boolean isValid(String role) {
        if (role == null || role.isBlank()) {
            return false;
        }
        try {
            StaffRole.valueOf(role.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Gets the StaffRole from a string value.
     */
    public static StaffRole fromString(String role) {
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Staff role cannot be null or blank");
        }
        try {
            return StaffRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid staff role: " + role + ". Valid roles are: ADMIN, MANAGER, HOST");
        }
    }
}
