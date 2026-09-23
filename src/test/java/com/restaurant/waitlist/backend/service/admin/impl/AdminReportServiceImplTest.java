package com.restaurant.waitlist.backend.service.admin.impl;

import com.restaurant.waitlist.backend.entity.ReportRecord;
import com.restaurant.waitlist.backend.repository.CampaignRepository;
import com.restaurant.waitlist.backend.repository.FeedbackRepository;
import com.restaurant.waitlist.backend.repository.RedemptionRepository;
import com.restaurant.waitlist.backend.repository.ReportRepository;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
import com.restaurant.waitlist.backend.repository.WaitlistRepository;
import com.restaurant.waitlist.backend.service.AdminLocationAccessService;
import com.restaurant.waitlist.backend.service.AuditLogService;
import com.restaurant.waitlist.backend.dto.response.admin.ReportResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminReportServiceImplTest {

    @Mock
    private WaitlistRepository waitlistRepository;

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private RedemptionRepository redemptionRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private AdminLocationAccessService adminLocationAccessService;

    @Mock
    private AuditLogService auditLogService;

    private AdminReportServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AdminReportServiceImpl(
                waitlistRepository,
                feedbackRepository,
                campaignRepository,
                redemptionRepository,
                restaurantRepository,
                reportRepository,
                adminLocationAccessService,
                auditLogService
        );
    }

    @Test
    void generateReport_overallWithoutDates_shouldScopeCountsToAdminsOwnRestaurants() throws Exception {
        when(adminLocationAccessService.getAccessibleRestaurantIds()).thenReturn(List.of(1L));
        when(waitlistRepository.countByRestaurantInDateRange(eq(1L), any(Date.class), any(Date.class))).thenReturn(229L);
        when(waitlistRepository.countByRestaurantAndStatusInDateRange(eq(1L), any(), any(Date.class), any(Date.class))).thenReturn(52L);
        when(waitlistRepository.averageSeatedDurationMinutes(eq(1L), any(Date.class), any(Date.class))).thenReturn(99.94);
        when(waitlistRepository.aggregateCustomersByRestaurantIds(eq(List.of(1L)), any(Date.class), any(Date.class))).thenReturn(Collections.emptyList());
        when(redemptionRepository.countRedemptionsByRestaurantAndDateRange(eq(1L), any(), any())).thenReturn(82L);
        when(campaignRepository.countActiveCampaignsByRestaurant(1L)).thenReturn(13L);
        when(feedbackRepository.averageRatingByRestaurantIdAndDateRange(eq(1L), any(Date.class), any(Date.class))).thenReturn(0.0);
        when(feedbackRepository.countByWaitlistRestaurantIdAndDateRange(eq(1L), any(Date.class), any(Date.class))).thenReturn(0L);
        when(reportRepository.save(any(ReportRecord.class))).thenAnswer(invocation -> {
            ReportRecord record = invocation.getArgument(0);
            record.setId(1L);
            return record;
        });

        ReportResponse response = service.generateReport("overall", null, "", null, null);

        assertNotNull(response);
        assertEquals("overall", response.getType());
        verify(waitlistRepository, org.mockito.Mockito.atLeastOnce())
                .countByRestaurantAndStatusInDateRange(eq(1L), any(), any(Date.class), any(Date.class));
    }
}
