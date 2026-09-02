package com.restaurant.waitlist.backend.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {
    private Long id;
    private String guest;
    private Long locationId;
    private String location;
    private Integer rating;
    private String review;
    private String reply;
    private LocalDateTime createdAt;
    private LocalDateTime repliedAt;
}
