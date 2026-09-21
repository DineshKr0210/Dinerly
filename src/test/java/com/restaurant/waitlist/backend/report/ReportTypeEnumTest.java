package com.restaurant.waitlist.backend.report;

import com.restaurant.waitlist.backend.enums.ReportType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReportTypeEnumTest {

    @Test
    void shouldResolveConfiguredReportTypesCaseInsensitively() {
        assertEquals(ReportType.OVERALL, ReportType.fromValue("overall"));
        assertEquals(ReportType.LOCATION, ReportType.fromValue("location"));
        assertEquals(ReportType.PERFORMANCE, ReportType.fromValue("performance"));
        assertEquals(ReportType.REDEMPTION, ReportType.fromValue("redemption"));
        assertEquals(ReportType.CUSTOMER, ReportType.fromValue("customer"));
    }
}
