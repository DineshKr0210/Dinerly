package com.restaurant.waitlist.backend.menu.dao;

import com.restaurant.waitlist.backend.menu.model.Category;
import com.restaurant.waitlist.backend.menu.model.enums.Status;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT c FROM Category c WHERE c.status = 'ACTIVE'")
    List<Category> findAll();

    @Query("SELECT c FROM Category c WHERE c.status = 'ACTIVE' AND (:restaurantId IS NULL OR c.restaurant.id = :restaurantId)")
    List<Category> findAllByRestaurant(@Param("restaurantId") Long restaurantId);

    @Query("SELECT c FROM Category c")
    List<Category> findAllAdmin();

    @Query("SELECT c FROM Category c WHERE (:restaurantId IS NULL OR c.restaurant.id = :restaurantId)")
    List<Category> findAllAdminByRestaurant(@Param("restaurantId") Long restaurantId);

    Optional<Category> findById(Long id);

    Optional<Category> findByName(String name);

    Optional<Category> findByNameAndRestaurantId(String name, Long restaurantId);

    @Query("""
        SELECT DISTINCT c FROM Category c
        LEFT JOIN FETCH c.dishes d
        WHERE c.status = 'ACTIVE'
    """)
    List<Category> getAllCategoriesWithDishes();

    @Query("""
        SELECT DISTINCT c FROM Category c
        LEFT JOIN FETCH c.dishes d
        WHERE c.status = 'ACTIVE'
        AND (:restaurantId IS NULL OR c.restaurant.id = :restaurantId)
    """)
    List<Category> getAllCategoriesWithDishesByRestaurant(@Param("restaurantId") Long restaurantId);

    boolean existsByNameIgnoreCase(String name);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Category c WHERE LOWER(c.name) = LOWER(:name) AND c.restaurant.id = :restaurantId AND c.status = :status")
    boolean existsByNameIgnoreCaseAndRestaurantIdAndStatus(@Param("name") String name, @Param("restaurantId") Long restaurantId, @Param("status") Status status);

    Optional<Category> findByNameIgnoreCaseAndStatus(String name, Status status);

    @Modifying
    @Query("UPDATE Category c SET c.status = 'INACTIVE' WHERE LOWER(c.name) = LOWER(:name)")
    int softDeleteByName(String name);

    @Modifying
    @Query("UPDATE Category c SET c.status = 'INACTIVE' WHERE c.id = :id")
    int softDeleteById(Long id);

    @Modifying
    @Query("UPDATE Category c SET c.status = 'ACTIVE' WHERE c.id = :id")
    int restoreById(Long id);
}
