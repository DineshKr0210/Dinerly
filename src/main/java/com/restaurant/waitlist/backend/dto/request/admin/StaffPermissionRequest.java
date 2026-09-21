package com.restaurant.waitlist.backend.dto.request.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffPermissionRequest {
    private Boolean canManageOffers;
    private Boolean canManageStaff;
    private Boolean canViewReports;
    private Boolean canManageSettings;
    private Boolean canManageRewards;
}
