package com.restaurant.waitlist.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignServerToTableRequest {

    private Long serverId;
    
    private String sectionName; // Optional: e.g. "Window", "Bar", "Patio"
}

