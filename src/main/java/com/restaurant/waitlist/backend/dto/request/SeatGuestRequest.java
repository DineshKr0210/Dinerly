package com.restaurant.waitlist.backend.dto.request;

import lombok.Data;

@Data
public class SeatGuestRequest {
    // Optional: the actual floor-plan Table being seated at. When provided, the table's
    // status/current server are updated, the party is linked for the server's "my tables"
    // view, and the display name (Waitlist.tableName) is derived from the table itself -
    // no separate tableName field needed.
    private Long tableId;

    // Optional: explicitly choose which server (Staff.id, role=SERVER) gets this seating.
    // When null, the current "Next Up" server in the rotation is used automatically.
    private Long serverId;
}

