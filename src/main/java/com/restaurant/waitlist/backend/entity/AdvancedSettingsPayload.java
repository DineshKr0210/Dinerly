package com.restaurant.waitlist.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvancedSettingsPayload {

    @Builder.Default
    private Boolean darkMode = false;

    @Builder.Default
    private Boolean desktopNotifications = false;

    @Builder.Default
    private Boolean keepSignedIn = true;

    @Builder.Default
    private String language = "en";

    @Builder.Default
    private String timezone = "Canada/Central";

    public static AdvancedSettingsPayload defaults() {
        return AdvancedSettingsPayload.builder().build();
    }
}
