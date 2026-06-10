package com.restaurant.waitlist.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateSmsTemplateRequest {
    @NotBlank(message = "Message template is required")
    private String messageTemplate;

    private String description;
}

