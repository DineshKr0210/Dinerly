package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.Waitlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;

@Repository
public interface WaitlistRepository extends JpaRepository<Waitlist, Long>, JpaSpecificationExecutor<Waitlist> {
    List<Waitlist> findByRestaurantId(Long restaurantId);

    // Pushes the "joined today" filter down to the DB instead of loading a restaurant's
    // entire waitlist history and filtering it in memory.
    List<Waitlist> findByRestaurantIdAndJoinedAtGreaterThanAndJoinedAtLessThan(
            Long restaurantId, java.time.LocalDateTime start, java.time.LocalDateTime end);

    List<Waitlist> findByRestaurantIdInAndStatus(List<Long> restaurantIds, Waitlist.WaitlistStatus status);

        Optional<Waitlist> findFirstByGuestPhoneOrderByIdDesc(String guestPhone);

    // Return the most recent waitlist entry (by joined_at desc then id desc) for a given restaurant and guest phone
    @Query(value = "SELECT * FROM waitlist w WHERE w.restaurant_id = :restaurantId AND w.guest_phone = :guestPhone ORDER BY w.id DESC LIMIT 1", nativeQuery = true)
    Optional<Waitlist> findLatestByRestaurantIdAndGuestPhone(@Param("restaurantId") Long restaurantId, @Param("guestPhone") String guestPhone);

    long countByRestaurantIdAndStatusIn(Long restaurantId, java.util.Collection<Waitlist.WaitlistStatus> statuses);

    // Batched IN-list variant to avoid one query per restaurant when summing across a franchise group.
    long countByRestaurantIdInAndStatusIn(List<Long> restaurantIds, java.util.Collection<Waitlist.WaitlistStatus> statuses);

    java.util.List<Waitlist> findByRestaurantIdAndStatusInOrderByIdAsc(Long restaurantId, java.util.Collection<Waitlist.WaitlistStatus> statuses);

    @Query(value = "SELECT * FROM waitlist w WHERE w.restaurant_id = :restaurantId AND DATE(w.joined_at) = :joinedDate", nativeQuery = true)
    java.util.List<Waitlist> findByRestaurantIdAndJoinedDate(@Param("restaurantId") Long restaurantId, @Param("joinedDate") java.sql.Date joinedDate);

    @Query(value = "SELECT COUNT(*) FROM waitlist w WHERE w.restaurant_id = :restaurantId AND w.sms_status = 'SENT' AND EXTRACT(YEAR FROM w.sms_sent_at) = :year AND EXTRACT(MONTH FROM w.sms_sent_at) = :month", nativeQuery = true)
    long countSentSmsThisMonth(@Param("restaurantId") Long restaurantId,
                               @Param("year") int year,
                               @Param("month") int month);

     // Aggregation queries for reports (Postgres)
     @Query(value = "SELECT COUNT(*) FROM waitlist w WHERE w.restaurant_id = :restaurantId" +
             " AND (CAST(:fromDate AS DATE) IS NULL OR DATE(w.joined_at) >= CAST(:fromDate AS DATE))" +
             " AND (CAST(:toDate AS DATE) IS NULL OR DATE(w.joined_at) <= CAST(:toDate AS DATE))", nativeQuery = true)
     long countByRestaurantInDateRange(@Param("restaurantId") Long restaurantId,
                                      @Param("fromDate") java.sql.Date fromDate,
                                      @Param("toDate") java.sql.Date toDate);

     // Batched IN-list variant to avoid one query per restaurant when summing across a franchise group.
     @Query(value = "SELECT COUNT(*) FROM waitlist w WHERE w.restaurant_id IN (:restaurantIds)" +
             " AND (CAST(:fromDate AS DATE) IS NULL OR DATE(w.joined_at) >= CAST(:fromDate AS DATE))" +
             " AND (CAST(:toDate AS DATE) IS NULL OR DATE(w.joined_at) <= CAST(:toDate AS DATE))", nativeQuery = true)
     long countByRestaurantIdsInDateRange(@Param("restaurantIds") List<Long> restaurantIds,
                                      @Param("fromDate") java.sql.Date fromDate,
                                      @Param("toDate") java.sql.Date toDate);

    @Query(value = "SELECT COUNT(*) FROM waitlist w WHERE w.restaurant_id = :restaurantId" +
            " AND (CAST(:fromDate AS DATE) IS NULL OR DATE(w.joined_at) >= CAST(:fromDate AS DATE))" +
            " AND (CAST(:toDate AS DATE) IS NULL OR DATE(w.joined_at) <= CAST(:toDate AS DATE))", nativeQuery = true)
    long countByRestaurantIdInDateRange(@Param("restaurantId") Long restaurantId,
                                       @Param("fromDate") java.sql.Date fromDate,
                                       @Param("toDate") java.sql.Date toDate);

     @Query(value = "SELECT COUNT(*) FROM waitlist w WHERE (:restaurantId IS NULL OR w.restaurant_id = :restaurantId) AND w.status = :status" +
             " AND (CAST(:fromDate AS DATE) IS NULL OR COALESCE(DATE(w.seated_at), DATE(w.joined_at)) >= CAST(:fromDate AS DATE))" +
             " AND (CAST(:toDate AS DATE) IS NULL OR COALESCE(DATE(w.seated_at), DATE(w.joined_at)) <= CAST(:toDate AS DATE))", nativeQuery = true)
     long countByRestaurantAndStatusInDateRange(@Param("restaurantId") Long restaurantId,
                                                @Param("status") String status,
                                                @Param("fromDate") java.sql.Date fromDate,
                                                @Param("toDate") java.sql.Date toDate);

     // Batched IN-list variant, grouped by restaurant, to avoid one query per restaurant
     // when computing a weighted average across a franchise group.
     @Query(value = "SELECT w.restaurant_id as restaurantId, COUNT(*) as cnt FROM waitlist w " +
             "WHERE w.restaurant_id IN (:restaurantIds) AND w.status = :status" +
             " AND (CAST(:fromDate AS DATE) IS NULL OR COALESCE(DATE(w.seated_at), DATE(w.joined_at)) >= CAST(:fromDate AS DATE))" +
             " AND (CAST(:toDate AS DATE) IS NULL OR COALESCE(DATE(w.seated_at), DATE(w.joined_at)) <= CAST(:toDate AS DATE))" +
             " GROUP BY w.restaurant_id", nativeQuery = true)
     List<Object[]> countByRestaurantIdsAndStatusInDateRangeGrouped(@Param("restaurantIds") List<Long> restaurantIds,
                                                @Param("status") String status,
                                                @Param("fromDate") java.sql.Date fromDate,
                                                @Param("toDate") java.sql.Date toDate);

      @Query(value = "SELECT AVG(EXTRACT(EPOCH FROM (w.seated_at - w.joined_at))/60) FROM waitlist w " +
              "WHERE w.seated_at IS NOT NULL AND (:restaurantId IS NULL OR w.restaurant_id = :restaurantId)" +
              " AND (CAST(:fromDate AS DATE) IS NULL OR DATE(w.seated_at) >= CAST(:fromDate AS DATE))" +
              " AND (CAST(:toDate AS DATE) IS NULL OR DATE(w.seated_at) <= CAST(:toDate AS DATE))", nativeQuery = true)
      Double averageSeatedDurationMinutes(@Param("restaurantId") Long restaurantId,
                                          @Param("fromDate") java.sql.Date fromDate,
                                          @Param("toDate") java.sql.Date toDate);

      // Batched IN-list variant, grouped by restaurant, to avoid one query per restaurant
      // when computing a weighted average across a franchise group.
      @Query(value = "SELECT w.restaurant_id as restaurantId, AVG(EXTRACT(EPOCH FROM (w.seated_at - w.joined_at))/60) as avgMinutes FROM waitlist w " +
              "WHERE w.seated_at IS NOT NULL AND w.restaurant_id IN (:restaurantIds)" +
              " AND (CAST(:fromDate AS DATE) IS NULL OR DATE(w.seated_at) >= CAST(:fromDate AS DATE))" +
              " AND (CAST(:toDate AS DATE) IS NULL OR DATE(w.seated_at) <= CAST(:toDate AS DATE))" +
              " GROUP BY w.restaurant_id", nativeQuery = true)
      List<Object[]> averageSeatedDurationMinutesGrouped(@Param("restaurantIds") List<Long> restaurantIds,
                                          @Param("fromDate") java.sql.Date fromDate,
                                          @Param("toDate") java.sql.Date toDate);

    // Batched IN-list variant: returns one row per restaurant (avoids one query per restaurant
    // when building a leaderboard across a franchise group).
    @Query(value = "SELECT w.restaurant_id as restaurantId, r.name as name, COUNT(*) as joins " +
            "FROM waitlist w JOIN restaurants r ON w.restaurant_id = r.id " +
            "WHERE w.restaurant_id IN (:restaurantIds) " +
            "AND (CAST(:fromDate AS DATE) IS NULL OR DATE(w.joined_at) >= CAST(:fromDate AS DATE)) " +
            "AND (CAST(:toDate AS DATE) IS NULL OR DATE(w.joined_at) <= CAST(:toDate AS DATE)) " +
            "GROUP BY w.restaurant_id, r.name " +
            "ORDER BY joins DESC", nativeQuery = true)
    java.util.List<Object[]> topRestaurantByJoinsForLocations(@Param("restaurantIds") List<Long> restaurantIds,
                                                            @Param("fromDate") java.sql.Date fromDate,
                                                            @Param("toDate") java.sql.Date toDate);

    // Franchise-group scoped variant: an admin's "all my locations" view
    // resolves to a restaurant id list rather than a truly global query.
    @Query(value = "SELECT DATE(w.joined_at) AS day, COUNT(*) AS joins " +
            "FROM waitlist w " +
            "WHERE w.restaurant_id IN (:restaurantIds) " +
            "AND (CAST(:fromDate AS DATE) IS NULL OR DATE(w.joined_at) >= CAST(:fromDate AS DATE)) " +
            "AND (CAST(:toDate AS DATE) IS NULL OR DATE(w.joined_at) <= CAST(:toDate AS DATE)) " +
            "GROUP BY DATE(w.joined_at) ORDER BY DATE(w.joined_at)", nativeQuery = true)
    java.util.List<Object[]> countJoinsByDayForRestaurantIds(@Param("restaurantIds") List<Long> restaurantIds,
                                            @Param("fromDate") java.sql.Date fromDate,
                                            @Param("toDate") java.sql.Date toDate);

    @Query(value = "SELECT w.guest_name as guest, w.guest_phone as contact, COUNT(*) as visits, " +
            "MIN(DATE(w.joined_at)) as firstVisit, MAX(DATE(w.joined_at)) as lastVisit, " +
            "STRING_AGG(DISTINCT r.name, ', ' ORDER BY r.name) as locations, " +
            "(SELECT w2.marketing_sms_consent FROM waitlist w2 " +
            "  WHERE w2.guest_name = w.guest_name AND w2.guest_phone = w.guest_phone " +
            "  AND (:restaurantId IS NULL OR w2.restaurant_id = :restaurantId) " +
            "  ORDER BY w2.joined_at DESC LIMIT 1) as marketingSmsConsent " +
            "FROM waitlist w " +
            "JOIN restaurants r ON w.restaurant_id = r.id " +
            "WHERE (:restaurantId IS NULL OR w.restaurant_id = :restaurantId) " +
            "AND (CAST(:fromDate AS DATE) IS NULL OR DATE(w.joined_at) >= CAST(:fromDate AS DATE)) " +
            "AND (CAST(:toDate AS DATE) IS NULL OR DATE(w.joined_at) <= CAST(:toDate AS DATE)) " +
            "GROUP BY w.guest_name, w.guest_phone " +
            "ORDER BY visits DESC", nativeQuery = true)
    java.util.List<CustomerAggregation> aggregateCustomers(@Param("restaurantId") Long restaurantId,
                                                         @Param("fromDate") java.sql.Date fromDate,
                                                         @Param("toDate") java.sql.Date toDate);

    default java.util.List<CustomerAggregation> aggregateCustomers(Long restaurantId) {
        return aggregateCustomers(restaurantId, null, null);
    }

    // Franchise-group scoped variant: an admin's "all my locations" view
    // resolves to a restaurant id list rather than a truly global query.
    @Query(value = "SELECT w.guest_name as guest, w.guest_phone as contact, COUNT(*) as visits, " +
            "MIN(DATE(w.joined_at)) as firstVisit, MAX(DATE(w.joined_at)) as lastVisit, " +
            "STRING_AGG(DISTINCT r.name, ', ' ORDER BY r.name) as locations, " +
            "(SELECT w2.marketing_sms_consent FROM waitlist w2 " +
            "  WHERE w2.guest_name = w.guest_name AND w2.guest_phone = w.guest_phone " +
            "  AND w2.restaurant_id IN (:restaurantIds) " +
            "  ORDER BY w2.joined_at DESC LIMIT 1) as marketingSmsConsent " +
            "FROM waitlist w " +
            "JOIN restaurants r ON w.restaurant_id = r.id " +
            "WHERE w.restaurant_id IN (:restaurantIds) " +
            "GROUP BY w.guest_name, w.guest_phone " +
            "ORDER BY visits DESC", nativeQuery = true)
    java.util.List<CustomerAggregation> aggregateCustomersByRestaurantIds(@Param("restaurantIds") List<Long> restaurantIds);

    // Paginated variant of the above, for endpoints that page through the customer list
    // rather than needing the full aggregation (e.g. for summary stats).
    @Query(value = "SELECT w.guest_name as guest, w.guest_phone as contact, COUNT(*) as visits, " +
            "MIN(DATE(w.joined_at)) as firstVisit, MAX(DATE(w.joined_at)) as lastVisit, " +
            "STRING_AGG(DISTINCT r.name, ', ' ORDER BY r.name) as locations, " +
            "(SELECT w2.marketing_sms_consent FROM waitlist w2 " +
            "  WHERE w2.guest_name = w.guest_name AND w2.guest_phone = w.guest_phone " +
            "  AND w2.restaurant_id IN (:restaurantIds) " +
            "  ORDER BY w2.joined_at DESC LIMIT 1) as marketingSmsConsent " +
            "FROM waitlist w " +
            "JOIN restaurants r ON w.restaurant_id = r.id " +
            "WHERE w.restaurant_id IN (:restaurantIds) " +
            "GROUP BY w.guest_name, w.guest_phone " +
            "ORDER BY visits DESC",
            countQuery = "SELECT COUNT(*) FROM (SELECT 1 FROM waitlist w WHERE w.restaurant_id IN (:restaurantIds) " +
                    "GROUP BY w.guest_name, w.guest_phone) AS customer_count",
            nativeQuery = true)
    Page<CustomerAggregation> aggregateCustomersByRestaurantIds(@Param("restaurantIds") List<Long> restaurantIds, Pageable pageable);

    @Query(value = "SELECT w.guest_name as guest, w.guest_phone as contact, COUNT(*) as visits, " +
            "MIN(DATE(w.joined_at)) as firstVisit, MAX(DATE(w.joined_at)) as lastVisit, " +
            "STRING_AGG(DISTINCT r.name, ', ' ORDER BY r.name) as locations, " +
            "(SELECT w2.marketing_sms_consent FROM waitlist w2 " +
            "  WHERE w2.guest_name = w.guest_name AND w2.guest_phone = w.guest_phone " +
            "  AND w2.restaurant_id IN (:restaurantIds) " +
            "  ORDER BY w2.joined_at DESC LIMIT 1) as marketingSmsConsent " +
            "FROM waitlist w " +
            "JOIN restaurants r ON w.restaurant_id = r.id " +
            "WHERE w.restaurant_id IN (:restaurantIds) " +
            "AND (CAST(:fromDate AS DATE) IS NULL OR DATE(w.joined_at) >= CAST(:fromDate AS DATE)) " +
            "AND (CAST(:toDate AS DATE) IS NULL OR DATE(w.joined_at) <= CAST(:toDate AS DATE)) " +
            "GROUP BY w.guest_name, w.guest_phone " +
            "ORDER BY visits DESC", nativeQuery = true)
    java.util.List<CustomerAggregation> aggregateCustomersByRestaurantIds(@Param("restaurantIds") List<Long> restaurantIds,
                                                                         @Param("fromDate") java.sql.Date fromDate,
                                                                         @Param("toDate") java.sql.Date toDate);

       @Query("SELECT w FROM Waitlist w WHERE w.restaurant.id = :restaurantId " +
               "AND (:status IS NULL OR w.status = :status) " +
               "AND (:search IS NULL OR LOWER(w.guestName) LIKE LOWER(CONCAT('%', :search, '%')) OR w.guestPhone LIKE CONCAT('%', :search, '%')) " +
               "AND DATE(w.joinedAt) = :joinedDate " +
               "ORDER BY w.joinedAt DESC")
       Page<Waitlist> findByRestaurantIdWithSearchAndDate(@Param("restaurantId") Long restaurantId,
                                                          @Param("status") Waitlist.WaitlistStatus status,
                                                          @Param("search") String search,
                                                          @Param("joinedDate") java.time.LocalDate joinedDate,
                                                          Pageable pageable);

       // Insights feed queries
       @Query(value = "SELECT ROUND(100.0 * COUNT(CASE WHEN w.status = 'NO_SHOW' THEN 1 END) / NULLIF(COUNT(*), 0), 2) " +
               "FROM waitlist w WHERE w.restaurant_id = :restaurantId " +
               "AND (CAST(:fromDate AS DATE) IS NULL OR DATE(w.joined_at) >= CAST(:fromDate AS DATE)) " +
               "AND (CAST(:toDate AS DATE) IS NULL OR DATE(w.joined_at) <= CAST(:toDate AS DATE))", nativeQuery = true)
       Double getNoShowRateByLocation(@Param("restaurantId") Long restaurantId,
                                      @Param("fromDate") java.sql.Date fromDate,
                                      @Param("toDate") java.sql.Date toDate);

       @Query(value = "SELECT ROUND(AVG(EXTRACT(EPOCH FROM (w.seated_at - w.notified_at))/60), 2) - " +
               "ROUND(AVG(w.estimated_wait_time), 2) AS wait_time_variance " +
               "FROM waitlist w WHERE w.restaurant_id = :restaurantId " +
               "AND w.seated_at IS NOT NULL AND w.notified_at IS NOT NULL " +
               "AND (CAST(:fromDate AS DATE) IS NULL OR DATE(w.joined_at) >= CAST(:fromDate AS DATE)) " +
               "AND (CAST(:toDate AS DATE) IS NULL OR DATE(w.joined_at) <= CAST(:toDate AS DATE))", nativeQuery = true)
       Double getWaitTimeAccuracyByLocation(@Param("restaurantId") Long restaurantId,
                                           @Param("fromDate") java.sql.Date fromDate,
                                           @Param("toDate") java.sql.Date toDate);
}

