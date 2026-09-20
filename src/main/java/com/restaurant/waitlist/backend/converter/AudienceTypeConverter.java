package com.restaurant.waitlist.backend.converter;

import com.restaurant.waitlist.backend.enums.AudienceType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Converter(autoApply = true)
@Slf4j
public class AudienceTypeConverter implements AttributeConverter<AudienceType, String> {
    
    @Override
    public String convertToDatabaseColumn(AudienceType attribute) {
        return attribute == null ? AudienceType.ALL.getCode() : attribute.getCode();
    }

    @Override
    public AudienceType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return AudienceType.ALL;
        }
        
        try {
            return AudienceType.fromCode(dbData);
        } catch (IllegalArgumentException ex) {
            // Log invalid data and default to ALL instead of throwing exception
            log.warn("Invalid audience type value in database: '{}'. Defaulting to ALL", dbData);
            return AudienceType.ALL;
        }
    }
}
