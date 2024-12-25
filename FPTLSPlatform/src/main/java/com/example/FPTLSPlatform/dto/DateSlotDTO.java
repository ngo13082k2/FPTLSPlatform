package com.example.FPTLSPlatform.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DateSlotDTO {
    private LocalDate date;
    private List<Long> slotIds;
}
