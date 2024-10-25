package com.example.FPTLSPlatform.service;

import com.example.FPTLSPlatform.model.FeedbackQuestion;

import java.util.List;

public interface IFeedbackQuestionService {
    FeedbackQuestion createFeedbackQuestion(String questionText, Long categoryId);
    List<FeedbackQuestion> getAllFeedbackQuestions();
}
