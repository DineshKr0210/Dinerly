package com.restaurant.waitlist.backend.dto.request.admin;

import lombok.Data;

@Data
public class StaffUpdateRequest {
    private String name;
    private String role;  // Owner, Manager, Staff
    private String email;
    private Long locationId;
    private String status;  // Active, Invited, Inactive
}
