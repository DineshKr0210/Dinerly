package com.restaurant.waitlist.backend.dto.request;

import lombok.Data;

@Data
public class UpdateSettingsProfileRequest {
    private String name;
    private String email;
    private String phone;
    private String address;
    private Hours hours;

    @Data
    public static class Hours {
        private String open;
        private String close;
    }
}
