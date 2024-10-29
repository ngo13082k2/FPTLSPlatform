package com.example.FPTLSPlatform.service;

import com.example.FPTLSPlatform.dto.ScheduleDTO;

import java.util.List;

public interface IScheduleService {
    ScheduleDTO createSchedule(ScheduleDTO scheduleDto);

    ScheduleDTO updateSchedule(Long scheduleId, ScheduleDTO scheduleDto);

    List<ScheduleDTO> getSchedulesBySlotId(Long slotId);

    ScheduleDTO getSchedulesByClassId(Long classId);

    List<ScheduleDTO> getAllSchedules();

    ScheduleDTO getScheduleById(Long scheduleId);

}
