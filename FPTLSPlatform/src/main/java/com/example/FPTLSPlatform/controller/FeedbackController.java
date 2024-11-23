package com.example.FPTLSPlatform.controller;

import com.example.FPTLSPlatform.dto.FeedbackCommentDTO;
import com.example.FPTLSPlatform.dto.FeedbackDTO;
import com.example.FPTLSPlatform.dto.FeedbackSubmissionDTO;
import com.example.FPTLSPlatform.model.User;
import com.example.FPTLSPlatform.service.IFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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

    @GetMapping("/average-feedback/class/{classId}")
    public ResponseEntity<Double> getAverageFeedbackForClass(@PathVariable Long classId) {
        double average = feedbackService.getAverageOfAllFeedbackQuestionsInClass(classId);
        return ResponseEntity.ok(average);
    }

    @GetMapping("/average-feedback/teacher/{teacherName}")
    public ResponseEntity<Map<String, Object>> getAverageFeedbackForTeacher(@PathVariable String teacherName) {
        try {
            double averageFeedback = feedbackService.getAverageFeedbackForTeacher(teacherName);
            Map<String, Object> response = new HashMap<>();
            response.put("teacherName", teacherName);
            response.put("averageFeedback", averageFeedback);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/send-email/{classId}")
    public ResponseEntity<?> sendFeedbackForClass(@PathVariable Long classId) {
        feedbackService.sendFeedbackForClass(classId);
        return ResponseEntity.ok("Send mail for teacher's class successfull");
    }
    @GetMapping("/comments")
    public ResponseEntity<List<FeedbackCommentDTO>> getAllFeedbackComments() {
        List<FeedbackCommentDTO> comments = feedbackService.getAllFeedbackComments();
        return ResponseEntity.ok(comments);
    }
}
