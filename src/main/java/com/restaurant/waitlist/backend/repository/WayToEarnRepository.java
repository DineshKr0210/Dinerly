package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.WayToEarn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WayToEarnRepository extends JpaRepository<WayToEarn, Long> {
}
