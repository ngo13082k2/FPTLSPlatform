package com.example.FPTLSPlatform.service;

import com.example.FPTLSPlatform.dto.FeedbackCommentDTO;
import com.example.FPTLSPlatform.dto.FeedbackDTO;
import com.example.FPTLSPlatform.dto.FeedbackSubmissionDTO;
import com.example.FPTLSPlatform.dto.FeedbackSummaryDTO;
import com.example.FPTLSPlatform.model.User;

import java.util.List;
import java.util.Map;

public interface IFeedbackService {
    FeedbackSubmissionDTO submitFeedbackForOrder(Long orderId, FeedbackSubmissionDTO feedbackSubmission);

    List<FeedbackSummaryDTO> getClassFeedbackSummary(Long classId);

    List<FeedbackDTO> getAllFeedbackByClassId(Long classId);

    double getAverageOfAllFeedbackQuestionsInClass(Long classId);

    double getAverageFeedbackForTeacher(String teacherName);

    void sendFeedbackForClass(Long classId);

    List<FeedbackCommentDTO> getAllFeedbackComments();
}
