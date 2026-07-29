package com.restaurant.waitlist.backend.dto.response;

import com.restaurant.waitlist.backend.entity.HolidayHoursPayload;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class HolidayHoursResponse {
    private List<HolidayHourResponse> holidayHours;

    public static HolidayHoursResponse fromPayload(HolidayHoursPayload payload) {
        return HolidayHoursResponse.builder()
                .holidayHours(payload.getHolidayHours().stream()
                        .map(HolidayHourResponse::fromPayload)
                        .collect(Collectors.toList()))
                .build();
    }
}
