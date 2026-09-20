package com.restaurant.waitlist.backend.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportResponse {
    private Long id;
    private String fileName;
    private String type;              // "overall" or "location"
    private String scope;              // "All locations" or location name
    private Long locationId;
    private String locationName;       // Display name for location (if applicable)
    private String period;
    private String dateRange;          // Display format: "Aug 1 - Aug 31"
    private LocalDateTime generatedAt;
}
