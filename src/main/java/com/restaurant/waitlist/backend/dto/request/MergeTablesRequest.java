package com.restaurant.waitlist.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MergeTablesRequest {

    @NotNull(message = "tableId is required")
    private Long tableId;

    @NotNull(message = "mergedTableId is required")
    private Long mergedTableId;
}
