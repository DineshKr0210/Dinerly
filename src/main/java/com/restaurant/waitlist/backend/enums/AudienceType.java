package com.restaurant.waitlist.backend.enums;

public enum AudienceType {
    ALL("ALL", "All customers"),
    RECENT_30D("RECENT_30D", "Visited in last 30 days"),
    LAPSED_30D("LAPSED_30D", "Haven't visited in 30+ days"),
    GOLD_PLATINUM("GOLD_PLATINUM", "Loyal customers (5+ visits)"),
    HIGH_SPENDER("HIGH_SPENDER", "High average transaction value"),
    NEW_CUSTOMERS("NEW_CUSTOMERS", "Joined in last 30 days");

    private final String code;
    private final String description;

    AudienceType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static AudienceType fromCode(String code) {
        if (code == null || code.isBlank()) {
            return ALL; // Default to ALL if not specified
        }
        for (AudienceType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown audience type: " + code);
    }
}
