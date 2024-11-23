package com.example.FPTLSPlatform.repository;

import com.example.FPTLSPlatform.dto.FeedbackCommentDTO;
import com.example.FPTLSPlatform.model.Class;
import com.example.FPTLSPlatform.model.Feedback;
import com.example.FPTLSPlatform.model.FeedbackQuestion;
import com.example.FPTLSPlatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByFeedbackQuestionId(Long questionId);
    List<Feedback> findByClassEntityClassId(Long classId);
    boolean existsByStudentAndClassEntityAndFeedbackQuestionAndIsFeedbackTrue(User student, Class classEntity, FeedbackQuestion feedbackQuestion);
    @Query("SELECT new com.example.FPTLSPlatform.dto.FeedbackCommentDTO(f.student.userName, f.comment) " +
            "FROM Feedback f WHERE f.comment IS NOT NULL")
    List<FeedbackCommentDTO> findAllFeedbackComments();

}
