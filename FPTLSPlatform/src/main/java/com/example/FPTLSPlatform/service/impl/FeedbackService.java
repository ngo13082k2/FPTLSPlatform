package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.dto.FeedbackCategoryDTO;
import com.example.FPTLSPlatform.dto.FeedbackQuestionAnswerDTO;
import com.example.FPTLSPlatform.dto.FeedbackSubmissionDTO;
import com.example.FPTLSPlatform.model.Class;
import com.example.FPTLSPlatform.model.Feedback;
import com.example.FPTLSPlatform.model.FeedbackQuestion;
import com.example.FPTLSPlatform.model.User;
import com.example.FPTLSPlatform.repository.ClassRepository;
import com.example.FPTLSPlatform.repository.FeedbackQuestionRepository;
import com.example.FPTLSPlatform.repository.FeedbackRepository;
import com.example.FPTLSPlatform.service.IFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FeedbackService implements IFeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private FeedbackQuestionRepository feedbackQuestionRepository;

    @Autowired
    private ClassRepository classRepository;

    public void submitFeedbackForAllCategories(FeedbackSubmissionDTO feedbackSubmission, User student) {
        Class classEntity = classRepository.findById(feedbackSubmission.getClassId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid class ID"));

        for (FeedbackCategoryDTO categoryDTO : feedbackSubmission.getFeedbackCategories()) {
            for (FeedbackQuestionAnswerDTO feedbackAnswer : categoryDTO.getFeedbackAnswers()) {
                FeedbackQuestion question = feedbackQuestionRepository.findById(feedbackAnswer.getQuestionId())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid question ID: " + feedbackAnswer.getQuestionId()));

                Feedback feedback = Feedback.builder()
                        .student(student)
                        .classEntity(classEntity)
                        .feedbackQuestion(question)
                        .rating(feedbackAnswer.getRating())
                        .comment(feedbackAnswer.getComment())
                        .build();

                feedbackRepository.save(feedback);
            }
        }
    }
    public List<Map<String, Object>> getClassFeedbackSummary(Long classId) {
        List<Feedback> feedbacks = feedbackRepository.findByClassEntityClassId(classId);
        // Map để nhóm feedback theo questionId và tính toán kết quả
        Map<Long, List<Feedback>> feedbackByQuestion = feedbacks.stream()
                .collect(Collectors.groupingBy(feedback -> feedback.getFeedbackQuestion().getId()));
        List<Map<String, Object>> resultList = new ArrayList<>();
        // Tính toán số lượng và điểm trung bình cho mỗi questionId
        for (Map.Entry<Long, List<Feedback>> entry : feedbackByQuestion.entrySet()) {
            Long questionId = entry.getKey();
            List<Feedback> questionFeedbacks = entry.getValue();
            // Tính tổng số phản hồi theo từng mức điểm (1-5)
            Map<Integer, Long> ratingCount = questionFeedbacks.stream()
                    .collect(Collectors.groupingBy(Feedback::getRating, Collectors.counting()));
            // Tính điểm trung bình
            double totalScore = questionFeedbacks.stream()
                    .mapToDouble(Feedback::getRating)
                    .sum();
            double averageRating = totalScore / questionFeedbacks.size();
            // Chuẩn bị kết quả cho questionId
            Map<String, Object> summary = new HashMap<>();
            summary.put("questionId", questionId);
            summary.put("ratingCount", ratingCount);
            summary.put("averageRating", averageRating);
            summary.put("totalResponses", questionFeedbacks.size());

            resultList.add(summary);
        }

        return resultList;
    }
}
