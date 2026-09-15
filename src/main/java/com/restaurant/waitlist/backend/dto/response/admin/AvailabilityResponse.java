package com.restaurant.waitlist.backend.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityResponse {
    private Long locationId;
    private String location;
    private Boolean currentlyOpen;
    private List<RegularHour> regularHours;
    private List<SpecialHour> specialHours;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegularHour {
        private String date;
        private String day;
        private String open;
        private String close;
        private Boolean enabled;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpecialHour {
        private Long id;
        private String date;
        private String open;
        private String close;
        private Boolean enabled;
    }
}
