package com.restaurant.waitlist.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HolidayHourPayload {

    @Builder.Default
    private String id = UUID.randomUUID().toString();

    private String date;
    private String title;
    private String openTime;
    private String closeTime;
    private String notes;

    @Builder.Default
    private Boolean closed = false;
}
