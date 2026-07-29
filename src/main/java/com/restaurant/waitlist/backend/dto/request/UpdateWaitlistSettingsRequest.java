package com.restaurant.waitlist.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateWaitlistSettingsRequest {
    private Integer maxPartySize;

    private Integer tableReadyResponseMinutes;

    private Boolean walkInsOnly;

    private Boolean pauseNewJoinsAfterClosing;

    private Boolean allowGoogleJoin;

    private Boolean acceptOnlineJoin;
}
