package com.restaurant.waitlist.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HolidayHoursPayload {

    @Builder.Default
    private List<HolidayHourPayload> holidayHours = new ArrayList<>();

    public static HolidayHoursPayload defaults() {
        return HolidayHoursPayload.builder().build();
    }
}
