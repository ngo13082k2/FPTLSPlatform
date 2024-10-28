package com.example.FPTLSPlatform.service;

import com.example.FPTLSPlatform.dto.ScheduleDto;

import java.util.List;

public interface IScheduleService {
    ScheduleDto createSchedule(ScheduleDto scheduleDto);
    ScheduleDto updateSchedule(Long scheduleId, ScheduleDto scheduleDto);
    List<ScheduleDto> getSchedulesBySlotId(Long slotId);
    List<ScheduleDto> getSchedulesByClassId(Long classId);
    List<ScheduleDto> getAllSchedules();
    ScheduleDto getScheduleById(Long scheduleId);

}
