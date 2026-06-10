package com.restaurant.waitlist.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.waitlist.backend.dto.request.FeedbackRequest;
import com.restaurant.waitlist.backend.entity.Feedback;
import com.restaurant.waitlist.backend.entity.Waitlist;
import com.restaurant.waitlist.backend.repository.FeedbackRepository;
import com.restaurant.waitlist.backend.repository.WaitlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private WaitlistRepository waitlistRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void submitFeedback(FeedbackRequest request) {
        Waitlist waitlist = waitlistRepository.findById(request.getWaitlistId())
                .orElseThrow(() -> new RuntimeException("Waitlist entry not found"));

        try {
            String tagsJson = objectMapper.writeValueAsString(request.getTags());
            
            Feedback feedback = Feedback.builder()
                    .waitlist(waitlist)
                    .rating(request.getRating())
                    .comments(request.getComments())
                    .tags(tagsJson)
                    .build();

            feedbackRepository.save(feedback);
        } catch (Exception e) {
            throw new RuntimeException("Error saving feedback");
        }
    }

    public List<Feedback> getAllFeedback() {
        return feedbackRepository.findAll();
    }
}

