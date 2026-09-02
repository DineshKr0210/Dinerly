package com.restaurant.waitlist.backend.repository;

import java.time.LocalDate;

public interface CustomerAggregation {
    String getGuest();
    String getContact();
    Long getVisits();
    LocalDate getLastVisit();
}
