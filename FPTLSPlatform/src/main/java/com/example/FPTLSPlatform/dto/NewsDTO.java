package com.example.FPTLSPlatform.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsDTO {
    private Long id;
    private LocalDate date;
    private LocalTime time;
    private String title;
    private String content;
}
