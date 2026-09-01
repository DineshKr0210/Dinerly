package com.restaurant.waitlist.backend.menu.dto;

import com.restaurant.waitlist.backend.menu.model.enums.Status;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryDTO {
    private Long id;
    private String name;
    private String description;
    private List<DishDTO> dishes;
    private Status status;
}
