package com.restaurant.waitlist.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateSmsTemplateRequest {
    @NotBlank(message = "templateType is required")
    private String templateType;

    @NotBlank(message = "messageTemplate is required")
    private String messageTemplate;

    private String description;
}
