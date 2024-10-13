package com.example.FPTLSPlatform.service;

import com.example.FPTLSPlatform.dto.FeedbackSubmissionDTO;
import com.example.FPTLSPlatform.model.User;

import java.util.List;
import java.util.Map;

public interface IFeedbackService {
    void submitFeedbackForAllCategories(FeedbackSubmissionDTO feedbackSubmission, User student);
    List<Map<String, Object>> getClassFeedbackSummary(Long classId);
}
