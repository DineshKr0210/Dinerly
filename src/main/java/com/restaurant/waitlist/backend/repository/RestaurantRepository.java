package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findByName(String name);
    Optional<Restaurant> findByEmail(String email);
    Optional<Restaurant> findByPhone(String phone);
}

