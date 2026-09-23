package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    // Franchise-group scoped variant: an admin's "all my locations" view
    // resolves to a restaurant id list rather than a truly global query.
    @Query("SELECT f FROM Feedback f WHERE f.waitlist.restaurant.id IN :restaurantIds")
    Page<Feedback> findByWaitlistRestaurantIdIn(List<Long> restaurantIds, Pageable pageable);

    @Query("SELECT f FROM Feedback f WHERE f.waitlist.restaurant.id IN :restaurantIds")
    List<Feedback> findByWaitlistRestaurantIdIn(List<Long> restaurantIds);

    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.waitlist.restaurant.id = :restaurantId")
    Double averageRatingByRestaurantId(Long restaurantId);

    // Batched IN-list variant, grouped by restaurant, to avoid one query per restaurant
    // when computing a weighted average across a franchise group.
    @Query("SELECT f.waitlist.restaurant.id, AVG(f.rating) FROM Feedback f WHERE f.waitlist.restaurant.id IN :restaurantIds GROUP BY f.waitlist.restaurant.id")
    List<Object[]> averageRatingGroupedByRestaurantIds(List<Long> restaurantIds);

    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.waitlist.restaurant.id = :restaurantId")
    long countByWaitlistRestaurantId(Long restaurantId);

    // Batched IN-list variant, grouped by restaurant, to avoid one query per restaurant
    // when computing a weighted average across a franchise group.
    @Query("SELECT f.waitlist.restaurant.id, COUNT(f) FROM Feedback f WHERE f.waitlist.restaurant.id IN :restaurantIds GROUP BY f.waitlist.restaurant.id")
    List<Object[]> countGroupedByRestaurantIds(List<Long> restaurantIds);

    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.waitlist.restaurant.id = :restaurantId AND f.reply IS NOT NULL AND TRIM(f.reply) <> ''")
    long countRepliedByWaitlistRestaurantId(Long restaurantId);

    // Franchise-group scoped variant: an admin's "all my locations" view
    // resolves to a restaurant id list rather than a truly global query.
    @Query(value = "SELECT DATE(f.created_at) AS day, COUNT(*) AS reviews " +
            "FROM feedback f LEFT JOIN waitlist w ON f.waitlist_id = w.id " +
            "WHERE w.restaurant_id IN (:restaurantIds) " +
            "AND (CAST(:fromDate AS DATE) IS NULL OR DATE(f.created_at) >= CAST(:fromDate AS DATE)) " +
            "AND (CAST(:toDate AS DATE) IS NULL OR DATE(f.created_at) <= CAST(:toDate AS DATE)) " +
            "GROUP BY DATE(f.created_at) ORDER BY DATE(f.created_at)", nativeQuery = true)
    java.util.List<Object[]> countReviewsByDayForRestaurantIds(List<Long> restaurantIds, java.sql.Date fromDate, java.sql.Date toDate);

    // Date range queries for summary metrics
    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.waitlist.restaurant.id = :restaurantId AND CAST(f.createdAt AS date) >= CAST(:fromDate AS date) AND CAST(f.createdAt AS date) <= CAST(:toDate AS date)")
    long countByWaitlistRestaurantIdAndDateRange(Long restaurantId, java.sql.Date fromDate, java.sql.Date toDate);

    // Batched IN-list variant to avoid one query per restaurant when summing across a franchise group.
    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.waitlist.restaurant.id IN :restaurantIds AND CAST(f.createdAt AS date) >= CAST(:fromDate AS date) AND CAST(f.createdAt AS date) <= CAST(:toDate AS date)")
    long countByWaitlistRestaurantIdInAndDateRange(List<Long> restaurantIds, java.sql.Date fromDate, java.sql.Date toDate);

    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.waitlist.restaurant.id = :restaurantId AND CAST(f.createdAt AS date) >= CAST(:fromDate AS date) AND CAST(f.createdAt AS date) <= CAST(:toDate AS date)")
    Double averageRatingByRestaurantIdAndDateRange(Long restaurantId, java.sql.Date fromDate, java.sql.Date toDate);

    // Batched IN-list variant, grouped by restaurant, to avoid one query per restaurant
    // when computing a weighted average across a franchise group.
    @Query("SELECT f.waitlist.restaurant.id, AVG(f.rating) FROM Feedback f WHERE f.waitlist.restaurant.id IN :restaurantIds AND CAST(f.createdAt AS date) >= CAST(:fromDate AS date) AND CAST(f.createdAt AS date) <= CAST(:toDate AS date) GROUP BY f.waitlist.restaurant.id")
    List<Object[]> averageRatingByRestaurantIdsAndDateRangeGrouped(List<Long> restaurantIds, java.sql.Date fromDate, java.sql.Date toDate);

    @Query("SELECT f.waitlist.restaurant.id, COUNT(f) FROM Feedback f WHERE f.waitlist.restaurant.id IN :restaurantIds AND CAST(f.createdAt AS date) >= CAST(:fromDate AS date) AND CAST(f.createdAt AS date) <= CAST(:toDate AS date) GROUP BY f.waitlist.restaurant.id")
    List<Object[]> countByRestaurantIdsAndDateRangeGrouped(List<Long> restaurantIds, java.sql.Date fromDate, java.sql.Date toDate);

    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.waitlist.restaurant.id = :restaurantId AND f.reply IS NOT NULL AND TRIM(f.reply) <> '' AND CAST(f.createdAt AS date) >= CAST(:fromDate AS date) AND CAST(f.createdAt AS date) <= CAST(:toDate AS date)")
    long countRepliedByRestaurantIdAndDateRange(Long restaurantId, java.sql.Date fromDate, java.sql.Date toDate);

    // Batched IN-list variant to avoid one query per restaurant when summing across a franchise group.
    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.waitlist.restaurant.id IN :restaurantIds AND f.reply IS NOT NULL AND TRIM(f.reply) <> '' AND CAST(f.createdAt AS date) >= CAST(:fromDate AS date) AND CAST(f.createdAt AS date) <= CAST(:toDate AS date)")
    long countRepliedByRestaurantIdInAndDateRange(List<Long> restaurantIds, java.sql.Date fromDate, java.sql.Date toDate);
}

