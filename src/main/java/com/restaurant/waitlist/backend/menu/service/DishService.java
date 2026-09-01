package com.restaurant.waitlist.backend.menu.service;

import com.restaurant.waitlist.backend.menu.dto.DishDTO;
import com.restaurant.waitlist.backend.menu.dto.DishRequestDTO;

import java.util.List;

public interface DishService {
    List<DishDTO> getAllDishesWithRelations(Long locationId);
    List<DishDTO> getDishesByCategory(String categoryName, Long locationId);
    List<DishDTO> getDishesByType(String type, Long locationId);
    DishDTO createDish(DishRequestDTO dto);
    DishDTO updateDish(Long id, DishRequestDTO dto);
    void deleteDishById(Long id, Long locationId) ;

    List<DishDTO> getDishesByCategoryAdmin(String categoryName, Long locationId);

    void restoreDishById(Long id, Long locationId);

    List<DishDTO> getDishesByID(Long id, Long locationId);

}
