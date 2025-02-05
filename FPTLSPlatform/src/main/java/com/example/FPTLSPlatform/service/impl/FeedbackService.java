package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.dto.*;
import com.example.FPTLSPlatform.model.*;
import com.example.FPTLSPlatform.model.Class;
import com.example.FPTLSPlatform.model.System;
import com.example.FPTLSPlatform.model.enums.ClassStatus;
import com.example.FPTLSPlatform.model.enums.OrderStatus;
import com.example.FPTLSPlatform.repository.*;
import com.example.FPTLSPlatform.service.IEmailService;
import com.example.FPTLSPlatform.service.IFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    private final SystemRepository systemRepository;

    @Autowired
    public FeedbackService(FeedbackRepository feedbackRepository, FeedbackQuestionRepository feedbackQuestionRepository, OrderDetailRepository orderDetailRepository, ClassRepository classRepository, IEmailService emailService, SystemRepository systemRepository) {
        this.feedbackRepository = feedbackRepository;
        this.feedbackQuestionRepository = feedbackQuestionRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.classRepository = classRepository;
        this.emailService = emailService;
        this.systemRepository = systemRepository;
    }

    public FeedbackSubmissionDTO submitFeedbackForOrder(Long orderId, FeedbackSubmissionDTO feedbackSubmission) {
        // Fetch order details based on order ID
        Page<OrderDetail> orderDetails = orderDetailRepository.findByOrderOrderId(orderId, Pageable.unpaged());

        if (orderDetails.isEmpty()) {
            throw new IllegalArgumentException("Invalid order ID: " + orderId);
        }

        Order order = orderDetails.getContent().get(0).getOrder();
        User student = order.getUser();

        // Check if the order status is "COMPLETED" to allow feedback submission
        if (!order.getStatus().equals(OrderStatus.COMPLETED)) {
            throw new IllegalArgumentException("Order must be complete to submit feedback for order ID: " + orderId);
        }

        // Get the class entity related to this order detail
        Class classEntity = orderDetails.getContent().get(0).getClasses();
        LocalDate classEndDate = classEntity.getDateSlots().stream()
                .map(dateSlot -> dateSlot.getDate().atTime(dateSlot.getSlot().getEndTime())) // Lấy thời gian kết thúc
                .max(LocalDateTime::compareTo) // Tìm thời điểm muộn nhất
                .get() // Lấy giá trị từ Optional
                .toLocalDate(); // Chuyển đổi sang LocalDate


        // Define the maximum number of days allowed for feedback submission (e.g., 7 days)
        int feedbackDeadlineInDays = 7;
        System checkTimeBeforeStart = systemRepository.findByName("feedback_deadline");
        int checkTime = checkTimeBeforeStart != null
                ? Integer.parseInt(checkTimeBeforeStart.getValue())
                : feedbackDeadlineInDays;
        // Calculate the deadline date for feedback submission
        LocalDate feedbackDeadline = classEndDate.plusDays(feedbackDeadlineInDays);

        // Check if the current date is after the feedback deadline
        if (LocalDate.now().isAfter(feedbackDeadline)) {
            throw new IllegalArgumentException("Feedback submission deadline has passed for order ID: " + orderId);
        }

        String commonComment = feedbackSubmission.getComment();
        for (FeedbackQuestionAnswerDTO feedbackAnswer : feedbackSubmission.getFeedbackAnswers()) {
            FeedbackQuestion question = feedbackQuestionRepository.findById(feedbackAnswer.getQuestionId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid question ID: " + feedbackAnswer.getQuestionId()));

            boolean feedbackExists = feedbackRepository.existsByStudentAndClassEntityAndFeedbackQuestionAndIsFeedbackTrue(student, classEntity, question);
            if (feedbackExists) {
                throw new IllegalArgumentException("You have already provided feedback for question ID: " + feedbackAnswer.getQuestionId());
            }

            // Create and save the feedback
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

        // Prepare response DTO
        FeedbackSubmissionDTO response = new FeedbackSubmissionDTO();
        response.setUsername(student.getUserName());
        response.setClassId(classEntity.getClassId());
        response.setComment(commonComment);
        response.setFeedbackAnswers(feedbackSubmission.getFeedbackAnswers());

        return response;
    }


    public List<FeedbackSummaryDTO> getClassFeedbackSummary(Long classId) {
        List<Feedback> feedbacks = feedbackRepository.findByClassEntityClassId(classId);

        // Nhóm feedback theo questionId
        Map<Long, List<Feedback>> feedbackByQuestion = feedbacks.stream()
                .collect(Collectors.groupingBy(feedback -> feedback.getFeedbackQuestion().getId()));

        List<FeedbackSummaryDTO> resultList = new ArrayList<>();

        // Tính toán số lượng và điểm trung bình cho mỗi questionId
        for (Map.Entry<Long, List<Feedback>> entry : feedbackByQuestion.entrySet()) {
            Long questionId = entry.getKey();
            List<Feedback> questionFeedbacks = entry.getValue();

            // Tính số lượng các mức rating (1-5)
            Map<Integer, Long> ratingCount = questionFeedbacks.stream()
                    .collect(Collectors.groupingBy(Feedback::getRating, Collectors.counting()));

            // Tính điểm trung bình
            double totalScore = questionFeedbacks.stream()
                    .mapToDouble(Feedback::getRating)
                    .sum();
            double averageRating = totalScore / questionFeedbacks.size();

            // Tạo DTO cho câu hỏi
            FeedbackSummaryDTO summaryDTO = new FeedbackSummaryDTO(
                    questionId,
                    ratingCount,
                    averageRating,
                    (long) questionFeedbacks.size()
            );

            resultList.add(summaryDTO);
        }

        return resultList;
    }


    public double getAverageOfAllFeedbackQuestionsInClass(Long classId) {
        List<FeedbackSummaryDTO> feedbackSummary = getClassFeedbackSummary(classId);

        double totalAverageRating = feedbackSummary.stream()
                .mapToDouble(summary -> (double) summary.getAverageRating())
                .sum();

        return feedbackSummary.isEmpty() ? 0 : totalAverageRating / feedbackSummary.size();
    }

    public double getAverageFeedbackForTeacher(String teacherName) {
        List<Class> completedClasses = classRepository.findByTeacherTeacherNameAndStatus(teacherName, ClassStatus.COMPLETED);

        if (completedClasses.isEmpty()) {
            return 0;
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
            return 0;
        }

        return totalAverage / classCount;
    }

    @Override
    public void sendFeedbackForClass(Long classId) {
        List<FeedbackSummaryDTO> feedbackSummary = getClassFeedbackSummary(classId);
        Class clazz = classRepository.getReferenceById(classId);

        List<Feedback> feedbacks = feedbackRepository.findByClassEntityClassId(classId);
        if (feedbacks.isEmpty()) {
            throw new IllegalArgumentException("No feedback found for class ID: " + classId);
        }

        List<FeedbackQuestion> feedbackQuestions = feedbackQuestionRepository.findAll();
        Context context = new Context();
        context.setVariable("class", clazz);
        context.setVariable("feedbackSummary", feedbackSummary);
        context.setVariable("questions", feedbackQuestions);
        context.setVariable("feedbackGroupedByLearner", groupFeedbackByLearner(feedbacks));
        emailService.sendEmail(clazz.getTeacher().getEmail(), "Send feedback for teacher's class successful", "feedback-email", context);
    }

    public Map<User, List<Feedback>> groupFeedbackByLearner(List<Feedback> feedbackList) {
        return feedbackList.stream()
                .collect(Collectors.groupingBy(Feedback::getStudent));
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

    public List<FeedbackCommentDTO> getAllFeedbackComments() {
        return feedbackRepository.findAllFeedbackComments();
    }


}
