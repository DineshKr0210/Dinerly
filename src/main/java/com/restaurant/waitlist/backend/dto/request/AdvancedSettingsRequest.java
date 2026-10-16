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

    // Optional: "ROTATION" (fair round-robin, default) or "SECTION" (servers own
    // fixed tables). Left out of an update request, the current value is kept.
    private String serverAssignmentMode;
}
