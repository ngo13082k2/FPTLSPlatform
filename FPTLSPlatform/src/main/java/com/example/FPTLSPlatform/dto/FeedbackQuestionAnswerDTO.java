package com.example.FPTLSPlatform.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedbackQuestionAnswerDTO {
    private Long questionId;
    private int rating;
    private String comment;
}
