package com.restaurant.waitlist.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateHolidayHourRequest {
    private String date;
    private String title;
    private String openTime;
    private String closeTime;
    private String notes;
    private Boolean closed;
}
