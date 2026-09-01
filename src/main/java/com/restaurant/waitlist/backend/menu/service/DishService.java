package com.restaurant.waitlist.backend.menu.service;

import com.restaurant.waitlist.backend.menu.dto.DishDTO;
import com.restaurant.waitlist.backend.menu.dto.DishRequestDTO;

import java.util.List;

public interface DishService {
    List<DishDTO> getAllDishesWithRelations();
    List<DishDTO> getDishesByCategory(String categoryName);
    List<DishDTO> getDishesByType(String type);
    DishDTO createDish(DishRequestDTO dto);
    DishDTO updateDish(Long id, DishRequestDTO dto);
    void deleteDishById(Long id) ;

    List<DishDTO> getDishesByCategoryAdmin(String categoryName);

    void restoreDishById(Long id);

    List<DishDTO> getDishesByID(Long id);

}
