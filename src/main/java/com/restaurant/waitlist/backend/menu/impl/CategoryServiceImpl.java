package com.restaurant.waitlist.backend.menu.impl;

import com.restaurant.waitlist.backend.menu.dao.CategoryRepository;
import com.restaurant.waitlist.backend.menu.dto.CategoryDTO;
import com.restaurant.waitlist.backend.menu.dto.CategoryRequestDTO;
import com.restaurant.waitlist.backend.menu.exceptions.BadRequestException;
import com.restaurant.waitlist.backend.menu.exceptions.ResourceNotFoundException;
import com.restaurant.waitlist.backend.menu.mapper.CategoryMapper;
import com.restaurant.waitlist.backend.menu.model.Category;
import com.restaurant.waitlist.backend.menu.service.CategoryService;
import lombok.RequiredArgsConstructor;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
import com.restaurant.waitlist.backend.entity.Restaurant;
import org.jspecify.annotations.Nullable;
import com.restaurant.waitlist.backend.menu.model.enums.Status;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final RestaurantRepository restaurantRepository;

    @Override
    @Cacheable("AllCategories")
    public List<CategoryDTO> getAllCategories(Long locationId) {
        return categoryRepository.findAllByRestaurant(locationId).stream()
                .map(CategoryMapper::catToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable("AllCategoriesWithDishes")
    public List<CategoryDTO> getAllCategoriesWithDishes(Long locationId) {
        return categoryRepository.getAllCategoriesWithDishesByRestaurant(locationId).stream()
                .map(CategoryMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable("CategoriesByID")
    public CategoryDTO getCategoryWithDishes(Long id, Long locationId) {
        Category category = categoryRepository.findById(id)
                .orElse(null);
        if (category == null) return null;
        if (category.getRestaurant() != null && locationId != null && !category.getRestaurant().getId().equals(locationId)) {
            return null; // not found for this location
        }
        return CategoryMapper.toDTO(category);
    }
    @Override
    @CacheEvict(value = {"AllCategories", "AllCategoriesWithDishes"}, allEntries = true)
    public CategoryDTO createCategory(CategoryRequestDTO dto) {
        Restaurant restaurant = restaurantRepository.findById(dto.getLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        if (categoryRepository.existsByNameIgnoreCaseAndRestaurantIdAndStatus(dto.getName(), restaurant.getId(), Status.ACTIVE)) {
            throw new BadRequestException("Category already exists for this location");
        }

        Category category = Category.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .restaurant(restaurant)
                .build();

        return CategoryMapper.catToDTO(categoryRepository.save(category));
    }

    @Override
    @CacheEvict(value = {"AllCategories", "AllCategoriesWithDishes", "CategoriesByID"}, allEntries = true)
    public CategoryDTO updateCategory(Long id, CategoryRequestDTO dto) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        Restaurant restaurant = restaurantRepository.findById(dto.getLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        if (!category.getName().equalsIgnoreCase(dto.getName()) || (category.getRestaurant() == null || !category.getRestaurant().getId().equals(restaurant.getId()))) {
            if (categoryRepository.existsByNameIgnoreCaseAndRestaurantIdAndStatus(dto.getName(), restaurant.getId(), Status.ACTIVE)) {
                throw new BadRequestException("Category already exists for this location");
            }
        }

        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setRestaurant(restaurant);

        return CategoryMapper.catToDTO(categoryRepository.save(category));
    }

    @Override
    @Transactional
    @CacheEvict(value = {"AllCategories", "AllCategoriesWithDishes", "CategoriesByID"}, allEntries = true)
    public void deleteCategoryById(Long id, Long locationId) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (locationId != null && category.getRestaurant() != null && !category.getRestaurant().getId().equals(locationId)) {
            throw new ResourceNotFoundException("Category not found for this location");
        }

        categoryRepository.softDeleteById(category.getId());
    }

    @Override
    public @Nullable List<CategoryDTO> getAllCategoriesadmin(Long locationId) {
        return categoryRepository.findAllAdminByRestaurant(locationId).stream()
                .map(CategoryMapper::catToDTO)
                .collect(Collectors.toList());    }

    @Override
    @Transactional
    @CacheEvict(value = {"AllCategories", "AllCategoriesWithDishes", "CategoriesByID"}, allEntries = true)
    public void restoreCategoryById(Long id, Long locationId) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (locationId != null && category.getRestaurant() != null && !category.getRestaurant().getId().equals(locationId)) {
            throw new ResourceNotFoundException("Category not found for this location");
        }

        categoryRepository.restoreById(category.getId());
    }
}
