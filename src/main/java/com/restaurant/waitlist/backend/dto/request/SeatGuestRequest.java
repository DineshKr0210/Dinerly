package com.restaurant.waitlist.backend.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SeatGuestRequest {
    @Size(max = 100)
    private String tableName;

    // Optional: the actual floor-plan Table being seated at. When provided, the table's
    // status/current server are updated and the party is linked for the server's "my tables" view.
    private Long tableId;

    // Optional: explicitly choose which server (Staff.id, role=SERVER) gets this seating.
    // When null, the current "Next Up" server in the rotation is used automatically.
    private Long serverId;
}

