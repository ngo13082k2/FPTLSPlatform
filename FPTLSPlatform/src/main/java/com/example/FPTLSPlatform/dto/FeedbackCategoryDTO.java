package com.example.FPTLSPlatform.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class FeedbackCategoryDTO {
    private Long categoryId;
    private List<FeedbackQuestionAnswerDTO> feedbackAnswers;
}
