package com.example.FPTLSPlatform.service.impl;



import com.example.FPTLSPlatform.dto.ScheduleDto;
import com.example.FPTLSPlatform.model.Class;
import com.example.FPTLSPlatform.model.Schedule;
import com.example.FPTLSPlatform.model.Slot;
import com.example.FPTLSPlatform.repository.ClassRepository;
import com.example.FPTLSPlatform.repository.ScheduleRepository;
import com.example.FPTLSPlatform.repository.SlotRepository;
import com.example.FPTLSPlatform.service.IScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ScheduleService implements IScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private SlotRepository slotRepository;

    @Autowired
    private ClassRepository classRepository;


    public ScheduleDto createSchedule(ScheduleDto scheduleDto) {
        Schedule schedule = mapToEntity(scheduleDto);
        Schedule savedSchedule = scheduleRepository.save(schedule);
        return mapToDto(savedSchedule);
    }

    public ScheduleDto updateSchedule(Long scheduleId, ScheduleDto scheduleDto) {
        Schedule existingSchedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found with id: " + scheduleId));

        existingSchedule.setStartDate(scheduleDto.getStartDate());
        existingSchedule.setEndDate(scheduleDto.getEndDate());
        existingSchedule.setDayOfWeek(scheduleDto.getDayOfWeek());

        if (scheduleDto.getSlotId() != null) {
            Slot slot = slotRepository.findById(scheduleDto.getSlotId())
                    .orElseThrow(() -> new RuntimeException("Slot not found with id: " + scheduleDto.getSlotId()));
            existingSchedule.setSlot(slot);
        }

        if (scheduleDto.getClassId() != null) {
            Class aClass = classRepository.findById(scheduleDto.getClassId())
                    .orElseThrow(() -> new RuntimeException("Class not found with id: " + scheduleDto.getClassId()));
            existingSchedule.setClasses(aClass);
        }

        Schedule updatedSchedule = scheduleRepository.save(existingSchedule);
        return mapToDto(updatedSchedule);
    }
    public ScheduleDto getScheduleById(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found with id: " + scheduleId));
        return mapToDto(schedule);
    }
    public List<ScheduleDto> getAllSchedules() {
        return scheduleRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    public List<ScheduleDto> getSchedulesByClassId(Long classId) {
        return scheduleRepository.findByClasses_ClassId(classId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    public List<ScheduleDto> getSchedulesBySlotId(Long slotId) {
        return scheduleRepository.findBySlot_SlotId(slotId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    private Schedule mapToEntity(ScheduleDto dto) {
        Slot slot = dto.getSlotId() != null ? slotRepository.findById(dto.getSlotId())
                .orElseThrow(() -> new RuntimeException("Slot not found with id: " + dto.getSlotId())) : null;

        Class aClass = dto.getClassId() != null ? classRepository.findById(dto.getClassId())
                .orElseThrow(() -> new RuntimeException("Class not found with id: " + dto.getClassId())) : null;

        return Schedule.builder()
                .scheduleId(dto.getScheduleId())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .dayOfWeek(dto.getDayOfWeek())
                .slot(slot)
                .classes(aClass)
                .build();
    }

    private ScheduleDto mapToDto(Schedule schedule) {
        return ScheduleDto.builder()
                .scheduleId(schedule.getScheduleId())
                .startDate(schedule.getStartDate())
                .endDate(schedule.getEndDate())
                .dayOfWeek(schedule.getDayOfWeek())
                .slotId(schedule.getSlot() != null ? schedule.getSlot().getSlotId() : null)
                .classId(schedule.getClasses() != null ? schedule.getClasses().getClassId() : null)
                .build();
    }

}
