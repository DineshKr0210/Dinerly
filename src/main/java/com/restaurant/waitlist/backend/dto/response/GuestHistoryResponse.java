package com.restaurant.waitlist.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuestHistoryResponse {
    private String name;
    private String phone;
    private Long visits;
    private Double avgWait;
    private LocalDate lastVisit;
    private Double rating;
}

