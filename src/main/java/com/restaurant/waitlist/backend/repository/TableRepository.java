package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TableRepository extends JpaRepository<Table, Long> {
    List<Table> findByRestaurantId(Long restaurantId);
}

