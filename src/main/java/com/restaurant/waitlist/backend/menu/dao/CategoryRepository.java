package com.restaurant.waitlist.backend.menu.dao;

import com.restaurant.waitlist.backend.menu.model.Category;
import com.restaurant.waitlist.backend.menu.model.enums.Status;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT c FROM Category c WHERE c.status = 'ACTIVE'")
    List<Category> findAll();

    @Query("SELECT c FROM Category c")
    List<Category> findAllAdmin();

    Optional<Category> findById(Long id);

    Optional<Category> findByName(String name);

    @Query("""
        SELECT DISTINCT c FROM Category c
        LEFT JOIN FETCH c.dishes d
        WHERE c.status = 'ACTIVE'
    """)
    List<Category> getAllCategoriesWithDishes();

    boolean existsByNameIgnoreCase(String name);

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
