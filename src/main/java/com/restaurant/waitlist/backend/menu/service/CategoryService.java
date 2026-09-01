package com.restaurant.waitlist.backend.menu.service;

import com.restaurant.waitlist.backend.menu.dto.CategoryDTO;
import com.restaurant.waitlist.backend.menu.dto.CategoryRequestDTO;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface CategoryService {
    List<CategoryDTO> getAllCategories(Long locationId);
    List<CategoryDTO> getAllCategoriesWithDishes(Long locationId);
    CategoryDTO getCategoryWithDishes(Long id, Long locationId);
    CategoryDTO createCategory(CategoryRequestDTO dto);
    CategoryDTO updateCategory(Long id, CategoryRequestDTO dto);
    void deleteCategoryById(Long id, Long locationId);

    @Nullable List<CategoryDTO> getAllCategoriesadmin(Long locationId);
    void restoreCategoryById(Long id, Long locationId);
}
