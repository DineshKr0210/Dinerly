package com.restaurant.waitlist.backend.service.admin.impl;

import com.restaurant.waitlist.backend.dto.response.admin.WaitlistPerformanceResponse;
import com.restaurant.waitlist.backend.repository.CampaignRepository;
import com.restaurant.waitlist.backend.repository.FeedbackRepository;
import com.restaurant.waitlist.backend.repository.WaitlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminPerformanceServiceImplTest {

    @Mock
    private WaitlistRepository waitlistRepository;

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private FeedbackRepository feedbackRepository;

    private AdminPerformanceServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AdminPerformanceServiceImpl();
        ReflectionTestUtils.setField(service, "waitlistRepository", waitlistRepository);
        ReflectionTestUtils.setField(service, "campaignRepository", campaignRepository);
        ReflectionTestUtils.setField(service, "feedbackRepository", feedbackRepository);
    }

    @Test
    void getWaitlistPerformance_shouldReturnSummaryTrendAndLeaderboardForPastMonth() {
        when(waitlistRepository.countAllInDateRange(org.mockito.ArgumentMatchers.any(java.sql.Date.class), org.mockito.ArgumentMatchers.any(java.sql.Date.class)))
                .thenReturn(912L);
        when(waitlistRepository.countByRestaurantAndStatusInDateRange(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.eq("SEATED"), org.mockito.ArgumentMatchers.any(java.sql.Date.class), org.mockito.ArgumentMatchers.any(java.sql.Date.class)))
                .thenReturn(803L);
        when(waitlistRepository.averageSeatedDurationMinutes(org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.any(java.sql.Date.class), org.mockito.ArgumentMatchers.any(java.sql.Date.class)))
                .thenReturn(14.0);
        when(waitlistRepository.topRestaurantsByJoins(org.mockito.ArgumentMatchers.any(java.sql.Date.class), org.mockito.ArgumentMatchers.any(java.sql.Date.class), org.mockito.ArgumentMatchers.eq(10)))
                .thenReturn(java.util.List.of(new Object[]{101L, "Live Oak Bistro", 558L}, new Object[]{102L, "Riverfront", 342L}));
        when(waitlistRepository.countByRestaurantAndStatusInDateRange(org.mockito.ArgumentMatchers.eq(101L), org.mockito.ArgumentMatchers.eq("SEATED"), org.mockito.ArgumentMatchers.any(java.sql.Date.class), org.mockito.ArgumentMatchers.any(java.sql.Date.class)))
                .thenReturn(120L);
        when(waitlistRepository.countByRestaurantAndStatusInDateRange(org.mockito.ArgumentMatchers.eq(102L), org.mockito.ArgumentMatchers.eq("SEATED"), org.mockito.ArgumentMatchers.any(java.sql.Date.class), org.mockito.ArgumentMatchers.any(java.sql.Date.class)))
                .thenReturn(80L);
        when(waitlistRepository.averageSeatedDurationMinutes(org.mockito.ArgumentMatchers.eq(101L), org.mockito.ArgumentMatchers.any(java.sql.Date.class), org.mockito.ArgumentMatchers.any(java.sql.Date.class)))
                .thenReturn(11.0);
        when(waitlistRepository.averageSeatedDurationMinutes(org.mockito.ArgumentMatchers.eq(102L), org.mockito.ArgumentMatchers.any(java.sql.Date.class), org.mockito.ArgumentMatchers.any(java.sql.Date.class)))
                .thenReturn(13.0);

        WaitlistPerformanceResponse response = service.getWaitlistPerformance(null, "pastMonth");

        assertNotNull(response);
        assertNotNull(response.getSummary());
        assertNotNull(response.getTrend());
        assertNotNull(response.getLeaderboard());
        assertEquals(912L, response.getSummary().getWaitlistJoins());
        assertEquals(803L, response.getSummary().getGuestsSeated());
        assertEquals(14.0, response.getSummary().getAverageWaitTimeMinutes());
        assertFalse(response.getTrend().isEmpty());
        assertEquals("Live Oak Bistro", response.getLeaderboard().get(0).getLocation());
        assertEquals(558L, response.getLeaderboard().get(0).getWaitlistJoins());
    }
}
