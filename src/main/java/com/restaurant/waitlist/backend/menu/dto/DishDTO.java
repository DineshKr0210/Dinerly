package com.restaurant.waitlist.backend.menu.dto;

import com.restaurant.waitlist.backend.menu.model.enums.DishType;
import com.restaurant.waitlist.backend.menu.model.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DishDTO {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer calories;
    private String imageUrl;
    private String category;
    private Long locationId;
    private String location;
    private List<TypeDTO> types;
    private Status status;

}
