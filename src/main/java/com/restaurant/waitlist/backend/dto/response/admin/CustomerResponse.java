package com.restaurant.waitlist.backend.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponse {
    private String guest;
    private String contact;
    private String locations; // comma-separated
    private Long visits;
    private LocalDate firstVisit;
    private LocalDate lastVisit;
    private String status; // "Regular" or "New"
}
