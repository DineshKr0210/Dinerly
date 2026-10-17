package com.restaurant.waitlist.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateServerFloorRequest {
    private Boolean onFloor;

    // Optional: how many tables this server can handle at once (flag-only, see
    // ServerFloorStatusResponse.atCapacity). Omit/null to leave the current value
    // unchanged; send 0 to explicitly reset to unlimited.
    private Integer maxTables;
}

