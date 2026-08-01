package com.restaurant.waitlist.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoveToWaitingRequest {

    @JsonProperty("Status")
    @JsonAlias({"status"})
    @NotBlank(message = "Status is required")
    private String status;

    @NotNull(message = "partySize is required")
    @Positive(message = "partySize must be positive")
    private Integer partySize;

    @NotNull(message = "estimatedWaitTime is required")
    @Positive(message = "estimatedWaitTime must be positive")
    private Integer estimatedWaitTime;
}
