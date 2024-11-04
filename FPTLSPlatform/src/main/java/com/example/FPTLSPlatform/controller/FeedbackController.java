package com.example.FPTLSPlatform.controller;

import com.example.FPTLSPlatform.dto.FeedbackDTO;
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
@RequestMapping("/feedback")
public class FeedbackController {

    @Autowired
    private IFeedbackService feedbackService;

    @PostMapping("/order/{orderId}/submit")
    public ResponseEntity<FeedbackSubmissionDTO> submitFeedbackForOrder(@PathVariable Long orderId, @RequestBody FeedbackSubmissionDTO feedbackSubmissionDTO) {
        FeedbackSubmissionDTO response = feedbackService.submitFeedbackForOrder(orderId, feedbackSubmissionDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/class/{classId}/summary")
    public ResponseEntity<List<Map<String, Object>>> getClassFeedbackSummary(@PathVariable Long classId) {
        List<Map<String, Object>> summary = feedbackService.getClassFeedbackSummary(classId);
        return ResponseEntity.ok(summary);
    }
    @GetMapping("/class/{classId}")
    public ResponseEntity<List<FeedbackDTO>> getAllFeedbackByClassId(@PathVariable Long classId) {
        List<FeedbackDTO> feedbacks = feedbackService.getAllFeedbackByClassId(classId);
        return ResponseEntity.ok(feedbacks);
    }


}
