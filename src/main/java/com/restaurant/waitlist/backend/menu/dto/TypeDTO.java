package com.restaurant.waitlist.backend.menu.dto;

import com.restaurant.waitlist.backend.menu.model.enums.DishType;
import com.restaurant.waitlist.backend.menu.model.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TypeDTO {
    private Long id;
    private DishType name;
    private Status status;
    private Long locationId;
    private String location;
}
