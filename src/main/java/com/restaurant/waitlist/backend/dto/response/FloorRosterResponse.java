package com.restaurant.waitlist.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * The floor roster, plus which assignment mode (ROTATION/SECTION) the restaurant is
 * currently running - so a client never has to make a second call just to know which
 * fields on each server row (nextUp vs assignedTableNames) actually apply.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FloorRosterResponse {
    private String serverAssignmentMode;
    private List<ServerFloorStatusResponse> servers;
}
