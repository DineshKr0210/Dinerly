package com.restaurant.waitlist.backend.dto.request.admin;

import com.restaurant.waitlist.backend.validation.ValidStaffRole;
import lombok.Data;

@Data
public class StaffUpdateRequest {
    private String name;
    
    @ValidStaffRole
    private String role;  // ADMIN (Owner), MANAGER (Manager), HOST (Front Desk)
    
    private String email;
    private Long locationId;
    private String status;  // ACTIVE, INVITED, INACTIVE
}
