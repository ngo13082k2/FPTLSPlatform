package com.example.FPTLSPlatform.exception;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Data
@Builder
public class ScheduleDTO {
    private Long scheduleId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String dayOfWeek;
    private Long slotId;
    private List<Long> classId;
}