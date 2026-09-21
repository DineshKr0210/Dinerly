package com.restaurant.waitlist.backend.enums;

public enum ReportType {
    OVERALL("overall", "All locations"),
    LOCATION("location", "Single location"),
    PERFORMANCE("performance", "Performance overview"),
    REDEMPTION("redemption", "Redemption overview"),
    CUSTOMER("customer", "Customer activity");

    private final String value;
    private final String displayName;

    ReportType(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public String getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ReportType fromValue(String value) {
        if (value == null || value.isBlank()) {
            return OVERALL;
        }
        for (ReportType type : values()) {
            if (type.value.equalsIgnoreCase(value.trim())) {
                return type;
            }
        }
        return OVERALL;
    }
}
