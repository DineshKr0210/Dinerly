package com.restaurant.waitlist.backend.dto.response;

import com.restaurant.waitlist.backend.entity.Staff;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StaffResponse {
    private Long id;
    private String name;
    private String role;

    public static StaffResponse fromStaff(Staff staff) {
        return StaffResponse.builder()
                .id(staff.getId())
                .name(staff.getName())
                .role(staff.getRole())
                .build();
    }
}
