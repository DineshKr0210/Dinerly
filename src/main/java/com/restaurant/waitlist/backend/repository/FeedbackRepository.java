package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    Optional<Feedback> findByWaitlistId(Long waitlistId);

    @Query("SELECT AVG(f.rating) FROM Feedback f")
    Double averageRating();

    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.waitlist.restaurant.id = :restaurantId")
    Double averageRatingByRestaurantId(Long restaurantId);
}

