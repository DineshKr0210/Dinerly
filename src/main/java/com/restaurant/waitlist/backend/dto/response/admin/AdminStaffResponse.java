package com.restaurant.waitlist.backend.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminStaffResponse {
    private Long id;
    private String name;
    private String role;
    private Long locationId;
    private String location;
    private String email;
    private String status;
}
