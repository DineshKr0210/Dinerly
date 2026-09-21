package com.restaurant.waitlist.backend.dto.response.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StaffPermissionResponse {
    private Long staffId;
    private String role;
    private Boolean canManageOffers;
    private Boolean canManageStaff;
    private Boolean canViewReports;
    private Boolean canManageSettings;
    private Boolean canManageRewards;
    private Map<String, Boolean> effectivePermissions;
    private Boolean isCustom;
    private LocalDateTime updatedAt;
}
