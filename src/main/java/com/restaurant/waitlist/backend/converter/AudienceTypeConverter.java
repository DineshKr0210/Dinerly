package com.restaurant.waitlist.backend.converter;

import com.restaurant.waitlist.backend.enums.AudienceType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AudienceTypeConverter implements AttributeConverter<AudienceType, String> {
    
    @Override
    public String convertToDatabaseColumn(AudienceType attribute) {
        return attribute == null ? AudienceType.ALL.getCode() : attribute.getCode();
    }

    @Override
    public AudienceType convertToEntityAttribute(String dbData) {
        return dbData == null || dbData.isBlank() ? AudienceType.ALL : AudienceType.fromCode(dbData);
    }
}
