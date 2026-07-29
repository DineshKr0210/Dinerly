package com.restaurant.waitlist.backend.dto.response;

import com.restaurant.waitlist.backend.entity.RestaurantSettings;
import com.restaurant.waitlist.backend.entity.WaitlistSettingsPayload;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WaitlistSettingsResponse {
    private Integer maxPartySize;
    private Integer tableReadyResponseMinutes;
    private Boolean walkInsOnly;
    private Boolean pauseNewJoinsAfterClosing;
    private Boolean allowGoogleJoin;
    private Boolean acceptOnlineJoin;

    public static WaitlistSettingsResponse fromSettings(RestaurantSettings settings) {
        WaitlistSettingsPayload payload = settings.getWaitlistSettings();
        return WaitlistSettingsResponse.builder()
                .maxPartySize(payload.getMaxPartySize())
                .tableReadyResponseMinutes(payload.getTableReadyResponseMinutes())
                .walkInsOnly(payload.getWalkInsOnly())
                .pauseNewJoinsAfterClosing(payload.getPauseNewJoinsAfterClosing())
                .allowGoogleJoin(payload.getAllowGoogleJoin())
                .acceptOnlineJoin(payload.getAcceptOnlineJoin())
                .build();
    }
}
