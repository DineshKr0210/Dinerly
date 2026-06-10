package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.SmsTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SmsTemplateRepository extends JpaRepository<SmsTemplate, Long> {
    Optional<SmsTemplate> findByTemplateType(String templateType);
}

