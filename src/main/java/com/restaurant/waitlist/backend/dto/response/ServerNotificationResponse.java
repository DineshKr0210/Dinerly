package com.restaurant.waitlist.backend.dto.response;

import com.restaurant.waitlist.backend.entity.ServerNotification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServerNotificationResponse {
    private Long id;
    private Long waitlistId;
    private Long tableId;
    private String type;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;

    public static ServerNotificationResponse fromEntity(ServerNotification n) {
        return ServerNotificationResponse.builder()
                .id(n.getId())
                .waitlistId(n.getWaitlistId())
                .tableId(n.getTableId())
                .type(n.getType() != null ? n.getType().name() : null)
                .message(n.getMessage())
                .isRead(n.getIsRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}

