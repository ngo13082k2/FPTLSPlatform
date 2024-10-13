package com.example.FPTLSPlatform.controller;

import com.example.FPTLSPlatform.model.FeedbackCategory;
import com.example.FPTLSPlatform.service.IFeedbackCategoryService;
import com.example.FPTLSPlatform.service.impl.FeedbackCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/feedback-category")
public class FeedbackCategoryController {

    @Autowired
    private IFeedbackCategoryService feedbackCategoryService;

    @PostMapping
    public ResponseEntity<FeedbackCategory> createCategory(@RequestBody Map<String, String> request) {
        String categoryName = request.get("categoryName");
        FeedbackCategory category = feedbackCategoryService.createFeedbackCategory(categoryName);
        return ResponseEntity.ok(category);
    }
}
