package com.restaurant.waitlist.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdvancedSettingsRequest {
    @NotNull
    private Boolean darkMode;

    @NotNull
    private Boolean desktopNotifications;

    @NotNull
    private Boolean keepSignedIn;

    @NotNull
    private String language;

    @NotNull
    private String timezone;
}
