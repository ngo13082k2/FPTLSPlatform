package com.example.FPTLSPlatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FeedbackCommentDTO {
    private String username;
    private String comment;
}
