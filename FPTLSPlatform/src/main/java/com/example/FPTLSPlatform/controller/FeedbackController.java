package com.example.FPTLSPlatform.controller;

import com.example.FPTLSPlatform.dto.FeedbackSubmissionDTO;
import com.example.FPTLSPlatform.model.User;
import com.example.FPTLSPlatform.service.IFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    @Autowired
    private IFeedbackService feedbackService;

    @PostMapping("/submit")
    public ResponseEntity<String> submitFeedbackForAllCategories(@RequestBody FeedbackSubmissionDTO feedbackSubmissionDTO, @AuthenticationPrincipal User student) {
        feedbackService.submitFeedbackForAllCategories(feedbackSubmissionDTO, student);
        return ResponseEntity.ok("Feedback submitted successfully");
    }
    @GetMapping("/class/{classId}/summary")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<List<Map<String, Object>>> getClassFeedbackSummary(@PathVariable Long classId) {
        List<Map<String, Object>> summary = feedbackService.getClassFeedbackSummary(classId);
        return ResponseEntity.ok(summary);
    }
}
