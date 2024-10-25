package com.example.FPTLSPlatform.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class FeedbackSubmissionDTO {
    private String username;
    private Long classId;
    private String comment;
    private List<FeedbackQuestionAnswerDTO> feedbackAnswers;

}
