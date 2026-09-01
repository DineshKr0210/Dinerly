package com.restaurant.waitlist.backend.menu.dao;

import com.restaurant.waitlist.backend.menu.model.Dish;
import com.restaurant.waitlist.backend.menu.model.enums.DishType;
import com.restaurant.waitlist.backend.menu.model.enums.Status;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DishRepository extends JpaRepository<Dish, Long> {

    @Query("""
        SELECT DISTINCT d FROM Dish d
        LEFT JOIN FETCH d.types t
        LEFT JOIN FETCH d.category c
        WHERE d.status = 'ACTIVE'
    """)
    List<Dish> getAllDishesWithRelations();

    @Query("""
        SELECT DISTINCT d FROM Dish d
        LEFT JOIN FETCH d.types t
        LEFT JOIN FETCH d.category c
        WHERE d.status = 'ACTIVE'
        AND (:restaurantId IS NULL OR d.restaurant.id = :restaurantId)
    """)
    List<Dish> getAllDishesWithRelationsByRestaurant(@Param("restaurantId") Long restaurantId);

    @Query("""
        SELECT d FROM Dish d
        JOIN d.category c
        WHERE c.name = :categoryName
        AND d.status = 'ACTIVE'
        AND c.status = 'ACTIVE'
    """)
    List<Dish> getDishesByCategory(String categoryName);

    @Query("""
        SELECT d FROM Dish d
        JOIN d.category c
        WHERE c.name = :categoryName
        AND d.status = 'ACTIVE'
        AND c.status = 'ACTIVE'
        AND (:restaurantId IS NULL OR d.restaurant.id = :restaurantId)
    """)
    List<Dish> getDishesByCategoryAndRestaurant(@Param("categoryName") String categoryName,
                                               @Param("restaurantId") Long restaurantId);

    @Query("""
    SELECT d FROM Dish d
    JOIN d.category c
    WHERE LOWER(c.name) = LOWER(:categoryName)
""")
    List<Dish> getDishesByCategoryAdmin(String categoryName);

    @Query("""
    SELECT d FROM Dish d
    JOIN d.category c
    WHERE LOWER(c.name) = LOWER(:categoryName)
    AND (:restaurantId IS NULL OR d.restaurant.id = :restaurantId)
""")
    List<Dish> getDishesByCategoryAdminAndRestaurant(@Param("categoryName") String categoryName,
                                                    @Param("restaurantId") Long restaurantId);

    @Query("""
        SELECT DISTINCT d FROM Dish d
        JOIN d.types t
        WHERE t.name = :type
        AND d.status = 'ACTIVE'
    """)
    List<Dish> getDishesByType(DishType type);

    @Query("""
        SELECT DISTINCT d FROM Dish d
        JOIN d.types t
        WHERE t.name = :type
        AND d.status = 'ACTIVE'
        AND (:restaurantId IS NULL OR d.restaurant.id = :restaurantId)
    """)
    List<Dish> getDishesByTypeAndRestaurant(DishType type, @Param("restaurantId") Long restaurantId);

    Optional<Dish> findById(Long id);

    @Query("SELECT d FROM Dish d WHERE LOWER(d.dishName) = LOWER(:name) AND d.status = 'ACTIVE'")
    Optional<Dish> findByName(String name);

    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM Dish d WHERE LOWER(d.dishName) = LOWER(:name) AND d.restaurant.id = :restaurantId AND d.status = :status")
    boolean existsByDishNameIgnoreCaseAndRestaurantIdAndStatus(String name, Long restaurantId, Status status);

    boolean existsByDishNameIgnoreCaseAndStatus(String dishName, Status status);

    @Modifying
    @Query("UPDATE Dish d SET d.status = 'INACTIVE' WHERE d.id = :id")
    int softDeleteById(Long id);

    @Modifying
    @Query("UPDATE Dish d SET d.status = 'ACTIVE' WHERE d.id = :id")
    int restoreById(Long id);
}
