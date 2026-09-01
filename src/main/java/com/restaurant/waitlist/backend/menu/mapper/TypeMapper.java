package com.restaurant.waitlist.backend.menu.mapper;

import com.restaurant.waitlist.backend.menu.dto.TypeDTO;
import com.restaurant.waitlist.backend.menu.model.Type;

public class TypeMapper {
    public static TypeDTO toDTO(Type type) {
        if (type == null) return null;
        return TypeDTO.builder()
                .id(type.getId() != null ? Long.valueOf(type.getId()) : null)
                .name(type.getName())
                .status(type.getStatus())
                .build();
    }
}
