package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.DinerlyPoints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DinerlyPointsRepository extends JpaRepository<DinerlyPoints, Long> {
    Optional<DinerlyPoints> findByUserId(Long userId);
}
