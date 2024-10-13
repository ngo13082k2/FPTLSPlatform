package com.example.FPTLSPlatform.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateFeedbackCategoryRequest {
    private String categoryName;
    private List<String> questions;
}
