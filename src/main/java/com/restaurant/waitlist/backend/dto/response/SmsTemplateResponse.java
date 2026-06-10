package com.restaurant.waitlist.backend.dto.response;

import com.restaurant.waitlist.backend.entity.SmsTemplate;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SmsTemplateResponse {
    private Long id;
    private String templateType;
    private String messageTemplate;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SmsTemplateResponse fromSmsTemplate(SmsTemplate template) {
        return SmsTemplateResponse.builder()
                .id(template.getId())
                .templateType(template.getTemplateType())
                .messageTemplate(template.getMessageTemplate())
                .description(template.getDescription())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}

