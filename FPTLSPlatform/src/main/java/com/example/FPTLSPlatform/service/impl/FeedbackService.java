package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.dto.FeedbackCategoryDTO;
import com.example.FPTLSPlatform.dto.FeedbackDTO;
import com.example.FPTLSPlatform.dto.FeedbackQuestionAnswerDTO;
import com.example.FPTLSPlatform.dto.FeedbackSubmissionDTO;
import com.example.FPTLSPlatform.model.*;
import com.example.FPTLSPlatform.model.Class;
import com.example.FPTLSPlatform.model.enums.ClassStatus;
import com.example.FPTLSPlatform.model.enums.OrderStatus;
import com.example.FPTLSPlatform.repository.*;
import com.example.FPTLSPlatform.service.IEmailService;
import com.example.FPTLSPlatform.service.IFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FeedbackService implements IFeedbackService {

    private final FeedbackRepository feedbackRepository;

    private final FeedbackQuestionRepository feedbackQuestionRepository;

    private final OrderDetailRepository orderDetailRepository;

    private final ClassRepository classRepository;

    private final IEmailService emailService;

    @Autowired
    public FeedbackService(FeedbackRepository feedbackRepository, FeedbackQuestionRepository feedbackQuestionRepository, OrderDetailRepository orderDetailRepository, ClassRepository classRepository, IEmailService emailService) {
        this.feedbackRepository = feedbackRepository;
        this.feedbackQuestionRepository = feedbackQuestionRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.classRepository = classRepository;
        this.emailService = emailService;
    }

    public FeedbackSubmissionDTO submitFeedbackForOrder(Long orderId, FeedbackSubmissionDTO feedbackSubmission) {
        Page<OrderDetail> orderDetails = orderDetailRepository.findByOrderOrderId(orderId, Pageable.unpaged());

        if (orderDetails.isEmpty()) {
            throw new IllegalArgumentException("Invalid order ID: " + orderId);
        }

        Order order = orderDetails.getContent().get(0).getOrder();
        User student = order.getUser();

        if (!order.getStatus().equals(OrderStatus.COMPLETED)) {
            throw new IllegalArgumentException("Order must be complete to submit feedback for order ID: " + orderId);
        }

        String commonComment = feedbackSubmission.getComment();
        for (FeedbackQuestionAnswerDTO feedbackAnswer : feedbackSubmission.getFeedbackAnswers()) {
            FeedbackQuestion question = feedbackQuestionRepository.findById(feedbackAnswer.getQuestionId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid question ID: " + feedbackAnswer.getQuestionId()));

            Class classEntity = orderDetails.getContent().get(0).getClasses();
            boolean feedbackExists = feedbackRepository.existsByStudentAndClassEntityAndFeedbackQuestionAndIsFeedbackTrue(student, classEntity, question);
            if (feedbackExists) {
                throw new IllegalArgumentException("You have already provided feedback for question ID: " + feedbackAnswer.getQuestionId());

            }
            Feedback feedback = Feedback.builder()
                    .student(student)
                    .classEntity(classEntity)
                    .feedbackQuestion(question)
                    .rating(feedbackAnswer.getRating())
                    .comment(commonComment)
                    .isFeedback(true)
                    .build();

            feedbackRepository.save(feedback);
        }

        FeedbackSubmissionDTO response = new FeedbackSubmissionDTO();
        response.setUsername(student.getUserName());
        response.setClassId(orderDetails.getContent().get(0).getClasses().getClassId());
        response.setComment(commonComment);
        response.setFeedbackAnswers(feedbackSubmission.getFeedbackAnswers());

        return response;
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

    public double getAverageOfAllFeedbackQuestionsInClass(Long classId) {
        List<Map<String, Object>> feedbackSummary = getClassFeedbackSummary(classId);

        double totalAverageRating = feedbackSummary.stream()
                .mapToDouble(summary -> (double) summary.get("averageRating"))
                .sum();

        return feedbackSummary.isEmpty() ? 0 : totalAverageRating / feedbackSummary.size();
    }

    public double getAverageFeedbackForTeacher(String teacherName) {
        List<Class> completedClasses = classRepository.findByTeacherTeacherNameAndStatus(teacherName, ClassStatus.COMPLETED);

        if (completedClasses.isEmpty()) {
            throw new RuntimeException("No completed classes found for teacher: " + teacherName);
        }

        double totalAverage = 0;
        int classCount = 0;

        for (Class clazz : completedClasses) {
            List<Feedback> feedbacks = feedbackRepository.findByClassEntityClassId(clazz.getClassId());
            if (!feedbacks.isEmpty()) {
                double classAverage = feedbacks.stream()
                        .mapToInt(Feedback::getRating)
                        .average()
                        .orElse(0);
                totalAverage += classAverage;
                classCount++;
            }
        }

        if (classCount == 0) {
            throw new RuntimeException("No feedback found for completed classes of teacher: " + teacherName);
        }

        return totalAverage / classCount;
    }

    @Override
    public void sendFeedbackForClass(Long classId) {
        List<Map<String, Object>> feedbackSummary = getClassFeedbackSummary(classId);
        Class clazz = classRepository.getReferenceById(classId);

        double totalAverageRating = feedbackSummary.stream()
                .mapToDouble(summary -> (double) summary.get("averageRating"))
                .sum();
        double averageRating = feedbackSummary.isEmpty() ? 0 : totalAverageRating / feedbackSummary.size();
        Context context = new Context();
        context.setVariable("teacherName", clazz.getTeacher().getTeacherName());
        context.setVariable("feedbackSummary", averageRating);
        emailService.sendEmail(clazz.getTeacher().getTeacherName(), "Send feedback for teacher's class successful", "feedback-email", context);

    }


    public List<FeedbackDTO> getAllFeedbackByClassId(Long classId) {
        List<Feedback> feedbacks = feedbackRepository.findByClassEntityClassId(classId);

        if (feedbacks.isEmpty()) {
            throw new IllegalArgumentException("No feedback found for class ID: " + classId);
        }

        return feedbacks.stream().map(feedback -> {
            FeedbackDTO responseDTO = new FeedbackDTO();
            responseDTO.setStudentUsername(feedback.getStudent().getUserName());
            responseDTO.setQuestionId(feedback.getFeedbackQuestion().getId());
            responseDTO.setRating(feedback.getRating());
            responseDTO.setComment(feedback.getComment());
            return responseDTO;
        }).collect(Collectors.toList());
    }
}
