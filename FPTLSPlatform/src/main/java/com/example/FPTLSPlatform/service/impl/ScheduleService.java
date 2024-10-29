package com.example.FPTLSPlatform.service.impl;


import com.example.FPTLSPlatform.dto.ScheduleDTO;
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


    public ScheduleDTO createSchedule(ScheduleDTO scheduleDto) {
        Optional<Schedule> existingSchedule = scheduleRepository.findByDayOfWeekAndSlot_SlotId(
                scheduleDto.getDayOfWeek(),
                scheduleDto.getSlotId()
        );

        if (existingSchedule.isPresent()) {
            throw new RuntimeException("Đã có lịch học cho slot này vào ngày " + scheduleDto.getDayOfWeek());
        }

        Schedule schedule = mapToEntity(scheduleDto);
        Schedule savedSchedule = scheduleRepository.save(schedule);
        return mapToDto(savedSchedule);
    }

    public ScheduleDTO updateSchedule(Long scheduleId, ScheduleDTO scheduleDto) {
        Schedule existingSchedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found with id: " + scheduleId));
        Optional<Schedule> conflictingSchedule = scheduleRepository.findByDayOfWeekAndSlot_SlotId(
                scheduleDto.getDayOfWeek(),
                scheduleDto.getSlotId()
        );
        if (conflictingSchedule.isPresent() && !conflictingSchedule.get().getScheduleId().equals(scheduleId)) {
            throw new RuntimeException("Đã có lịch học cho slot này vào ngày " + scheduleDto.getDayOfWeek());
        }
        existingSchedule.setStartDate(scheduleDto.getStartDate());
        existingSchedule.setEndDate(scheduleDto.getEndDate());
        existingSchedule.setDayOfWeek(scheduleDto.getDayOfWeek());

        if (scheduleDto.getSlotId() != null) {
            Slot slot = slotRepository.findById(scheduleDto.getSlotId())
                    .orElseThrow(() -> new RuntimeException("Slot not found with id: " + scheduleDto.getSlotId()));
            existingSchedule.setSlot(slot);
        }

        if (scheduleDto.getClassId() != null) {
            List<Class> classes = scheduleDto.getClassId().stream()
                    .map(id -> classRepository.findById(id)
                            .orElseThrow(() -> new RuntimeException("Class not found with id: " + id)))
                    .collect(Collectors.toList());
            existingSchedule.setClasses(classes);
        }

        Schedule updatedSchedule = scheduleRepository.save(existingSchedule);
        return mapToDto(updatedSchedule);
    }

    public ScheduleDTO getScheduleById(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found with id: " + scheduleId));
        return mapToDto(schedule);
    }

    public List<ScheduleDTO> getAllSchedules() {
        return scheduleRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public ScheduleDTO getSchedulesByClassId(Long classId) {
        return mapToDto(scheduleRepository.findByClasses_ClassId(classId));
    }

    public List<ScheduleDTO> getSchedulesBySlotId(Long slotId) {
        return scheduleRepository.findBySlot_SlotId(slotId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private Schedule mapToEntity(ScheduleDTO dto) {
        Slot slot = dto.getSlotId() != null ? slotRepository.findById(dto.getSlotId())
                .orElseThrow(() -> new RuntimeException("Slot not found with id: " + dto.getSlotId())) : null;

        List<Class> classes = dto.getClassId() != null ? dto.getClassId().stream()
                .map(id -> classRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Class not found with id: " + id)))
                .toList() : null;

        return Schedule.builder()
                .scheduleId(dto.getScheduleId())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .dayOfWeek(dto.getDayOfWeek())
                .slot(slot)
                .classes(classes)
                .build();
    }

    private ScheduleDTO mapToDto(Schedule schedule) {
        return ScheduleDTO.builder()
                .scheduleId(schedule.getScheduleId())
                .startDate(schedule.getStartDate())
                .endDate(schedule.getEndDate())
                .dayOfWeek(schedule.getDayOfWeek())
                .slotId(schedule.getSlot() != null ? schedule.getSlot().getSlotId() : null)
                .classId(schedule.getClasses() != null ?
                        schedule.getClasses().stream().map(Class::getClassId).collect(Collectors.toList()) : null)
                .build();
    }

}
