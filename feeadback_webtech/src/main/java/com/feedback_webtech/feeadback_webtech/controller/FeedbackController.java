package com.feedback_webtech.feeadback_webtech.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.feedback_webtech.feeadback_webtech.model.Feedback;
import com.feedback_webtech.feeadback_webtech.repository.FeedbackRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin(origins = "http://localhost:5173")
public class FeedbackController {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @PostMapping
    public ResponseEntity<?> createFeedback(@Valid @RequestBody Feedback feedback) {
        Feedback saved = feedbackRepository.save(feedback);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<Feedback>> getAllFeedback() {
        List<Feedback> feedbacks = feedbackRepository.findAll();
        return ResponseEntity.ok(feedbacks);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFeedback(@PathVariable String id) {
        Optional<Feedback> existing = feedbackRepository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Feedback not found with id: " + id));
        }
        feedbackRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Feedback deleted successfully"));
    }

    @GetMapping("/average")
    public ResponseEntity<?> getAverageRating() {
        List<Feedback> feedbacks = feedbackRepository.findAll();
        if (feedbacks.isEmpty()) {
            return ResponseEntity.ok(Map.of("average", 0));
        }
        double average = feedbacks.stream()
                .mapToInt(Feedback::getRating)
                .average()
                .orElse(0);
        return ResponseEntity.ok(Map.of("average", Math.round(average * 10.0) / 10.0));
    }
}
