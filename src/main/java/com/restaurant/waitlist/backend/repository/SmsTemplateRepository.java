package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.SmsTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface SmsTemplateRepository extends JpaRepository<SmsTemplate, Long> {
    @Query("SELECT s FROM SmsTemplate s WHERE s.templateType = :templateType")
    Optional<SmsTemplate> findByTemplateType(@Param("templateType") String templateType);
}

