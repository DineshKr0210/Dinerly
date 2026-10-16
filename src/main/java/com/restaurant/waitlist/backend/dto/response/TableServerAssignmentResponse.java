package com.restaurant.waitlist.backend.dto.response;

import com.restaurant.waitlist.backend.entity.TableServerAssignment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableServerAssignmentResponse {

    private Long id;
    private Long tableId;
    private String tableName;
    private Long assignedServerId;
    private String serverName;
    private String sectionName;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TableServerAssignmentResponse fromEntity(TableServerAssignment assignment, String tableName, String serverName) {
        return TableServerAssignmentResponse.builder()
                .id(assignment.getId())
                .tableId(assignment.getTable() != null ? assignment.getTable().getId() : null)
                .tableName(tableName)
                .assignedServerId(assignment.getAssignedServerId())
                .serverName(serverName)
                .sectionName(assignment.getSectionName())
                .isActive(assignment.getIsActive())
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                .build();
    }
}

