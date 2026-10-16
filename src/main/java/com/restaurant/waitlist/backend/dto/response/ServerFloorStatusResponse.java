package com.restaurant.waitlist.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServerFloorStatusResponse {
    private Long staffId;
    private String name;
    private Boolean onFloor;
    private Integer seatedCount;
    private Integer rotationOrder;
    private Boolean nextUp;
}

