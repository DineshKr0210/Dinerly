package com.restaurant.waitlist.backend.dto.response;

import com.restaurant.waitlist.backend.entity.AdvancedSettingsPayload;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdvancedSettingsResponse {
    private Boolean darkMode;
    private Boolean desktopNotifications;
    private Boolean keepSignedIn;
    private String language;
    private String timezone;

    public static AdvancedSettingsResponse fromPayload(AdvancedSettingsPayload payload) {
        return AdvancedSettingsResponse.builder()
                .darkMode(payload.getDarkMode())
                .desktopNotifications(payload.getDesktopNotifications())
                .keepSignedIn(payload.getKeepSignedIn())
                .language(payload.getLanguage())
                .timezone(payload.getTimezone())
                .build();
    }
}
