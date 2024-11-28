package com.example.FPTLSPlatform.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackSummaryDTO {
    private Long questionId;
    private Map<Integer, Long> ratingCount;
    private Double averageRating;
    private Long totalResponses;
}
