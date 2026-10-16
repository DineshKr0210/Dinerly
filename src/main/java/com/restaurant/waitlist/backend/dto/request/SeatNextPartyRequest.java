package com.restaurant.waitlist.backend.dto.request;

import lombok.Data;

@Data
public class SeatNextPartyRequest {
    // Optional: explicitly choose which server (Staff.id, role=SERVER) gets this seating.
    // When null, the restaurant's configured assignment mode (rotation or section) decides.
    private Long serverId;
}
