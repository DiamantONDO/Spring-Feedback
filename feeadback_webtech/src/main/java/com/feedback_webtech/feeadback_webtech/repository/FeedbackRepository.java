package com.feedback_webtech.feeadback_webtech.repository;

import com.feedback_webtech.feeadback_webtech.model.Feedback;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepository extends MongoRepository<Feedback, String> {
}