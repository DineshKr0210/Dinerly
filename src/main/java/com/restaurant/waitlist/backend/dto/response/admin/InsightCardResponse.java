package com.restaurant.waitlist.backend.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsightCardResponse {
    private String type; // GROWTH, OPERATIONS, TIPS, ANNOUNCEMENTS
    private String title;
    private String description;
    private String message;
    private String actionLabel;
    private String actionUrl;
    private String metric; // percentage, number, etc.
    private Long locationId;
    private String locationName;
    private Long timestamp;
    private String priority; // HIGH, MEDIUM, LOW
}
