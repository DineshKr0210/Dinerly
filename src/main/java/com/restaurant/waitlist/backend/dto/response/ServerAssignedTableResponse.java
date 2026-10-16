package com.restaurant.waitlist.backend.dto.response;

import com.restaurant.waitlist.backend.entity.Waitlist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * A single table currently assigned to a server, shown on the server's own "My Tables" screen.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServerAssignedTableResponse {
    private Long waitlistId;
    private Long tableId;
    private String tableName;
    private String guestName;
    private Integer partySize;
    private String notes;
    private String preference;
    private LocalDateTime seatedAt;
    private String orderNotes;
    private LocalDateTime orderNotesUpdatedAt;

    public static ServerAssignedTableResponse fromWaitlist(Waitlist w) {
        return ServerAssignedTableResponse.builder()
                .waitlistId(w.getId())
                .tableId(w.getTableId())
                .tableName(w.getTableName())
                .guestName(w.getGuestName())
                .partySize(w.getPartySize())
                .notes(w.getNotes())
                .preference(w.getPreference())
                .seatedAt(w.getSeatedAt())
                .orderNotes(w.getOrderNotes())
                .orderNotesUpdatedAt(w.getOrderNotesUpdatedAt())
                .build();
    }
}

