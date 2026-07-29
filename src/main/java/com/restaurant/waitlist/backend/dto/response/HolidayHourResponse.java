package com.restaurant.waitlist.backend.dto.response;

import com.restaurant.waitlist.backend.entity.HolidayHourPayload;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HolidayHourResponse {
    private String id;
    private String date;
    private String title;
    private String openTime;
    private String closeTime;
    private String notes;
    private Boolean closed;

    public static HolidayHourResponse fromPayload(HolidayHourPayload payload) {
        return HolidayHourResponse.builder()
                .id(payload.getId())
                .date(payload.getDate())
                .title(payload.getTitle())
                .openTime(payload.getOpenTime())
                .closeTime(payload.getCloseTime())
                .notes(payload.getNotes())
                .closed(payload.getClosed())
                .build();
    }
}
