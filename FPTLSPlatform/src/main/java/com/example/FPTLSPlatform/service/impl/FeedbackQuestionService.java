package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.model.FeedbackCategory;
import com.example.FPTLSPlatform.model.FeedbackQuestion;
import com.example.FPTLSPlatform.repository.FeedbackCategoryRepository;
import com.example.FPTLSPlatform.repository.FeedbackQuestionRepository;
import com.example.FPTLSPlatform.service.IFeedbackQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedbackQuestionService implements IFeedbackQuestionService {
    @Autowired
    private FeedbackQuestionRepository feedbackQuestionRepository;

    @Autowired
    private FeedbackCategoryRepository feedbackCategoryRepository;

    public FeedbackQuestion createFeedbackQuestion(String questionText, Long categoryId) {
        FeedbackCategory category = feedbackCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid category ID"));

        FeedbackQuestion question = FeedbackQuestion.builder()
                .questionText(questionText)
                .feedbackCategory(category)
                .build();

        return feedbackQuestionRepository.save(question);
    }
    public List<FeedbackQuestion> getAllFeedbackQuestions() {
        return feedbackQuestionRepository.findAll();
    }
}
