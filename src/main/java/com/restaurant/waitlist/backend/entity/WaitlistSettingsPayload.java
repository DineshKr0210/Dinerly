package com.restaurant.waitlist.backend.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaitlistSettingsPayload {

    @Builder.Default
    @JsonProperty("maxPartySize")
    private Integer maxPartySize = 6;

    @Builder.Default
    @JsonProperty("tableReadyResponseMinutes")
    private Integer tableReadyResponseMinutes = 10;

    @Builder.Default
    @JsonProperty("walkInsOnly")
    private Boolean walkInsOnly = false;

    @Builder.Default
    @JsonProperty("pauseNewJoinsAfterClosing")
    private Boolean pauseNewJoinsAfterClosing = false;

    @Builder.Default
    @JsonProperty("allowGoogleJoin")
    private Boolean allowGoogleJoin = true;

    @Builder.Default
    @JsonProperty("acceptOnlineJoin")
    private Boolean acceptOnlineJoin = true;

    public static WaitlistSettingsPayload defaults() {
        return WaitlistSettingsPayload.builder().build();
    }
}
