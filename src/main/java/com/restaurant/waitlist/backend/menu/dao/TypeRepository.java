package com.restaurant.waitlist.backend.menu.dao;

import com.restaurant.waitlist.backend.menu.model.Type;
import com.restaurant.waitlist.backend.menu.model.enums.Status;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TypeRepository extends JpaRepository<Type, Long> {

    @Query("SELECT t FROM Type t WHERE t.status = 'ACTIVE'")
    List<Type> findAll();

    @Query("SELECT t FROM Type t WHERE t.status = 'ACTIVE' AND (:restaurantId IS NULL OR t.restaurant.id = :restaurantId)")
    List<Type> findAllByRestaurant(@Param("restaurantId") Long restaurantId);

    List<Type> findByNameInAndStatus(List<String> names, Status status);

    @Query("SELECT t FROM Type t WHERE t.name IN :names AND t.status = :status AND (:restaurantId IS NULL OR t.restaurant.id = :restaurantId)")
    List<Type> findByNameInAndStatusAndRestaurantId(@Param("names") List<String> names, @Param("status") Status status, @Param("restaurantId") Long restaurantId);

    @Modifying
    @Query("UPDATE Type t SET t.status = 'INACTIVE' WHERE t.id = :id")
    void softDeleteById(Long id);
}
