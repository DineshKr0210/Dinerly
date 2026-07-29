package com.restaurant.waitlist.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HolidayHourRequest {

    @NotBlank(message = "Holiday date is required")
    private String date;

    private String title;
    private String openTime;
    private String closeTime;
    private String notes;
    private Boolean closed;
}
