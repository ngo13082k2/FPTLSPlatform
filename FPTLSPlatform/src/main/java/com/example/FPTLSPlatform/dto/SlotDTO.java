package com.example.FPTLSPlatform.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Builder
@Setter
public class SlotDTO {
    private Long slotId;

    private String period;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime start;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime end;
}


