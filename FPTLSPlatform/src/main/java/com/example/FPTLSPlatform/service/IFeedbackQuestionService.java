package com.example.FPTLSPlatform.service;

import com.example.FPTLSPlatform.model.FeedbackQuestion;

public interface IFeedbackQuestionService {
    FeedbackQuestion createFeedbackQuestion(String questionText, Long categoryId);
}
