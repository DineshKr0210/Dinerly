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
import org.jspecify.annotations.Nullable;
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

    @Override
    @Cacheable("AllCategories")
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryMapper::catToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable("AllCategoriesWithDishes")
    public List<CategoryDTO> getAllCategoriesWithDishes() {
        return categoryRepository.getAllCategoriesWithDishes().stream()
                .map(CategoryMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable("CategoriesByID")
    public CategoryDTO getCategoryWithDishes(Long id) {
        return categoryRepository.findById(id)
                .map(CategoryMapper::toDTO)
                .orElse(null);
    }
    @Override
    @CacheEvict(value = {"AllCategories", "AllCategoriesWithDishes"}, allEntries = true)
    public CategoryDTO createCategory(CategoryRequestDTO dto) {
        if (categoryRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new BadRequestException("Category already exists");
        }

        Category category = Category.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .build();

        return CategoryMapper.catToDTO(categoryRepository.save(category));
    }

    @Override
    @CacheEvict(value = {"AllCategories", "AllCategoriesWithDishes", "CategoriesByID"}, allEntries = true)
    public CategoryDTO updateCategory(Long id, CategoryRequestDTO dto) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        category.setName(dto.getName());
        category.setDescription(dto.getDescription());

        return CategoryMapper.catToDTO(categoryRepository.save(category));
    }

    @Override
    @Transactional
    @CacheEvict(value = {"AllCategories", "AllCategoriesWithDishes", "CategoriesByID"}, allEntries = true)
    public void deleteCategoryById(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        categoryRepository.softDeleteById(category.getId());
    }

    @Override
    public @Nullable List<CategoryDTO> getAllCategoriesadmin() {
        return categoryRepository.findAllAdmin().stream()
                .map(CategoryMapper::catToDTO)
                .collect(Collectors.toList());    }

    @Override
    @Transactional
    @CacheEvict(value = {"AllCategories", "AllCategoriesWithDishes", "CategoriesByID"}, allEntries = true)
    public void restoreCategoryById(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        categoryRepository.restoreById(category.getId());
    }
}
