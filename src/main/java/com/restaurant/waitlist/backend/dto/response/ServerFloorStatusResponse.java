package com.restaurant.waitlist.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

    // How many tables this server currently has seated (live count, not cumulative).
    private Integer currentTableCount;
    // Null/0 = unlimited. A visibility flag only - does not block rotation assignment.
    private Integer maxTables;
    private Boolean atCapacity;

    // SECTION mode only: the tables this server currently owns. Null/empty in ROTATION mode.
    private List<String> assignedTableNames;
}

