package com.restaurant.waitlist.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RestaurantOverviewResponse {
    private RestaurantResponse profile;
    private RestaurantSettingsResponse settings;
    private List<StaffResponse> staff;
    private FloorSummary floor;

    @Data
    @Builder
    public static class FloorSummary {
        private Integer totalTables;
        private Integer totalSeats;
        private Integer staffOnFile;
    }
}
