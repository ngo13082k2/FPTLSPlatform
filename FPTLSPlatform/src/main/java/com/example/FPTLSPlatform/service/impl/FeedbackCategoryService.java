package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.model.FeedbackCategory;
import com.example.FPTLSPlatform.repository.FeedbackCategoryRepository;
import com.example.FPTLSPlatform.service.IFeedbackCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FeedbackCategoryService implements IFeedbackCategoryService {
    @Autowired
    private FeedbackCategoryRepository feedbackCategoryRepository;

    public FeedbackCategory createFeedbackCategory(String categoryName) {
        FeedbackCategory category = FeedbackCategory.builder()
                .categoryName(categoryName)
                .build();
        return feedbackCategoryRepository.save(category);
    }
}
