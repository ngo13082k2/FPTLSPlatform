package com.example.FPTLSPlatform.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
@Getter
@Setter
@Data
@Builder
public class ScheduleDto {
    private Long scheduleId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String dayOfWeek;
    private Long slotId;
    private Long classId;
}
