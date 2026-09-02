package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.RewardTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RewardTierRepository extends JpaRepository<RewardTier, Long> {
}
