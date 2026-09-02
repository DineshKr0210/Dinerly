package com.restaurant.waitlist.backend.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LocationResponse {
    private Long id;
    private String name;
    private String address;
    private String phoneNumber;
    private String managerEmail;
    private String ownerName;
    private String ownerEmail;
    private String menuTemplate;
    private Integer seats;
    private Boolean locationOpen;
}
