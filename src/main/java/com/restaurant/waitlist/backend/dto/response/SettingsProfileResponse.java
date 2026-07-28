package com.restaurant.waitlist.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SettingsProfileResponse {
    private ProfileResponse profile;

    @Data
    @Builder
    public static class ProfileResponse {
        private RestaurantProfileResponse restaurant;
        private PlanResponse plan;
    }

    @Data
    @Builder
    public static class RestaurantProfileResponse {
        private Long id;
        private String name;
        private String email;
        private String phone;
        private String address;
        private HoursResponse hours;
    }

    @Data
    @Builder
    public static class HoursResponse {
        private String open;
        private String close;
    }

    @Data
    @Builder
    public static class PlanResponse {
        private String name;
        private Integer smssentthismonth;
        private Integer marketingsmssentthismonth;
        private Double smsChargesThisMonth;
        private Double callChargesThisMonth;
        private Double totalChargesThisMonth;
        private String nextRenewal;
    }
}
