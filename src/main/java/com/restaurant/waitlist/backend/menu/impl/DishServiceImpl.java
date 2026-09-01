package com.restaurant.waitlist.backend.menu.impl;

import com.restaurant.waitlist.backend.menu.dao.CategoryRepository;
import com.restaurant.waitlist.backend.menu.dao.DishRepository;
import com.restaurant.waitlist.backend.menu.dao.TypeRepository;
import com.restaurant.waitlist.backend.menu.dto.DishDTO;
import com.restaurant.waitlist.backend.menu.dto.DishRequestDTO;
import com.restaurant.waitlist.backend.menu.exceptions.BadRequestException;
import com.restaurant.waitlist.backend.menu.exceptions.ResourceNotFoundException;
import com.restaurant.waitlist.backend.menu.mapper.DishMapper;
import com.restaurant.waitlist.backend.menu.model.Category;
import com.restaurant.waitlist.backend.menu.model.Dish;
import com.restaurant.waitlist.backend.menu.model.Type;
import com.restaurant.waitlist.backend.menu.model.enums.DishType;
import com.restaurant.waitlist.backend.menu.model.enums.Status;
import com.restaurant.waitlist.backend.menu.service.DishService;
import com.restaurant.waitlist.backend.menu.service.GitHubImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DishServiceImpl implements DishService {

    private final DishRepository dishRepository;
    private final CategoryRepository categoryRepository;
    private final TypeRepository typeRepository;
    private final GitHubImageService gitHubImageService;

    @Override
    @Cacheable("AllDishes")
    public List<DishDTO> getAllDishesWithRelations() {
        return dishRepository.getAllDishesWithRelations().stream()
                .map(DishMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable("DishesByCategory")
    public List<DishDTO> getDishesByCategory(String categoryName) {
        return dishRepository.getDishesByCategory(categoryName).stream()
                .map(DishMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable("DishesByType")
    public List<DishDTO> getDishesByType(String type) {
        DishType parsed = DishType.valueOf(type.toUpperCase());
        return dishRepository.getDishesByType(parsed).stream()
                .map(DishMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable("DishesByID")
    public List<DishDTO> getDishesByID(Long id) {
        return dishRepository.findById(id).stream()
                .map(DishMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = {"AllDishes", "DishesByCategory", "DishesByType","DishesByID"}, allEntries = true)
    public DishDTO createDish(DishRequestDTO dto) {
        validateDish(dto);

        if (dishRepository.existsByDishNameIgnoreCaseAndStatus(dto.getDishName(), Status.ACTIVE)) {
            throw new BadRequestException("Dish with this name already exists");
        }

        Category category = categoryRepository
                .findByNameIgnoreCaseAndStatus(dto.getCategoryName(), Status.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        List<Type> types = typeRepository.findByNameInAndStatus(dto.getTypeNames(), Status.ACTIVE);
        if (types.size() != dto.getTypeNames().size()) {
            throw new ResourceNotFoundException("One or more dish types not found");
        }

        String imageUrl = gitHubImageService.uploadImage(dto.getImage());

        Dish dish = Dish.builder()
                .dishName(dto.getDishName())
                .category(category)
                .types(types)
                .price(dto.getPrice())
                .calories(dto.getCalories())
                .description(dto.getDescription())
                .imageUrl(imageUrl)
                .build();

        return DishMapper.toDTO(dishRepository.save(dish));
    }

    @Override
    @CacheEvict(value = {"AllDishes", "DishesByCategory", "DishesByType", "DishesByID"}, allEntries = true)
    public DishDTO updateDish(Long id, DishRequestDTO dto) {

        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dish not found"));

        validateDish(dto);

        Category category = categoryRepository
                .findByNameIgnoreCaseAndStatus(dto.getCategoryName(), Status.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        List<Type> types = typeRepository.findByNameInAndStatus(dto.getTypeNames(), Status.ACTIVE);
        if (types.size() != dto.getTypeNames().size()) {
            throw new BadRequestException("One or more dish types are invalid");
        }

        String imageUrl = gitHubImageService.uploadImage(dto.getImage());

        dish.setDishName(dto.getDishName());
        dish.setCategory(category);
        dish.setTypes(types);
        dish.setPrice(dto.getPrice());
        dish.setCalories(dto.getCalories());
        dish.setDescription(dto.getDescription());
        dish.setImageUrl(imageUrl);

        return DishMapper.toDTO(dishRepository.save(dish));
    }

    @Override
    @Transactional
    @CacheEvict(value = {"AllDishes", "DishesByCategory", "DishesByType", "DishesByID"}, allEntries = true)
    public void deleteDishById(Long id) {

        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dish not found with id: " + id));

        dishRepository.softDeleteById(dish.getId());
    }

    @Override
    public List<DishDTO> getDishesByCategoryAdmin(String categoryName) {
        return dishRepository.getDishesByCategoryAdmin(categoryName).stream()
                .map(DishMapper::toDTO)
                .collect(Collectors.toList());    }

    @Override
    @Transactional
    @CacheEvict(value = {"AllDishes", "DishesByCategory", "DishesByType", "DishesByID"}, allEntries = true)
    public void restoreDishById(Long id) {

        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dish not found with id: " + id));

        dishRepository.restoreById(dish.getId());
    }

    private void validateDish(DishRequestDTO dto) {
        if (dto.getPrice() <= 0) {
            throw new BadRequestException("Price must be greater than zero");
        }
    }

}
