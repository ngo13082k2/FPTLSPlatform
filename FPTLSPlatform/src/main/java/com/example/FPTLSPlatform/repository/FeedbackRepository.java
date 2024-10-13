package com.example.FPTLSPlatform.repository;

import com.example.FPTLSPlatform.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByFeedbackQuestionId(Long questionId);
    List<Feedback> findByClassEntityClassId(Long classId);

}
