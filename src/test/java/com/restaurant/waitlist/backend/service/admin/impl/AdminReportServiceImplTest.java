package com.restaurant.waitlist.backend.service.admin.impl;

import com.restaurant.waitlist.backend.entity.ReportRecord;
import com.restaurant.waitlist.backend.repository.AuditLogRepository;
import com.restaurant.waitlist.backend.repository.CampaignRepository;
import com.restaurant.waitlist.backend.repository.FeedbackRepository;
import com.restaurant.waitlist.backend.repository.RedemptionRepository;
import com.restaurant.waitlist.backend.repository.ReportRepository;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
import com.restaurant.waitlist.backend.repository.WaitlistRepository;
import com.restaurant.waitlist.backend.dto.response.admin.ReportResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
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
    private AuditLogRepository auditLogRepository;

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
                auditLogRepository
        );
    }

    @Test
    void generateReport_overallWithoutDates_shouldUseNullRestaurantIdForGlobalCounts() throws Exception {
        when(waitlistRepository.countAllInDateRange(any(Date.class), any(Date.class))).thenReturn(229L);
        when(waitlistRepository.countByRestaurantAndStatusInDateRange(isNull(), any(), any(Date.class), any(Date.class))).thenReturn(52L);
        when(waitlistRepository.averageSeatedDurationMinutes(isNull(), any(Date.class), any(Date.class))).thenReturn(99.94);
        when(waitlistRepository.aggregateCustomers(isNull(), any(Date.class), any(Date.class))).thenReturn(Collections.emptyList());
        when(redemptionRepository.countRedemptionsByDateRange(any(), any())).thenReturn(82L);
        when(campaignRepository.countActiveCampaigns()).thenReturn(13L);
        when(feedbackRepository.averageRatingByDateRange(any(Date.class), any(Date.class))).thenReturn(0.0);
        when(feedbackRepository.countByDateRange(any(Date.class), any(Date.class))).thenReturn(0L);
        when(reportRepository.save(any(ReportRecord.class))).thenAnswer(invocation -> {
            ReportRecord record = invocation.getArgument(0);
            record.setId(1L);
            return record;
        });
        when(auditLogRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ReportResponse response = service.generateReport("overall", null, "", null, null);

        assertNotNull(response);
        assertEquals("overall", response.getType());
        verify(waitlistRepository).countByRestaurantAndStatusInDateRange(isNull(), any(), any(Date.class), any(Date.class));
    }
}
