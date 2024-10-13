package com.example.FPTLSPlatform.controller;

import com.example.FPTLSPlatform.model.FeedbackQuestion;
import com.example.FPTLSPlatform.service.IFeedbackQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/feedback-question")
public class FeedbackQuestionController {

    @Autowired
    private IFeedbackQuestionService feedbackQuestionService;

    @PostMapping
    public ResponseEntity<FeedbackQuestion> createQuestion(@RequestBody Map<String, Object> request) {
        String questionText = (String) request.get("questionText");
        Long categoryId = ((Number) request.get("categoryId")).longValue();
        FeedbackQuestion question = feedbackQuestionService.createFeedbackQuestion(questionText, categoryId);
        return ResponseEntity.ok(question);
    }
}
