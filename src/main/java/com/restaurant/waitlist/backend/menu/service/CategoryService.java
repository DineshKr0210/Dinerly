package com.restaurant.waitlist.backend.menu.service;

import com.restaurant.waitlist.backend.menu.dto.CategoryDTO;
import com.restaurant.waitlist.backend.menu.dto.CategoryRequestDTO;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface CategoryService {
    List<CategoryDTO> getAllCategories();
    List<CategoryDTO> getAllCategoriesWithDishes();
    CategoryDTO getCategoryWithDishes(Long id);
    CategoryDTO createCategory(CategoryRequestDTO dto);
    CategoryDTO updateCategory(Long id, CategoryRequestDTO dto);
    void deleteCategoryById(Long id);

    @Nullable List<CategoryDTO> getAllCategoriesadmin();
    void restoreCategoryById(Long id);
}
