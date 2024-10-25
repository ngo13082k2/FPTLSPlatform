package com.example.FPTLSPlatform.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class FeedbackSubmissionDTO {
    private Long classId;
    private List<FeedbackCategoryDTO> feedbackCategories;
    private String username;
    private String comments;

}
